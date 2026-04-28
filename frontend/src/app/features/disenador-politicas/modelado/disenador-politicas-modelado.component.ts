import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { AfterViewInit, Component, DestroyRef, ElementRef, OnDestroy, ViewChild, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import BpmnModeler from 'bpmn-js/lib/Modeler';
import { AreasApiService } from '@features/disenador-politicas/data/areas-api.service';
import { PoliticasApiService } from '@features/disenador-politicas/data/politicas-api.service';
import { UsuariosAreaApiService } from '@features/disenador-politicas/data/usuarios-area-api.service';
import type { AreaDto } from '@features/disenador-politicas/models/area.model';
import type { PoliticaNegocioDto } from '@features/disenador-politicas/models/politica-negocio.model';
import type { UsuarioAreaDto } from '@features/disenador-politicas/models/usuario-area.model';
import {
  bpmnXmlToPolicyUpsertBody,
  defaultBpmnXml,
  policyToBpmnXml,
  validateBpmnXml,
  type BpmnValidationIssue,
} from '@features/disenador-politicas/utils/bpmn-policy-adapter';
import {
  BpmnPropertiesPanelComponent,
  type BpmnBusinessProperties,
} from './bpmn-properties-panel.component';
import { BpmnToolbarComponent } from './bpmn-toolbar.component';
import { BpmnValidationPanelComponent } from './bpmn-validation-panel.component';

interface NodeBusinessMeta {
  areaId: string | null;
  carrilBpmn: string | null;
  formularioExternoUrl: string | null;
  asignacionesResponsable: { usuarioId: string; areaId: string; fechaAsignacion: string; estado: boolean }[];
}

@Component({
  selector: 'app-disenador-politicas-modelado',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    BpmnToolbarComponent,
    BpmnPropertiesPanelComponent,
    BpmnValidationPanelComponent,
  ],
  templateUrl: './disenador-politicas-modelado.component.html',
  styleUrl: './disenador-politicas-modelado.component.scss',
})
export class DisenadorPoliticasModeladoComponent implements AfterViewInit, OnDestroy {
  @ViewChild('bpmnCanvas', { static: true }) private readonly bpmnCanvas!: ElementRef<HTMLDivElement>;
  @ViewChild('xmlInput') private readonly xmlInput!: ElementRef<HTMLInputElement>;

  private readonly route = inject(ActivatedRoute);
  private readonly api = inject(PoliticasApiService);
  private readonly areasApi = inject(AreasApiService);
  private readonly usuariosAreaApi = inject(UsuariosAreaApiService);
  private readonly destroyRef = inject(DestroyRef);

  private modeler: BpmnModeler | null = null;
  private selectedElementShape: any | null = null;
  private selectedElement: any | null = null;
  private selectedElementName = '';
  private selectedElementType = '';

  politica: PoliticaNegocioDto | null = null;
  metaNombre = '';
  metaDescripcion = '';
  metaVersion = 1;
  metaEstado = 'BORRADOR';
  saving = false;
  bannerMsg = '';
  bannerKind: 'info' | 'error' = 'info';
  validationIssues: BpmnValidationIssue[] = [];

  areas: AreaDto[] = [];
  usuariosDelArea: UsuarioAreaDto[] = [];
  props: BpmnBusinessProperties | null = null;

  ngAfterViewInit(): void {
    this.areasApi.list().subscribe({
      next: (a) => {
        this.areas = a.filter((x) => x.estado).sort((x, y) => x.nombre.localeCompare(y.nombre, 'es'));
        if (this.modeler && this.areas.length) {
          this.ensureDepartmentLanes(this.areas.map((x) => x.nombre));
        }
      },
      error: () => (this.areas = []),
    });
    this.initModeler();
    this.route.queryParamMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((q) => {
      void this.loadPolitica(q.get('politicaId'));
    });
  }

  ngOnDestroy(): void {
    this.modeler?.destroy();
    this.modeler = null;
  }

