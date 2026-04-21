import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import {
  AfterViewInit,
  Component,
  DestroyRef,
  ElementRef,
  HostListener,
  NgZone,
  OnDestroy,
  ViewChild,
  inject,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Graph, Shape, type Edge, type Node } from '@antv/x6';
import { Keyboard } from '@antv/x6-plugin-keyboard';
import { Selection } from '@antv/x6-plugin-selection';
import '@antv/x6-plugin-selection/es/api';
import '@antv/x6-plugin-keyboard/es/api';
import { map, distinctUntilChanged } from 'rxjs/operators';
import { AreasApiService } from '@features/disenador-politicas/data/areas-api.service';
import { PoliticasApiService } from '@features/disenador-politicas/data/politicas-api.service';
import { UsuariosAreaApiService } from '@features/disenador-politicas/data/usuarios-area-api.service';
import type { AreaDto } from '@features/disenador-politicas/models/area.model';
import {
  politicaDtoToUpsertBody,
  type PoliticaAsignacionDto,
  type PoliticaNegocioDto,
} from '@features/disenador-politicas/models/politica-negocio.model';
import type { UsuarioAreaDto } from '@features/disenador-politicas/models/usuario-area.model';
import {
  buildCellsFromPolitica,
  createBlankNodeCell,
  createPoliticaEdgeBetween,
  graphToPoliticaNegocio,
  newConexionId,
  newNodoId,
  type PoliticaEdgeCellData,
  type PoliticaMetaCabecera,
  type PoliticaNodoCellData,
} from '@features/disenador-politicas/utils/politica-x6-mapper';

const TIPOS_FLUJO = ['SECUENCIAL', 'ALTERNATIVO', 'PARALELO', 'MULTILINEAL'] as const;

type ToolMode = 'select' | 'connect';

@Component({
  selector: 'app-disenador-politicas-modelado',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './disenador-politicas-modelado.component.html',
  styleUrl: './disenador-politicas-modelado.component.scss',
})
export class DisenadorPoliticasModeladoComponent implements AfterViewInit, OnDestroy {
  @ViewChild('graphHost', { static: true }) private readonly graphHost!: ElementRef<HTMLDivElement>;

  private readonly route = inject(ActivatedRoute);
  private readonly api = inject(PoliticasApiService);
  private readonly areasApi = inject(AreasApiService);
  private readonly usuariosAreaApi = inject(UsuariosAreaApiService);
  private readonly zone = inject(NgZone);
  private readonly destroyRef = inject(DestroyRef);

  private graph: Graph | null = null;

  politica: PoliticaNegocioDto | null = null;
  metaNombre = '';
  metaDescripcion = '';
  metaVersion = 1;
  metaEstado = 'BORRADOR';
  savingMeta = false;
  savingGrafo = false;

  areas: AreaDto[] = [];
  usuariosDelArea: UsuarioAreaDto[] = [];

  selectedNode: Node | null = null;
  selectedEdge: Edge | null = null;

  inspNodoNombre = '';
  inspNodoAreaId = '';
  inspNodoResponsableId = '';

  inspEdgeTipoFlujo: (typeof TIPOS_FLUJO)[number] = 'SECUENCIAL';
  inspEdgeCondicion = '';

  bannerMsg = '';
  bannerKind: 'info' | 'error' = 'info';

  readonly tiposFlujo = TIPOS_FLUJO;

  toolMode: ToolMode = 'select';
  /** En modo «Conectar»: id del nodo origen esperando destino. */
  private pendingConnectSourceId: string | null = null;

