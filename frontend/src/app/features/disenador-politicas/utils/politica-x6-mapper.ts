import type { Cell, Edge, Graph, Node } from '@antv/x6';
import type {
  PoliticaAsignacionDto,
  PoliticaConexionDto,
  PoliticaNegocioDto,
  PoliticaNodoDto,
} from '@features/disenador-politicas/models/politica-negocio.model';

/** Datos de dominio embebidos en cada celda X6 (alineado con `politicas_negocio.nodos` en script.db). */
export interface PoliticaNodoCellData {
  idNodo: string;
  nombre: string;
  tipoNodo: string;
  orden: number;
  condicion: string | null;
  esInicial: boolean;
  esFinal: boolean;
  areaId: string | null;
  asignacionesResponsable: PoliticaAsignacionDto[];
}

export interface PoliticaEdgeCellData {
  idConexion: string;
  tipoFlujo: string;
  condicion: string | null;
}

/** Puerto de entrada (arriba). Varias salidas abajo permiten bifurcar a varios departamentos. */
export const PORT_IN = 'in';
/** Salidas por defecto al crear conexión por clic (centro). */
export const PORT_OUT_DEFAULT = 'out3';
export const PORTS_OUT = ['out1', 'out2', 'out3', 'out4', 'out5'] as const;

function nodoPorts(): Node.Properties['ports'] {
  return {
    groups: {
      anchor: {
        position: 'absolute',
        attrs: {
          circle: {
            r: 6,
            magnet: true,
            stroke: '#4f46e5',
            strokeWidth: 2,
            fill: '#eef2ff',
            cursor: 'crosshair',
          },
        },
      },
    },
    items: [
      { id: PORT_IN, group: 'anchor', args: { x: '50%', y: 0 } },
      { id: 'out1', group: 'anchor', args: { x: '12%', y: '100%' } },
      { id: 'out2', group: 'anchor', args: { x: '30%', y: '100%' } },
      { id: PORT_OUT_DEFAULT, group: 'anchor', args: { x: '50%', y: '100%' } },
      { id: 'out4', group: 'anchor', args: { x: '70%', y: '100%' } },
      { id: 'out5', group: 'anchor', args: { x: '88%', y: '100%' } },
    ],
  };
}

const COL_X = 220;
const ROW_H = 100;
const BASE_Y = 40;

function strokeForTipo(tipo: string): string {
  switch (tipo) {
    case 'INICIO':
      return '#10b981';
    case 'FIN':
      return '#dc2626';
    case 'DECISION':
      return '#6366f1';
    case 'PARALELO':
      return '#0ea5e9';
    default:
      return '#64748b';
  }
}

function fillForTipo(tipo: string): string {
  switch (tipo) {
    case 'INICIO':
      return '#ecfdf5';
    case 'FIN':
      return '#fef2f2';
    case 'DECISION':
      return '#eef2ff';
    case 'PARALELO':
      return '#e0f2fe';
    default:
      return '#ffffff';
  }
}

function nodeShape(tipo: string): string {
  if (tipo === 'INICIO' || tipo === 'FIN') {
    return 'ellipse';
  }
  if (tipo === 'DECISION' || tipo === 'PARALELO') {
    return 'polygon';
  }
  return 'rect';
}

function nodeSize(tipo: string): { width: number; height: number } {
  if (tipo === 'INICIO' || tipo === 'FIN') {
    return { width: 72, height: 72 };
  }
  if (tipo === 'DECISION' || tipo === 'PARALELO') {
    return { width: 76, height: 76 };
  }
  return { width: 200, height: 56 };
}

function polygonRefPoints(tipo: string): string {
  if (tipo === 'PARALELO') {
    return '38,0 76,38 38,76 0,38';
  }
  return '38,0 76,38 38,76 0,38';
}

function politicaNodoToCell(n: PoliticaNodoDto, x: number, y: number): Node.Properties {
  const { width, height } = nodeSize(n.tipoNodo);
  const shape = nodeShape(n.tipoNodo);
  const data: PoliticaNodoCellData = {
    idNodo: n.idNodo,
    nombre: n.nombre,
    tipoNodo: n.tipoNodo,
    orden: n.orden,
    condicion: n.condicion ?? null,
    esInicial: n.esInicial,
    esFinal: n.esFinal,
    areaId: n.areaId ?? null,
    asignacionesResponsable: [...(n.asignacionesResponsable ?? [])],
  };
  const body: Record<string, string | number> = {
    stroke: strokeForTipo(n.tipoNodo),
    strokeWidth: 2,
    fill: fillForTipo(n.tipoNodo),
  };
  if (shape === 'rect') {
    body['rx'] = 10;
    body['ry'] = 10;
  }
  if (shape === 'polygon') {
    body['refPoints'] = polygonRefPoints(n.tipoNodo);
  }
  return {
    id: n.idNodo,
    shape,
    x,
    y,
    width,
    height,
    data,
    ports: nodoPorts(),
    attrs: {
      body,
      label: {
        text: n.nombre,
        fill: '#0f172a',
        fontSize: 12,
        fontWeight: 600,
        refX: 0.5,
        refY: 0.5,
        textAnchor: 'middle',
        textVerticalAnchor: 'middle',
      },
    },
  };
}

const NOMBRE_TIPO_DEF: Record<string, string> = {
  INICIO: 'Inicio',
  ACTIVIDAD: 'Actividad (departamento)',
  DECISION: 'Decisión',
  PARALELO: 'Paralelo',
  FIN: 'Fin',
};

