import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';
import type { UsuarioAreaDto } from '@features/disenador-politicas/models/usuario-area.model';

@Injectable({ providedIn: 'root' })
export class UsuariosAreaApiService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBackendUrl.replace(/\/$/, '')}/api/seguridad/usuarios`;

  listPorArea(areaId: string): Observable<UsuarioAreaDto[]> {
    const params = new HttpParams().set('areaId', areaId);
    return this.http.get<UsuarioAreaDto[]>(this.base, { params });
  }
}
