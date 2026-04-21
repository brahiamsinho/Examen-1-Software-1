import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';
import type {
  BitacoraDto,
  PagedDto,
  PermisoDto,
  RolAdminDto,
  UsuarioAdminDto,
} from '@features/admin/admin.models';

@Injectable({ providedIn: 'root' })
export class AdminApiService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBackendUrl.replace(/\/$/, '')}/api/admin`;

  listUsuarios(page = 0, size = 20): Observable<PagedDto<UsuarioAdminDto>> {
    const params = new HttpParams().set('page', String(page)).set('size', String(size));
    return this.http.get<PagedDto<UsuarioAdminDto>>(`${this.base}/usuarios`, { params });
  }

  createUsuario(body: {
    correo: string;
    contrasena: string;
    nombres: string;
    apellidos: string;
    telefono?: string;
    rolCodigo: string;
    estado?: boolean;
    areaId?: string | null;
  }): Observable<UsuarioAdminDto> {
    return this.http.post<UsuarioAdminDto>(`${this.base}/usuarios`, body);
  }

  patchUsuario(
    id: string,
    body: Partial<{
      correo: string;
      contrasena: string;
      nombres: string;
      apellidos: string;
      telefono: string;
      rolCodigo: string;
      estado: boolean;
      areaId: string | null;
    }>,
  ): Observable<UsuarioAdminDto> {
    return this.http.patch<UsuarioAdminDto>(`${this.base}/usuarios/${id}`, body);
  }

  deleteUsuario(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/usuarios/${id}`);
  }

  listRoles(): Observable<RolAdminDto[]> {
    return this.http.get<RolAdminDto[]>(`${this.base}/roles`);
  }

  createRol(body: { codigo: string; nombre: string }): Observable<RolAdminDto> {
    return this.http.post<RolAdminDto>(`${this.base}/roles`, body);
  }

  patchRol(id: string, body: { nombre?: string; permisoCodigos?: string[] }): Observable<RolAdminDto> {
    return this.http.patch<RolAdminDto>(`${this.base}/roles/${id}`, body);
  }

  deleteRol(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/roles/${id}`);
  }

  listPermisos(): Observable<PermisoDto[]> {
    return this.http.get<PermisoDto[]>(`${this.base}/permisos`);
  }

  createPermiso(body: { codigo: string; nombre: string; descripcion?: string; modulo: string }): Observable<PermisoDto> {
    return this.http.post<PermisoDto>(`${this.base}/permisos`, body);
  }

  patchPermiso(
    id: string,
    body: { nombre?: string; descripcion?: string; modulo?: string },
  ): Observable<PermisoDto> {
    return this.http.patch<PermisoDto>(`${this.base}/permisos/${id}`, body);
  }

  deletePermiso(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/permisos/${id}`);
  }

  listBitacora(page = 0, size = 25): Observable<PagedDto<BitacoraDto>> {
    const params = new HttpParams().set('page', String(page)).set('size', String(size));
    return this.http.get<PagedDto<BitacoraDto>>(`${this.base}/bitacora`, { params });
  }
}
