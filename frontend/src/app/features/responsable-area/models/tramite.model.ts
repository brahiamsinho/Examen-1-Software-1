/** Alineado con `TramiteResponse` del backend. */
export interface TramiteDto {
  id: string;
  codigo: string;
  asunto: string;
  descripcion: string;
  fechaRegistro: string;
  prioridad: string;
  estado: string;
  numeroTurno: number;
  politicaId: string | null;
  clienteId: string;
  nodoActualId: string | null;
  areaActualId: string | null;
}

export interface TramitePageDto {
  content: TramiteDto[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export const TRAMITE_ESTADOS = [
  'REGISTRADO',
  'EN_PROCESO',
  'OBSERVADO',
  'DERIVADO',
  'APROBADO',
  'RECHAZADO',
  'CERRADO',
] as const;

export const TRAMITE_PRIORIDADES = ['BAJA', 'MEDIA', 'ALTA'] as const;
