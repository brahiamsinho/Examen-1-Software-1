export interface PagedDto<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface UsuarioAdminDto {
  id: string;
  correo: string;
  nombres: string;
  apellidos: string;
  telefono: string;
  estado: boolean;
  rolId: string;
  rolCodigo: string;
  /** Hex de `areas._id` si el usuario pertenece a un departamento (p. ej. responsables de área). */
  areaId?: string | null;
}

export interface RolAdminDto {
  id: string;
  codigo: string;
  nombre: string;
  permisoCodigos: string[];
}

export interface PermisoDto {
  id: string;
  codigo: string;
  nombre: string;
  descripcion: string;
  modulo: string;
}

export interface BitacoraDto {
  id: string;
  fecha: string;
  actorUsuarioId: string;
  actorCorreo: string;
  accion: string;
  entidad: string;
  detalle: string;
}

export interface ApiErrorBody {
  message?: string;
}
