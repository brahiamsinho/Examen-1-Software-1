import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '@environments/environment';
import type { UsuarioAreaDto } from '@features/disenador-politicas/models/usuario-area.model';

@Injectable({ providedIn: 'root' })
export class UsuariosAreaApiService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBackendUrl.replace(/\/$/, '')}/api/seguridad/usuarios`;

  listPorArea(areaId: string): Observable<UsuarioAreaDto[]> {
    const params = new HttpParams().set('areaId', this.asStringId(areaId));
    return this.http
      .get<UsuarioAreaDto[]>(this.base, { params })
      .pipe(map((users) => (users ?? []).map((u) => ({ ...u, id: this.asStringId(u.id) }))));
  }

  private asStringId(value: unknown): string {
    if (typeof value === 'string') {
      return value;
    }
    if (value && typeof value === 'object') {
      const rec = value as Record<string, unknown>;
      const oid = rec['$oid'];
      if (typeof oid === 'string') {
        return oid;
      }
      const hex = rec['hexString'];
      if (typeof hex === 'string') {
        return hex;
      }
    }
    return String(value ?? '');
  }
}