  get selectedElementLabel(): string {
    if (!this.selectedElementType) return '';
    return this.selectedElementName ? `${this.selectedElementType} — ${this.selectedElementName}` : this.selectedElementType;
  }

  get isTaskLikeSelected(): boolean {
    if (!this.selectedElement) return false;
    return this.selectedElementType !== 'bpmn:SequenceFlow';
  }

  onSave(): void {
    if (!this.modeler || !this.politica || this.saving) return;
    this.saving = true;
    this.modeler
      .saveXML({ format: true })
      .then(({ xml }) => {
        const xmlText = xml ?? '';
        this.validationIssues = [];
        const draft = {
          ...this.politica!,
          nombre: this.metaNombre.trim(),
          descripcion: this.metaDescripcion.trim(),
          version: this.metaVersion,
          estado: this.metaEstado.trim(),
        };
        const body = bpmnXmlToPolicyUpsertBody(xmlText, draft);
        this.api.update(this.politica!.id, body).subscribe({
          next: (p) => {
            this.politica = p;
            this.patchMeta(p);
            this.saving = false;
            this.setBanner('Política BPMN guardada correctamente.', 'info');
          },
          error: (e) => {
            this.saving = false;
            this.setBanner(this.msg(e), 'error');
          },
        });
      })
      .catch((e) => {
        this.saving = false;
        this.setBanner(this.msg(e), 'error');
      });
  }

  onValidate(): void {
    if (!this.modeler) return;
    this.modeler
      .saveXML({ format: true })
      .then(({ xml }) => {
        this.validationIssues = validateBpmnXml(xml ?? '');
        if (!this.validationIssues.length) {
          this.setBanner('Validación BPMN base completada sin errores.', 'info');
        }
      })
      .catch((e) => {
        this.validationIssues = [{ level: 'error', message: this.msg(e) }];
      });
  }

  onNewDiagram(): void {
    this.importXml(defaultBpmnXml());
    this.setBanner('Se creó un diagrama BPMN base.', 'info');
  }

  onSyncLanesByDepartment(): void {
    if (!this.modeler) return;
    const activeAreas = this.areas.filter((a) => a.estado);
    if (!activeAreas.length) {
      this.setBanner('No hay departamentos activos para crear carriles.', 'error');
      return;
    }
    this.ensureDepartmentLanes(activeAreas.map((a) => a.nombre));
    this.setBanner('Carriles BPMN sincronizados por departamento.', 'info');
  }

  onImportRequest(): void {
    this.xmlInput?.nativeElement.click();
  }

  onFilePicked(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    file.text().then((xml) => this.importXml(xml));
    input.value = '';
  }

