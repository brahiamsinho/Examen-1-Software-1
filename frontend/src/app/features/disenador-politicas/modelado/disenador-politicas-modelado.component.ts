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
import { History } from '@antv/x6-plugin-history';
import { Keyboard } from '@antv/x6-plugin-keyboard';
import { Selection } from '@antv/x6-plugin-selection';
import '@antv/x6-plugin-history/es/api';
import '@antv/x6-plugin-selection/es/api';
import '@antv/x6-plugin-keyboard/es/api';
import { map, distinctUntilChanged } from 'rxjs/operators';
import { AuthService } from '@core/auth/auth.service';
import { AreasApiService } from '@features/disenador-politicas/data/areas-api.service';
import {
  PoliticasCollaborationService,
  type PoliticaCollabInbound,
  type PoliticaCollabPeer,
} from '@features/disenador-politicas/data/politicas-collaboration.service';
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

function collabColorForSession(sessionId: string): string {
  let h = 0;
  for (let i = 0; i < sessionId.length; i += 1) {
    h = (h * 31 + sessionId.charCodeAt(i)) >>> 0;
  }
  return `hsl(${h % 360} 72% 42%)`;
}

export interface RemotePointerUi {
  sessionId: string;
  displayName: string;
  px: number;
  py: number;
  color: string;
  selectionSummary: string;
}

@Component({
  selector: 'app-disenador-politicas-modelado',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './disenador-politicas-modelado.component.html',
  styleUrl: './disenador-politicas-modelado.component.scss',
})
export class DisenadorPoliticasModeladoComponent implements AfterViewInit, OnDestroy {
  @ViewChild('graphWrap', { static: true }) private readonly graphWrap!: ElementRef<HTMLDivElement>;
  @ViewChild('graphHost', { static: true }) private readonly graphHost!: ElementRef<HTMLDivElement>;

  private readonly route = inject(ActivatedRoute);
  private readonly api = inject(PoliticasApiService);
  private readonly areasApi = inject(AreasApiService);
  private readonly usuariosAreaApi = inject(UsuariosAreaApiService);
  private readonly collab = inject(PoliticasCollaborationService);
  private readonly auth = inject(AuthService);
  private readonly zone = inject(NgZone);
  private readonly destroyRef = inject(DestroyRef);

  private graph: Graph | null = null;

  /** Estado del plugin History (Deshacer / Rehacer). */
  canUndo = false;
  canRedo = false;

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

  /** Otros usuarios en la misma sala WebSocket (presencia). */
  collabPeers: PoliticaCollabPeer[] = [];

  /** Punteros remotos (posición ya en px relativos al lienzo). */
  remotePointerList: RemotePointerUi[] = [];

  readonly tiposFlujo = TIPOS_FLUJO;

  private applyingRemote = false;
  private lastRemoteRevision = 0;
  private graphCollabTimer: ReturnType<typeof setTimeout> | null = null;

  private readonly remotePointerBySession = new Map<
    string,
    { displayName: string; gx: number; gy: number; selectedIds: string[]; visible: boolean }
  >();
  private readonly remoteHighlightEls = new Map<string, HTMLElement[]>();

  private lastPointerClientX = 0;
  private lastPointerClientY = 0;
  private pointerRaf: number | null = null;

  private readonly onGraphWrapPointerMove = (e: PointerEvent): void => {
    this.lastPointerClientX = e.clientX;
    this.lastPointerClientY = e.clientY;
    this.scheduleLocalPointerSend();
  };

  private readonly onGraphWrapPointerLeave = (): void => {
    if (!this.auth.isDisenadorPoliticas() && !this.auth.isAdministrador()) {
      return;
    }
    this.collab.pushPointer(0, 0, [], false);
  };

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

