/** Respuesta de `GET /api/seguridad/usuarios?areaId=` (usuarios activos del área). */
export interface UsuarioAreaDto {
  id: string;
  correo: string;
  nombres: string;
  apellidos: string;
}