  onExportXml(): void {
    if (!this.modeler || !this.politica) return;
    this.modeler.saveXML({ format: true }).then(({ xml }) => {
      const blob = new Blob([xml ?? ''], { type: 'application/xml;charset=utf-8' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `politica-${this.politica!.id}.bpmn`;
      a.click();
      URL.revokeObjectURL(url);
    });
  }

  onPropsAreaChanged(areaId: string | null): void {
    if (!areaId) {
      this.usuariosDelArea = [];
      return;
    }
    this.usuariosAreaApi.listPorArea(areaId).subscribe({
      next: (u) => (this.usuariosDelArea = u),
      error: () => (this.usuariosDelArea = []),
    });
  }

  onApplyProps(props: BpmnBusinessProperties): void {
    if (!this.modeler || !this.selectedElement || !this.selectedElementShape) return;
    const modeling = this.modeler.get('modeling') as any;
    const moddle = this.modeler.get('moddle') as any;
    const bo = this.selectedElement as any;
    const areaSeleccionada = this.areas.find((a) => a.id === (props.areaId ?? '')) ?? null;
    const carrilDerivado = props.carrilBpmn?.trim() || areaSeleccionada?.nombre || null;

    const meta: NodeBusinessMeta = {
      areaId: props.areaId ?? null,
      carrilBpmn: carrilDerivado,
      formularioExternoUrl: props.formularioExternoUrl ?? null,
      asignacionesResponsable:
        props.areaId && props.responsableId
          ? [{ usuarioId: props.responsableId, areaId: props.areaId, fechaAsignacion: new Date().toISOString(), estado: true }]
          : [],
    };
    const docs = bo.documentation ?? [];
    const firstDoc = docs[0] ?? moddle.create('bpmn:Documentation', { text: '' });
    firstDoc.text = JSON.stringify(meta);
    modeling.updateProperties(this.selectedElement, {
      documentation: [firstDoc],
    });
    const normalizedProps: BpmnBusinessProperties = { ...props, carrilBpmn: carrilDerivado };
    this.props = normalizedProps;
    if (carrilDerivado) {
      this.ensureDepartmentLanes([carrilDerivado]);
      this.moveSelectedToLane(carrilDerivado);
    }
    this.setBanner('Propiedades de negocio aplicadas al elemento BPMN.', 'info');
  }

  private initModeler(): void {
    this.modeler = new BpmnModeler({
      container: this.bpmnCanvas.nativeElement,
    });
    const eventBus = this.modeler.get('eventBus') as any;
    eventBus.on('selection.changed', (e: { newSelection: any[] }) => {
      const el = e.newSelection?.[0] ?? null;
      this.selectedElementShape = el;
      this.selectedElement = el?.businessObject ?? null;
      this.selectedElementName = this.selectedElement?.name ?? '';
      this.selectedElementType = this.selectedElement?.$type ?? '';
      this.props = this.readCurrentProps();
    });
  }

  private readCurrentProps(): BpmnBusinessProperties | null {
    const bo = this.selectedElement as any;
    if (!bo) return null;
    const docs = bo.documentation ?? [];
    if (!docs.length || !docs[0]?.text) {
      return { areaId: null, responsableId: null, carrilBpmn: null, formularioExternoUrl: null };
    }
    try {
      const meta = JSON.parse(String(docs[0].text)) as NodeBusinessMeta;
      const firstAsignacion = (meta.asignacionesResponsable ?? [])[0];
      return {
        areaId: meta.areaId ?? null,
        responsableId: firstAsignacion?.usuarioId ?? null,
        carrilBpmn: meta.carrilBpmn ?? null,
        formularioExternoUrl: meta.formularioExternoUrl ?? null,
      };
    } catch {
      return { areaId: null, responsableId: null, carrilBpmn: null, formularioExternoUrl: null };
    }
  }

  private async loadPolitica(id: string | null): Promise<void> {
    if (!this.modeler) return;
    if (!id) {
      this.politica = null;
      this.metaNombre = '';
      this.metaDescripcion = '';
      this.metaVersion = 1;
      this.metaEstado = 'BORRADOR';
      void this.importXml(defaultBpmnXml());
      this.setBanner('Sin política seleccionada. Se muestra diagrama base BPMN.', 'info');
      return;
    }
    this.api.getById(id).subscribe({
      next: (p) => {
        this.politica = p;
        this.patchMeta(p);
        const preferredXml = policyToBpmnXml(p);
        this.importXml(preferredXml)
          .then((ok) => {
            if (ok) {
              this.setBanner('Política cargada en modelador BPMN.', 'info');
              return;
            }
            // Fallback defensivo: evita canvas en blanco si el XML persistido vino inconsistente.
            return this.importXml(defaultBpmnXml()).then(() => {
              this.setBanner(
                'La política tenía un XML BPMN inválido; se cargó un diagrama base para continuar editando.',
                'error',
              );
            });
          })
          .catch(() => {
            this.setBanner('No se pudo cargar el diagrama BPMN de la política.', 'error');
          });
      },
      error: (e) => {
        this.politica = null;
        void this.importXml(defaultBpmnXml());
        this.setBanner(this.msg(e), 'error');
      },
    });
  }

  private patchMeta(p: PoliticaNegocioDto): void {
    this.metaNombre = p.nombre;
    this.metaDescripcion = p.descripcion;
    this.metaVersion = p.version;
    this.metaEstado = p.estado;
  }

  private importXml(xml: string): Promise<boolean> {
    if (!this.modeler) return Promise.resolve(false);
    return this.modeler
      .importXML(xml)
      .then((result: { warnings?: unknown[] }) => {
        if (this.areas.length) {
          this.ensureDepartmentLanes(this.areas.filter((a) => a.estado).map((a) => a.nombre));
        }
        this.refreshViewport();
        const warnings = Array.isArray(result?.warnings) ? result.warnings : [];
        if (warnings.length) {
          const summary = warnings
            .map((w) => this.msg(w))
            .filter(Boolean)
            .slice(0, 2)
            .join(' | ');
          this.setBanner(`El BPMN cargó con advertencias: ${summary}`, 'error');
        }
        return true;
      })
      .catch((e) => {
        this.setBanner(`Error importando BPMN: ${this.msg(e)}`, 'error');
        return false;
      });
  }

  private refreshViewport(): void {
    if (!this.modeler) return;
    const canvas = this.modeler.get('canvas') as any;
    canvas.resized?.();
    canvas.zoom('fit-viewport');
    setTimeout(() => {
      try {
        canvas.resized?.();
        canvas.zoom('fit-viewport');
      } catch {
        // no-op defensivo
      }
    }, 120);
  }

  private ensureDepartmentLanes(departments: string[]): void {
    if (!this.modeler || !departments.length) return;
    if (!this.isDiagramReady()) return;
    const modeling = this.modeler.get('modeling') as any;
    const elementRegistry = this.modeler.get('elementRegistry') as any;
    try {
      let participant = this.firstParticipant(elementRegistry);
      if (!participant) {
        modeling.makeCollaboration();
        participant = this.firstParticipant(elementRegistry);
        if (!participant) return;
      }

      let lanes = this.findLanes(elementRegistry);
      if (!lanes.length) {
        modeling.addLane(participant, 'bottom');
        lanes = this.findLanes(elementRegistry);
      }
      while (lanes.length < departments.length) {
        modeling.addLane(lanes[lanes.length - 1] ?? participant, 'bottom');
        lanes = this.findLanes(elementRegistry);
      }
      lanes.slice(0, departments.length).forEach((lane: any, idx: number) => {
        modeling.updateLabel(lane, departments[idx]);
      });
    } catch {
      // Evita romper la UI si el modelador todavía está resolviendo el árbol interno.
    }
  }

  private moveSelectedToLane(laneName: string): void {
    if (!this.modeler || !this.selectedElementShape) return;
    const modeling = this.modeler.get('modeling') as any;
    const elementRegistry = this.modeler.get('elementRegistry') as any;
    const lane = this.findLanes(elementRegistry).find((x: any) => x.businessObject?.name === laneName);
    if (!lane) return;
    const shape = this.selectedElementShape;
    if (!shape || shape.waypoints) return;

    const nextX = lane.x + 90;
    const nextY = lane.y + Math.max(50, (lane.height - shape.height) / 2);
    const delta = { x: nextX - shape.x, y: nextY - shape.y };
    modeling.moveElements([shape], delta, lane);
  }

  private firstParticipant(elementRegistry: any): any | null {
    return elementRegistry.filter((el: any) => el.type === 'bpmn:Participant')[0] ?? null;
  }

  private findLanes(elementRegistry: any): any[] {
    return elementRegistry.filter((el: any) => el.type === 'bpmn:Lane');
  }

  private isDiagramReady(): boolean {
    if (!this.modeler) return false;
    try {
      const definitions = (this.modeler as any).getDefinitions?.();
      return Array.isArray(definitions?.rootElements) && definitions.rootElements.length > 0;
    } catch {
      return false;
    }
  }

  private setBanner(text: string, kind: 'info' | 'error'): void {
    this.bannerMsg = text;
    this.bannerKind = kind;
  }

  private msg(err: unknown): string {
    if (err instanceof HttpErrorResponse) {
      return typeof err.error?.message === 'string' ? err.error.message : err.message;
    }
    if (typeof err === 'object' && err && 'message' in err) {
      return String((err as any).message);
    }
    return 'Error de red o servidor.';
  }
}