  constructor() {
    this.route.queryParamMap
      .pipe(
        map((q) => q.get('politicaId')),
        distinctUntilChanged(),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((id) => {
        if (!this.graph) {
          return;
        }
        void this.syncPoliticaId(id);
      });
  }

  ngAfterViewInit(): void {
    this.areasApi.list().subscribe({
      next: (a) =>
        (this.areas = a.filter((x) => x.estado).sort((x, y) => x.nombre.localeCompare(y.nombre, 'es'))),
      error: () => (this.areas = []),
    });
    this.initGraph();
    void this.syncPoliticaId(this.route.snapshot.queryParamMap.get('politicaId'));
  }

  ngOnDestroy(): void {
    this.disposeGraph();
  }

  @HostListener('window:resize')
  onWinResize(): void {
    this.resizeGraph();
  }

  @HostListener('document:keydown.escape')
  onEscapeGlobal(): void {
    this.clearPendingConnect();
    if (this.toolMode === 'connect') {
      this.toolMode = 'select';
      this.setBanner('Modo conectar cancelado.', 'info');
    }
  }

  setToolSelect(): void {
    this.toolMode = 'select';
    this.clearPendingConnect();
  }

  setToolConnect(): void {
    this.toolMode = 'connect';
    this.clearPendingConnect();
    this.setBanner(
      'Modo conectar: 1) clic en el nodo origen (ej. atención al cliente), 2) clic en cada destino (legal, otro depto…). Podés repetir desde el mismo origen. ESC vuelve a selección.',
      'info',
    );
  }

  zoomIn(): void {
    this.graph?.zoom(0.18);
  }

  zoomOut(): void {
    this.graph?.zoom(-0.18);
  }

  zoomFit(): void {
    this.graph?.zoomToFit({ padding: 24, maxScale: 1.2 });
  }

  guardarMetadatos(): void {
    if (!this.politica || this.savingMeta) {
      return;
    }
    const merged: PoliticaNegocioDto = {
      ...this.politica,
      nombre: this.metaNombre.trim(),
      descripcion: this.metaDescripcion.trim(),
      version: this.metaVersion,
      estado: this.metaEstado.trim(),
    };
    const body = politicaDtoToUpsertBody(merged);
    this.savingMeta = true;
    this.api.update(this.politica.id, body).subscribe({
      next: (p) => {
        this.politica = p;
        this.patchMetaFromPolitica(p);
        this.savingMeta = false;
        this.setBanner('Metadatos guardados.', 'info');
      },
      error: (e) => {
        this.savingMeta = false;
        this.setBanner(this.msg(e), 'error');
      },
    });
  }

  guardarGrafo(): void {
    if (!this.politica || !this.graph || this.savingGrafo) {
      return;
    }
    const meta: PoliticaMetaCabecera = {
      nombre: this.metaNombre,
      descripcion: this.metaDescripcion,
      version: this.metaVersion,
      estado: this.metaEstado,
    };
    const draft = graphToPoliticaNegocio(this.graph, this.politica, meta);
    this.savingGrafo = true;
    this.api.update(this.politica.id, politicaDtoToUpsertBody(draft)).subscribe({
      next: (p) => {
        this.politica = p;
        this.patchMetaFromPolitica(p);
        this.savingGrafo = false;
        this.setBanner(
          'Grafo y asignaciones guardados. La vista no se resetea: se conservan posición de nodos, zoom y desplazamiento.',
          'info',
        );
      },
      error: (e) => {
        this.savingGrafo = false;
        this.setBanner(this.msg(e), 'error');
      },
    });
  }

  agregarNodo(tipo: string): void {
    if (!this.graph || !this.politica) {
      this.setBanner('Abrí una política desde el catálogo para editar el grafo.', 'error');
      return;
    }
    if (tipo === 'INICIO') {
      const ya = this.graph.getNodes().some((n) => Boolean((n.getData() as PoliticaNodoCellData | undefined)?.esInicial));
      if (ya) {
        this.setBanner('Solo puede existir un nodo de inicio (validación del dominio).', 'error');
        return;
      }
    }
    const id = newNodoId('n');
    const count = this.graph.getNodes().length;
    const y = 60 + count * 95;
    const x = 200;
    this.graph.addNode(createBlankNodeCell(tipo, id, x, y));
    this.setBanner(
      `Nodo «${tipo}» agregado. Arrastrá desde un círculo morado (salida abajo / entrada arriba) o usá la herramienta «Conectar» en la caja de la izquierda.`,
      'info',
    );
  }

  aplicarInspectorNodo(): void {
    const node = this.selectedNode;
    if (!node) {
      return;
    }
    const nombre = this.inspNodoNombre.trim();
    const areaId = this.inspNodoAreaId.trim() || null;
    const respId = this.inspNodoResponsableId.trim();
    let asignaciones: PoliticaAsignacionDto[] = [];
    if (areaId && respId) {
      asignaciones = [
        {
          usuarioId: respId,
          areaId,
          fechaAsignacion: new Date().toISOString(),
          estado: true,
        },
      ];
    }
    const prev = node.getData() as PoliticaNodoCellData;
    const next: PoliticaNodoCellData = {
      ...prev,
      nombre: nombre || prev.idNodo,
      areaId,
      asignacionesResponsable: asignaciones,
    };
    node.setData(next);
    node.attr('label/text', next.nombre);
    this.setBanner('Cambios aplicados al nodo (usá «Guardar grafo» para persistir).', 'info');
  }

  aplicarInspectorArista(): void {
    const edge = this.selectedEdge;
    if (!edge) {
      return;
    }
    const d: PoliticaEdgeCellData = {
      idConexion: (edge.getData() as PoliticaEdgeCellData)?.idConexion ?? edge.id,
      tipoFlujo: this.inspEdgeTipoFlujo,
      condicion: this.inspEdgeCondicion.trim() || null,
    };
    edge.setData(d);
    this.setBanner('Cambios aplicados a la conexión (usá «Guardar grafo» para persistir).', 'info');
  }

  onCambioAreaNodo(): void {
    this.inspNodoResponsableId = '';
    this.cargarUsuariosArea(this.inspNodoAreaId.trim());
  }

  reiniciarVista(): void {
    const id = this.route.snapshot.queryParamMap.get('politicaId');
    void this.syncPoliticaId(id);
  }

  private cargarUsuariosArea(areaId: string): void {
    if (!areaId) {
      this.usuariosDelArea = [];
      return;
    }
    this.usuariosAreaApi.listPorArea(areaId).subscribe({
      next: (u) => (this.usuariosDelArea = u),
      error: () => (this.usuariosDelArea = []),
    });
  }

  private initGraph(): void {
    const el = this.graphHost.nativeElement;
    const w = Math.max(320, el.clientWidth || 640);
    const h = Math.max(400, el.clientHeight || 560);
    this.graph = new Graph({
      container: el,
      width: w,
      height: h,
      grid: true,
      mousewheel: { enabled: true, modifiers: ['ctrl', 'meta'] },
      panning: { enabled: true, eventTypes: ['rightMouseDown'] },
      connecting: {
        snap: { radius: 28 },
        allowBlank: false,
        allowLoop: false,
        highlight: true,
        router: { name: 'manhattan' },
        connector: { name: 'rounded' },
        validateConnection({ sourceCell, targetCell }) {
          if (!sourceCell || !targetCell) {
            return false;
          }
          return sourceCell.id !== targetCell.id;
        },
        createEdge: () =>
          new Shape.Edge({
            attrs: {
              line: {
                stroke: '#475569',
                strokeWidth: 2,
                targetMarker: { name: 'classic', size: 8 },
              },
            },
            data: { idConexion: newConexionId(), tipoFlujo: 'SECUENCIAL', condicion: null } satisfies PoliticaEdgeCellData,
          }),
      },
    });

    this.graph.use(
      new Selection({
        enabled: true,
        multiple: true,
        rubberband: true,
        strict: false,
        showNodeSelectionBox: true,
      }),
    );
    this.graph.use(new Keyboard({ enabled: true, global: true }));

    this.graph.bindKey(['backspace', 'delete'], () => {
      const cells = this.graph?.getSelectedCells() ?? [];
      if (cells.length) {
        this.graph?.removeCells(cells);
        this.clearSelection();
      }
    });

    this.graph.on('edge:connected', ({ edge }) => {
      this.zone.run(() => {
        const d = edge.getData() as PoliticaEdgeCellData | undefined;
        if (!d?.idConexion) {
          edge.setData({
            idConexion: newConexionId(),
            tipoFlujo: 'SECUENCIAL',
            condicion: null,
          } satisfies PoliticaEdgeCellData);
        }
      });
    });

    this.graph.on('blank:click', () => {
      this.zone.run(() => {
        this.clearPendingConnect();
        this.clearSelection();
      });
    });

    this.graph.on('node:click', ({ e, node }) => {
      e.preventDefault();
      e.stopPropagation();
      this.zone.run(() => {
        if (this.toolMode === 'connect') {
          this.handleNodeClickConnect(node);
        } else {
          this.selectNode(node);
        }
      });
    });

    this.graph.on('edge:click', ({ e, edge }) => {
      e.preventDefault();
      e.stopPropagation();
      this.zone.run(() => this.selectEdge(edge));
    });
  }

  private selectNode(node: Node): void {
    this.selectedEdge = null;
    this.graph?.cleanSelection();
    this.graph?.resetSelection(node);
    this.selectedNode = node;
    const d = node.getData() as PoliticaNodoCellData;
    this.inspNodoNombre = d.nombre;
    this.inspNodoAreaId = d.areaId ?? '';
    const first = d.asignacionesResponsable?.[0];
    this.inspNodoResponsableId = first?.usuarioId ?? '';
    this.cargarUsuariosArea(this.inspNodoAreaId);
  }

  private selectEdge(edge: Edge): void {
    this.selectedNode = null;
    this.graph?.cleanSelection();
    this.graph?.resetSelection(edge);
    this.selectedEdge = edge;
    const d = edge.getData() as PoliticaEdgeCellData | undefined;
    this.inspEdgeTipoFlujo = (d?.tipoFlujo as (typeof TIPOS_FLUJO)[number]) ?? 'SECUENCIAL';
    this.inspEdgeCondicion = d?.condicion ?? '';
  }

  private clearSelection(): void {
    this.graph?.cleanSelection();
    this.selectedNode = null;
    this.selectedEdge = null;
    this.usuariosDelArea = [];
  }

  private clearPendingConnect(): void {
    this.pendingConnectSourceId = null;
  }

  private handleNodeClickConnect(node: Node): void {
    if (!this.graph) {
      return;
    }
    const id = node.id;
    if (!this.pendingConnectSourceId) {
      this.pendingConnectSourceId = id;
      this.setBanner('Origen elegido. Hacé clic en el nodo destino (podés crear varias ramas al mismo origen).', 'info');
      return;
    }
    if (this.pendingConnectSourceId === id) {
      this.clearPendingConnect();
      this.setBanner('Origen cancelado. Elegí de nuevo el primer nodo.', 'info');
      return;
    }
    this.graph.addEdge(createPoliticaEdgeBetween(this.pendingConnectSourceId, id));
    this.clearPendingConnect();
    this.setBanner(
      'Flecha creada. Si es bifurcación paralela, seleccioná la flecha y en el inspector poné tipo de flujo PARALELO. Podés enlazar de nuevo desde el mismo origen.',
      'info',
    );
  }

  private resizeGraph(): void {
    if (!this.graph) {
      return;
    }
    const el = this.graphHost.nativeElement;
    const w = Math.max(320, el.clientWidth);
    const h = Math.max(400, el.clientHeight);
    this.graph.resize(w, h);
  }

  private disposeGraph(): void {
    this.graph?.dispose();
    this.graph = null;
  }

  private patchMetaFromPolitica(p: PoliticaNegocioDto): void {
    this.metaNombre = p.nombre;
    this.metaDescripcion = p.descripcion;
    this.metaVersion = p.version;
    this.metaEstado = p.estado;
  }

  private async syncPoliticaId(id: string | null): Promise<void> {
    if (!this.graph) {
      return;
    }
    this.clearPendingConnect();
    this.clearSelection();
    if (!id) {
      this.politica = null;
      this.metaNombre = '';
      this.metaDescripcion = '';
      this.metaVersion = 1;
      this.metaEstado = 'BORRADOR';
      this.graph.clearCells();
      this.setBanner('Sin política seleccionada. Abrí una desde el catálogo para modelar nodos y departamentos.', 'info');
      return;
    }

    this.setBanner('Cargando política…', 'info');
    this.api.getById(id).subscribe({
      next: (p) => {
        this.politica = p;
        this.patchMetaFromPolitica(p);
        this.graph!.clearCells();
        this.graph!.fromJSON({ cells: buildCellsFromPolitica(p) });
        this.resizeGraph();
        this.graph!.centerContent();
        this.setBanner(
          'Política cargada en el editor X6. Cada nodo puede tener areaId y asignacionesResponsable (encargado). Guardá el grafo para persistir en Mongo.',
          'info',
        );
      },
      error: (e) => {
        this.politica = null;
        this.graph!.clearCells();
        this.setBanner(this.msg(e), 'error');
      },
    });
  }

  private setBanner(text: string, kind: 'info' | 'error'): void {
    this.bannerMsg = text;
    this.bannerKind = kind;
  }

  private msg(err: unknown): string {
    if (err instanceof HttpErrorResponse) {
      const b = err.error as { message?: string } | null;
      if (b && typeof b.message === 'string') {
        return b.message;
      }
      return err.message;
    }
    return 'Error de red o servidor.';
  }
}