/** Crea metadatos de celda para un nodo nuevo en el lienzo (tipos según script.db / backend). */
export function createBlankNodeCell(tipo: string, id: string, x: number, y: number): Node.Properties {
  const n: PoliticaNodoDto = {
    idNodo: id,
    nombre: NOMBRE_TIPO_DEF[tipo] ?? 'Nodo',
    tipoNodo: tipo,
    orden: 0,
    condicion: null,
    esInicial: tipo === 'INICIO',
    esFinal: tipo === 'FIN',
    areaId: null,
    asignacionesResponsable: [],
  };
  return politicaNodoToCell(n, x, y);
}

export function buildCellsFromPolitica(p: PoliticaNegocioDto): Cell.Properties[] {
  const cells: Cell.Properties[] = [];
  const nodos = [...(p.nodos ?? [])].sort((a, b) => a.orden - b.orden);
  nodos.forEach((n, i) => {
    cells.push(politicaNodoToCell(n, COL_X, BASE_Y + i * ROW_H));
  });
  for (const c of p.conexiones ?? []) {
    cells.push({
      id: c.idConexion,
      shape: 'edge',
      source: { cell: c.origenNodoId, port: PORT_OUT_DEFAULT },
      target: { cell: c.destinoNodoId, port: PORT_IN },
      data: {
        idConexion: c.idConexion,
        tipoFlujo: c.tipoFlujo,
        condicion: c.condicion ?? null,
      } satisfies PoliticaEdgeCellData,
      attrs: {
        line: {
          stroke: '#475569',
          strokeWidth: 2,
          targetMarker: { name: 'classic', size: 8, width: 8, height: 8 },
        },
      },
    } as Edge.Properties);
  }
  return cells;
}

function readNodeData(node: Node): PoliticaNodoCellData {
  const d = node.getData() as Partial<PoliticaNodoCellData> | undefined;
  const labelText = String(node.attr<string>('label/text') ?? '');
  const idNodo = d?.idNodo ?? node.id;
  const nombreRaw = (d?.nombre && String(d.nombre).trim()) || labelText.trim() || idNodo;
  return {
    idNodo,
    nombre: nombreRaw,
    tipoNodo: d?.tipoNodo ?? 'ACTIVIDAD',
    orden: typeof d?.orden === 'number' ? d.orden : 0,
    condicion: d?.condicion ?? null,
    esInicial: Boolean(d?.esInicial),
    esFinal: Boolean(d?.esFinal),
    areaId: d?.areaId ?? null,
    asignacionesResponsable: Array.isArray(d?.asignacionesResponsable) ? d!.asignacionesResponsable! : [],
  };
}

function readEdgeData(edge: Edge): PoliticaConexionDto {
  const src = edge.getSourceCellId();
  const tgt = edge.getTargetCellId();
  const d = edge.getData() as Partial<PoliticaEdgeCellData> | undefined;
  const idConexion = d?.idConexion ?? edge.id;
  return {
    idConexion,
    tipoFlujo: d?.tipoFlujo ?? 'SECUENCIAL',
    condicion: d?.condicion ?? null,
    origenNodoId: src ?? '',
    destinoNodoId: tgt ?? '',
  };
}

/** Ordena nodos por posición Y (flujo vertical típico en el lienzo). */
export function graphToPoliticaNegocio(graph: Graph, politica: PoliticaNegocioDto, meta: PoliticaMetaCabecera): PoliticaNegocioDto {
  const nodes = graph.getNodes().sort((a, b) => a.getBBox().y - b.getBBox().y);
  const nodos: PoliticaNodoDto[] = nodes.map((node, index) => {
    const r = readNodeData(node);
    return {
      idNodo: r.idNodo,
      nombre: r.nombre,
      tipoNodo: r.tipoNodo,
      orden: index,
      condicion: r.condicion,
      esInicial: r.esInicial,
      esFinal: r.esFinal,
      areaId: r.areaId,
      asignacionesResponsable: r.asignacionesResponsable,
    };
  });
  const conexiones: PoliticaConexionDto[] = graph
    .getEdges()
    .map((e) => readEdgeData(e))
    .filter((c) => c.origenNodoId.length > 0 && c.destinoNodoId.length > 0);
  return {
    ...politica,
    nombre: meta.nombre.trim(),
    descripcion: meta.descripcion.trim(),
    version: meta.version,
    estado: meta.estado.trim(),
    nodos,
    conexiones,
  };
}

export interface PoliticaMetaCabecera {
  nombre: string;
  descripcion: string;
  version: number;
  estado: string;
}

export function newNodoId(prefix: string): string {
  return `${prefix}_${Math.random().toString(36).slice(2, 10)}`;
}

export function newConexionId(): string {
  return `c_${Math.random().toString(36).slice(2, 10)}`;
}

/** Crea una arista lista para `graph.addEdge` (misma forma que al cargar desde API). */
export function createPoliticaEdgeBetween(
  sourceNodeId: string,
  targetNodeId: string,
  sourcePort: string = PORT_OUT_DEFAULT,
  targetPort: string = PORT_IN,
): Edge.Properties {
  const id = newConexionId();
  return {
    id,
    shape: 'edge',
    source: { cell: sourceNodeId, port: sourcePort },
    target: { cell: targetNodeId, port: targetPort },
    data: {
      idConexion: id,
      tipoFlujo: 'SECUENCIAL',
      condicion: null,
    } satisfies PoliticaEdgeCellData,
    attrs: {
      line: {
        stroke: '#475569',
        strokeWidth: 2,
        targetMarker: { name: 'classic', size: 8, width: 8, height: 8 },
      },
    },
  } as Edge.Properties;
}
