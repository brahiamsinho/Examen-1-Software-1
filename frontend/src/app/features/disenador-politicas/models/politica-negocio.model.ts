/** Alineado con `PoliticaNegocioResponse` del backend Spring. */
export interface PoliticaAsignacionDto {
  usuarioId: string;
  areaId: string;
  fechaAsignacion: string;
  estado: boolean;
}

export interface PoliticaNodoDto {
  idNodo: string;
  nombre: string;
  tipoNodo: string;
  orden: number;
  condicion?: string | null;
  esInicial: boolean;
  esFinal: boolean;
  areaId?: string | null;
  asignacionesResponsable?: PoliticaAsignacionDto[];
  /** URL HTTPS (p. ej. Google Forms) para el responsable en el nodo actual del flujo. */
  formularioExternoUrl?: string | null;
  /** Carril / swimlane BPMN (texto libre). */
  carrilBpmn?: string | null;
}

export interface PoliticaConexionDto {
  idConexion: string;
  tipoFlujo: string;
  condicion?: string | null;
  origenNodoId: string;
  destinoNodoId: string;
}

export interface PoliticaNegocioDto {
  id: string;
  nombre: string;
  descripcion: string;
  version: number;
  /** Concurrencia optimista (Spring `@Version`); obligatorio en PUT. */
  lockVersion: number;
  estado: string;
  fechaCreacion: string;
  /** Fuente canónica BPMN del modelador. */
  bpmnXml?: string | null;
  nodos: PoliticaNodoDto[];
  conexiones: PoliticaConexionDto[];
}

export interface PoliticaUpsertBody {
  nombre: string;
  descripcion: string;
  version: number;
  /** Obligatorio en PUT; omitir en POST de creación. */
  lockVersion?: number;
  estado: string;
  bpmnXml?: string | null;
  nodos: PoliticaNodoUpsert[];
  conexiones: PoliticaConexionUpsert[];
}

export interface PoliticaNodoUpsert {
  idNodo: string;
  nombre: string;
  tipoNodo: string;
  orden: number;
  condicion?: string | null;
  esInicial: boolean;
  esFinal: boolean;
  areaId?: string | null;
  asignacionesResponsable?: PoliticaAsignacionUpsert[];
  formularioExternoUrl?: string | null;
  carrilBpmn?: string | null;
}

export interface PoliticaAsignacionUpsert {
  usuarioId: string;
  areaId: string;
  fechaAsignacion?: string;
  estado: boolean;
}

export interface PoliticaConexionUpsert {
  idConexion: string;
  tipoFlujo: string;
  condicion?: string | null;
  origenNodoId: string;
  destinoNodoId: string;
}

export interface PoliticaPageDto {
  content: PoliticaNegocioDto[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

/** Plantilla mínima válida para crear una política nueva desde el catálogo. */
export function buildDefaultPoliticaUpsert(nombre: string, descripcion: string, version: number, estado: string): PoliticaUpsertBody {
  return {
    nombre: nombre.trim(),
    descripcion: descripcion.trim(),
    version,
    estado,
    nodos: [
      {
        idNodo: 'n_inicio',
        nombre: 'Inicio',
        tipoNodo: 'INICIO',
        orden: 0,
        condicion: null,
        esInicial: true,
        esFinal: false,
        areaId: null,
        asignacionesResponsable: [],
        formularioExternoUrl: null,
        carrilBpmn: null,
      },
      {
        idNodo: 'n_actividad',
        nombre: 'Actividad (definir departamento)',
        tipoNodo: 'ACTIVIDAD',
        orden: 1,
        condicion: null,
        esInicial: false,
        esFinal: false,
        areaId: null,
        asignacionesResponsable: [],
        formularioExternoUrl: null,
        carrilBpmn: null,
      },
      {
        idNodo: 'n_fin',
        nombre: 'Fin',
        tipoNodo: 'FIN',
        orden: 2,
        condicion: null,
        esInicial: false,
        esFinal: true,
        areaId: null,
        asignacionesResponsable: [],
        formularioExternoUrl: null,
        carrilBpmn: null,
      },
    ],
    conexiones: [
      {
        idConexion: 'c_1',
        tipoFlujo: 'SECUENCIAL',
        condicion: null,
        origenNodoId: 'n_inicio',
        destinoNodoId: 'n_actividad',
      },
      {
        idConexion: 'c_2',
        tipoFlujo: 'SECUENCIAL',
        condicion: null,
        origenNodoId: 'n_actividad',
        destinoNodoId: 'n_fin',
      },
    ],
  };
}

export function politicaDtoToUpsertBody(p: PoliticaNegocioDto): PoliticaUpsertBody {
  const asStringId = (value: unknown): string | null => {
    if (value == null) {
      return null;
    }
    if (typeof value === 'string') {
      return value;
    }
    if (typeof value === 'object') {
      const rec = value as Record<string, unknown>;
      const directKeys = ['$oid', 'oid', 'hexString', 'id', '_id'] as const;
      for (const key of directKeys) {
        const candidate = rec[key];
        if (typeof candidate === 'string' && candidate.trim()) {
          return candidate;
        }
      }
      const toHex = rec['toHexString'];
      if (typeof toHex === 'function') {
        const resolved = String((toHex as () => unknown).call(value));
        if (resolved && resolved !== '[object Object]') {
          return resolved;
        }
      }
      const toStr = rec['toString'];
      if (typeof toStr === 'function') {
        const resolved = String((toStr as () => unknown).call(value));
        if (resolved && resolved !== '[object Object]') {
          return resolved;
        }
      }
    }
    const fallback = String(value);
    return fallback === '[object Object]' ? null : fallback;
  };

  return {
    nombre: p.nombre,
    descripcion: p.descripcion,
    version: p.version,
    lockVersion: p.lockVersion ?? 0,
    estado: p.estado,
    bpmnXml: p.bpmnXml?.trim() || null,
    nodos: p.nodos.map((n) => ({
      idNodo: n.idNodo,
      nombre: n.nombre,
      tipoNodo: n.tipoNodo,
      orden: n.orden,
      condicion: n.condicion ?? null,
      esInicial: n.esInicial,
      esFinal: n.esFinal,
      areaId: asStringId(n.areaId),
      asignacionesResponsable: (n.asignacionesResponsable ?? []).map((a) => ({
        usuarioId: asStringId(a.usuarioId) ?? '',
        areaId: asStringId(a.areaId) ?? '',
        fechaAsignacion: a.fechaAsignacion,
        estado: a.estado,
      })),
      formularioExternoUrl: n.formularioExternoUrl?.trim() || null,
      carrilBpmn: n.carrilBpmn?.trim() || null,
    })),
    conexiones: p.conexiones.map((c) => ({
      idConexion: c.idConexion,
      tipoFlujo: c.tipoFlujo,
      condicion: c.condicion ?? null,
      origenNodoId: c.origenNodoId,
      destinoNodoId: c.destinoNodoId,
    })),
  };
}
