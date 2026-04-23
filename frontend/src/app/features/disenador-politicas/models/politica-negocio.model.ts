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
  return {
    nombre: p.nombre,
    descripcion: p.descripcion,
    version: p.version,
    lockVersion: p.lockVersion ?? 0,
    estado: p.estado,
    nodos: p.nodos.map((n) => ({
      idNodo: n.idNodo,
      nombre: n.nombre,
      tipoNodo: n.tipoNodo,
      orden: n.orden,
      condicion: n.condicion ?? null,
      esInicial: n.esInicial,
      esFinal: n.esFinal,
      areaId: n.areaId ?? null,
      asignacionesResponsable: (n.asignacionesResponsable ?? []).map((a) => ({
        usuarioId: a.usuarioId,
        areaId: a.areaId,
        fechaAsignacion: a.fechaAsignacion,
        estado: a.estado,
      })),
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