    this.collab.inbound$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((msg) => this.zone.run(() => this.onCollabInbound(msg)));
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
    if (this.graphCollabTimer != null) {
      clearTimeout(this.graphCollabTimer);
      this.graphCollabTimer = null;
    }
    if (this.pointerRaf != null) {
      cancelAnimationFrame(this.pointerRaf);
      this.pointerRaf = null;
    }
    this.collab.leavePolitica();
    this.collab.disconnect();
    this.disposeGraph();
  }

  @HostListener('window:resize')
  onWinResize(): void {
    this.resizeGraph();
    this.rebuildRemotePointerList();
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

  undoGrafo(): void {
    if (!this.graph?.canUndo()) {
      return;
    }
    this.graph.undo();
  }

  redoGrafo(): void {
    if (!this.graph?.canRedo()) {
      return;
    }
    this.graph.redo();
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
    this.graph.use(
      new History({
        enabled: true,
        /** 0 = ilimitado; con políticas medianas basta; evita crecer sin tope. */
        stackSize: 300,
      }),
    );
    this.graph.use(new Keyboard({ enabled: true, global: true }));

    this.graph.on('history:change', () => {
      this.zone.run(() => this.syncHistoryButtonState());
    });

    this.graph.bindKey(['backspace', 'delete'], () => {
      const cells = this.graph?.getSelectedCells() ?? [];
      if (cells.length) {
        this.graph?.removeCells(cells);
        this.clearSelection();
      }
    });

    this.graph.bindKey(['meta+z', 'ctrl+z'], (e) => {
      if (this.isTextInputFocused()) {
        return;
      }
      e.preventDefault();
      if (this.graph?.canUndo()) {
        this.graph.undo();
      }
    });
    this.graph.bindKey(['meta+shift+z', 'ctrl+shift+z', 'meta+y', 'ctrl+y'], (e) => {
      if (this.isTextInputFocused()) {
        return;
      }
      e.preventDefault();
      if (this.graph?.canRedo()) {
        this.graph.redo();
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

    this.graph.on('blank:click', ({ e }) => {
      this.zone.run(() => {
        this.clearPendingConnect();
        this.clearSelection({ x: e.clientX, y: e.clientY });
      });
    });

    this.graph.on('node:click', ({ e, node }) => {
      e.preventDefault();
      e.stopPropagation();
      this.zone.run(() => {
        if (this.toolMode === 'connect') {
          this.handleNodeClickConnect(node, e.clientX, e.clientY);
        } else {
          this.selectNode(node, { x: e.clientX, y: e.clientY });
        }
      });
    });

    this.graph.on('edge:click', ({ e, edge }) => {
      e.preventDefault();
      e.stopPropagation();
      this.zone.run(() => this.selectEdge(edge, { x: e.clientX, y: e.clientY }));
    });

    this.wireCollabGraphEvents();
    this.wirePointerAndRemoteLayer();
    this.syncHistoryButtonState();
  }

  private readonly onGraphTransformForRemote = (): void => {
    this.zone.run(() => this.rebuildRemotePointerList());
  };

  private wirePointerAndRemoteLayer(): void {
    if (!this.graph) {
      return;
    }
    const wrap = this.graphWrap.nativeElement;
    wrap.addEventListener('pointermove', this.onGraphWrapPointerMove, { passive: true });
    wrap.addEventListener('pointerleave', this.onGraphWrapPointerLeave);
    this.graph.on('scale', this.onGraphTransformForRemote);
    this.graph.on('translate', this.onGraphTransformForRemote);
  }

  private wireCollabGraphEvents(): void {
    if (!this.graph) {
      return;
    }
    const schedule = (): void => this.scheduleGraphCollabPush();
    this.graph.on('cell:added', schedule);
    this.graph.on('cell:removed', schedule);
    this.graph.on('cell:change:*', schedule);
  }

  private scheduleGraphCollabPush(): void {
    if (this.applyingRemote || !this.graph || !this.politica) {
      return;
    }
    if (!this.auth.isDisenadorPoliticas() && !this.auth.isAdministrador()) {
      return;
    }
    if (this.graphCollabTimer != null) {
      clearTimeout(this.graphCollabTimer);
    }
    this.graphCollabTimer = setTimeout(() => {
      this.graphCollabTimer = null;
      if (this.applyingRemote || !this.graph || !this.politica) {
        return;
      }
      const raw = this.graph.toJSON() as { cells?: unknown[] };
      this.collab.pushGraphCells(raw.cells ?? []);
    }, 260);
  }

  private onCollabInbound(msg: PoliticaCollabInbound): void {
    if (msg.type === 'ERROR') {
      this.setBanner(msg.message, 'error');
      return;
    }
    if (msg.type === 'PRESENCE_SYNC') {
      const me = this.collab.getLocalSessionId();
      this.collabPeers = me ? msg.peers.filter((p) => p.sessionId !== me) : msg.peers;
      return;
    }
    if (msg.type === 'POINTER') {
      if (!this.graph || !this.politica || msg.politicaId !== this.politica.id) {
        return;
      }
      const me = this.collab.getLocalSessionId();
      if (me && msg.sourceSessionId === me) {
        return;
      }
      if (!msg.visible) {
        this.remotePointerBySession.delete(msg.sourceSessionId);
        this.clearRemoteHighlights(msg.sourceSessionId);
        this.rebuildRemotePointerList();
        return;
      }
      if (msg.gx == null || msg.gy == null) {
        return;
      }
      this.remotePointerBySession.set(msg.sourceSessionId, {
        displayName: msg.displayName,
        gx: msg.gx,
        gy: msg.gy,
        selectedIds: msg.selectedIds ?? [],
        visible: true,
      });
      this.rebuildRemotePointerList();
      this.applyRemoteHighlights(msg.sourceSessionId, msg.selectedIds ?? [], collabColorForSession(msg.sourceSessionId));
      return;
    }
    if (msg.type === 'GRAPH_UPDATE') {
      if (!this.graph || !this.politica || msg.politicaId !== this.politica.id) {
        return;
      }
      if (msg.revision <= this.lastRemoteRevision) {
        return;
      }
      this.applyingRemote = true;
      this.lastRemoteRevision = msg.revision;
      try {
        this.applyRemoteGraphCells(msg.cells);
        this.resizeGraph();
      } finally {
        this.applyingRemote = false;
      }
      this.refreshRemoteHighlightsAfterGraphChange();
      return;
    }
  }

  private selectNode(node: Node, client?: { x: number; y: number }): void {
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
    if (client) {
      this.lastPointerClientX = client.x;
      this.lastPointerClientY = client.y;
      this.flushLocalPointer(client.x, client.y);
    }
  }

  private selectEdge(edge: Edge, client?: { x: number; y: number }): void {
    this.selectedNode = null;
    this.graph?.cleanSelection();
    this.graph?.resetSelection(edge);
    this.selectedEdge = edge;
    const d = edge.getData() as PoliticaEdgeCellData | undefined;
    this.inspEdgeTipoFlujo = (d?.tipoFlujo as (typeof TIPOS_FLUJO)[number]) ?? 'SECUENCIAL';
    this.inspEdgeCondicion = d?.condicion ?? '';
    if (client) {
      this.lastPointerClientX = client.x;
      this.lastPointerClientY = client.y;
      this.flushLocalPointer(client.x, client.y);
    }
  }

  private clearSelection(client?: { x: number; y: number }): void {
    this.graph?.cleanSelection();
    this.selectedNode = null;
    this.selectedEdge = null;
    this.usuariosDelArea = [];
    if (client) {
      this.lastPointerClientX = client.x;
      this.lastPointerClientY = client.y;
      this.flushLocalPointer(client.x, client.y);
    }
  }

  private clearPendingConnect(): void {
    this.pendingConnectSourceId = null;
  }

  private handleNodeClickConnect(node: Node, clientX: number, clientY: number): void {
    if (!this.graph) {
      return;
    }
    const id = node.id;
    if (!this.pendingConnectSourceId) {
      this.pendingConnectSourceId = id;
      this.lastPointerClientX = clientX;
      this.lastPointerClientY = clientY;
      this.flushLocalPointer(clientX, clientY);
      this.setBanner('Origen elegido. Hacé clic en el nodo destino (podés crear varias ramas al mismo origen).', 'info');
      return;
    }
    if (this.pendingConnectSourceId === id) {
      this.clearPendingConnect();
      this.lastPointerClientX = clientX;
      this.lastPointerClientY = clientY;
      this.flushLocalPointer(clientX, clientY);
      this.setBanner('Origen cancelado. Elegí de nuevo el primer nodo.', 'info');
      return;
    }
    this.graph.addEdge(createPoliticaEdgeBetween(this.pendingConnectSourceId, id));
    this.clearPendingConnect();
    this.lastPointerClientX = clientX;
    this.lastPointerClientY = clientY;
    this.flushLocalPointer(clientX, clientY);
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
    this.rebuildRemotePointerList();
  }

  private disposeGraph(): void {
    if (this.pointerRaf != null) {
      cancelAnimationFrame(this.pointerRaf);
      this.pointerRaf = null;
    }
    const wrap = this.graphWrap?.nativeElement;
    if (wrap) {
      wrap.removeEventListener('pointermove', this.onGraphWrapPointerMove);
      wrap.removeEventListener('pointerleave', this.onGraphWrapPointerLeave);
    }
    if (this.graph) {
      this.graph.off('scale', this.onGraphTransformForRemote);
      this.graph.off('translate', this.onGraphTransformForRemote);
    }
    this.clearAllRemotePointerUi();
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
      this.clearAllRemotePointerUi();
      this.collab.leavePolitica();
      this.collabPeers = [];
      this.lastRemoteRevision = 0;
      this.politica = null;
      this.metaNombre = '';
      this.metaDescripcion = '';
      this.metaVersion = 1;
      this.metaEstado = 'BORRADOR';
      this.clearGraphAndHistory();
      this.setBanner('Sin política seleccionada. Abrí una desde el catálogo para modelar nodos y departamentos.', 'info');
      return;
    }

    this.setBanner('Cargando política…', 'info');
    this.clearAllRemotePointerUi();
    this.api.getById(id).subscribe({
      next: (p) => {
        this.politica = p;
        this.patchMetaFromPolitica(p);
        this.lastRemoteRevision = 0;
        this.replaceEntireGraphFromJsonCells(buildCellsFromPolitica(p) as unknown[]);
        this.resizeGraph();
        this.graph!.centerContent();
        if (this.auth.isDisenadorPoliticas() || this.auth.isAdministrador()) {
          this.collab.joinPolitica(p.id);
        } else {
          this.collab.leavePolitica();
          this.collabPeers = [];
        }
        this.setBanner(
          'Política cargada en el editor X6. Cada nodo puede tener areaId y asignacionesResponsable (encargado). Guardá el grafo para persistir en Mongo.',
          'info',
        );
      },
      error: (e) => {
        this.clearAllRemotePointerUi();
        this.collab.leavePolitica();
        this.collabPeers = [];
        this.politica = null;
        this.clearGraphAndHistory();
        this.setBanner(this.msg(e), 'error');
      },
    });
  }

  private setBanner(text: string, kind: 'info' | 'error'): void {
    this.bannerMsg = text;
    this.bannerKind = kind;
  }

  private isTextInputFocused(): boolean {
    const el = document.activeElement;
    if (!el) {
      return false;
    }
    if (el instanceof HTMLInputElement || el instanceof HTMLTextAreaElement) {
      return true;
    }
    if (el instanceof HTMLSelectElement) {
      return true;
    }
    return el.getAttribute('contenteditable') === 'true';
  }

  private syncHistoryButtonState(): void {
    if (!this.graph) {
      this.canUndo = false;
      this.canRedo = false;
      return;
    }
    this.canUndo = this.graph.canUndo();
    this.canRedo = this.graph.canRedo();
  }

  /** Carga/reset de grafo sin dejar en el historial un “deshacer” a estado vacío o añadir pasos. */
  private clearGraphAndHistory(): void {
    if (!this.graph) {
      return;
    }
    this.graph.disableHistory();
    try {
      this.graph.clearCells();
    } finally {
      this.graph.enableHistory();
      this.graph.cleanHistory();
    }
    this.syncHistoryButtonState();
  }

  private replaceEntireGraphFromJsonCells(cells: unknown[]): void {
    if (!this.graph) {
      return;
    }
    this.graph.disableHistory();
    try {
      this.graph.clearCells();
      this.graph.fromJSON({ cells: cells as never });
    } finally {
      this.graph.enableHistory();
      this.graph.cleanHistory();
    }
    this.syncHistoryButtonState();
  }

  private applyRemoteGraphCells(cells: unknown[]): void {
    if (!this.graph) {
      return;
    }
    this.graph.disableHistory();
    try {
      this.graph.fromJSON({ cells: cells as never });
    } finally {
      this.graph.enableHistory();
      this.graph.cleanHistory();
    }
    this.syncHistoryButtonState();
  }

  private scheduleLocalPointerSend(): void {
    if (!this.graph || !this.politica || this.applyingRemote) {
      return;
    }
    if (!this.auth.isDisenadorPoliticas() && !this.auth.isAdministrador()) {
      return;
    }
    if (this.pointerRaf != null) {
      return;
    }
    this.pointerRaf = requestAnimationFrame(() => {
      this.pointerRaf = null;
      this.flushLocalPointer(this.lastPointerClientX, this.lastPointerClientY);
    });
  }

  private flushLocalPointer(clientX: number, clientY: number): void {
    if (!this.graph || !this.politica || this.applyingRemote) {
      return;
    }
    if (!this.auth.isDisenadorPoliticas() && !this.auth.isAdministrador()) {
      return;
    }
    const g = this.graph as unknown as {
      clientToGraph(x: number, y: number): { x: number; y: number };
    };
    const gp = g.clientToGraph(clientX, clientY);
    this.collab.pushPointer(gp.x, gp.y, this.getSelectedCellIds(), true);
  }

  private getSelectedCellIds(): string[] {
    if (!this.graph) {
      return [];
    }
    const g = this.graph as unknown as { getSelectedCells(): { id: string }[] };
    try {
      return g.getSelectedCells().map((c) => c.id);
    } catch {
      return [];
    }
  }

  private rebuildRemotePointerList(): void {
    if (!this.graph) {
      this.remotePointerList = [];
      return;
    }
    const wrap = this.graphWrap.nativeElement;
    const wr = wrap.getBoundingClientRect();
    const g = this.graph as unknown as {
      graphToLocal(x: number, y: number): { x: number; y: number };
      localToClient(x: number, y: number): { x: number; y: number };
    };
    const list: RemotePointerUi[] = [];
    for (const [sid, st] of this.remotePointerBySession) {
      if (!st.visible) {
        continue;
      }
      const local = g.graphToLocal(st.gx, st.gy);
      const client = g.localToClient(local.x, local.y);
      list.push({
        sessionId: sid,
        displayName: st.displayName?.trim() || sid.slice(0, 8),
        px: client.x - wr.left,
        py: client.y - wr.top,
        color: collabColorForSession(sid),
        selectionSummary: this.formatRemoteSelectionSummary(st.selectedIds),
      });
    }
    this.remotePointerList = list;
  }

  private formatRemoteSelectionSummary(ids: string[]): string {
    if (!ids.length || !this.graph) {
      return '';
    }
    const labels: string[] = [];
    for (const id of ids.slice(0, 4)) {
      const cell = this.graph.getCellById(id);
      if (!cell) {
        labels.push(id);
        continue;
      }
      if (cell.isNode()) {
        const d = cell.getData() as PoliticaNodoCellData | undefined;
        labels.push(d?.nombre?.trim() || id);
      } else {
        labels.push('Conexión');
      }
    }
    let s = labels.join(', ');
    if (ids.length > 4) {
      s += ` +${ids.length - 4}`;
    }
    return s.length > 48 ? `${s.slice(0, 45)}…` : s;
  }

  private applyRemoteHighlights(sessionId: string, ids: string[], color: string): void {
    this.clearRemoteHighlights(sessionId);
    if (!this.graph || !ids.length) {
      return;
    }
    const acc: HTMLElement[] = [];
    for (const id of ids) {
      const cell = this.graph.getCellById(id);
      if (!cell) {
        continue;
      }
      const view = this.graph.findViewByCell(cell);
      const el = view?.container as HTMLElement | undefined;
      if (el) {
        el.dataset['dpRemoteHl'] = '1';
        el.style.filter = `drop-shadow(0 0 5px ${color})`;
        el.style.opacity = '0.95';
        acc.push(el);
      }
    }
    this.remoteHighlightEls.set(sessionId, acc);
  }

  private clearRemoteHighlights(sessionId: string): void {
    const els = this.remoteHighlightEls.get(sessionId);
    if (els) {
      for (const el of els) {
        delete el.dataset['dpRemoteHl'];
        el.style.filter = '';
        el.style.opacity = '';
      }
    }
    this.remoteHighlightEls.delete(sessionId);
  }

  private clearAllRemotePointerUi(): void {
    for (const sid of [...this.remoteHighlightEls.keys()]) {
      this.clearRemoteHighlights(sid);
    }
    this.remotePointerBySession.clear();
    this.remotePointerList = [];
  }

  private refreshRemoteHighlightsAfterGraphChange(): void {
    for (const sid of [...this.remoteHighlightEls.keys()]) {
      this.clearRemoteHighlights(sid);
    }
    for (const [sid, st] of this.remotePointerBySession) {
      if (st.visible && st.selectedIds.length) {
        this.applyRemoteHighlights(sid, st.selectedIds, collabColorForSession(sid));
      }
    }
    this.rebuildRemotePointerList();
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
