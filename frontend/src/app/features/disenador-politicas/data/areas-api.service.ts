import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '@environments/environment';
import type { AreaDto } from '@features/disenador-politicas/models/area.model';

export interface AreaUpsertBody {
  nombre: string;
  descripcion: string;
  estado: boolean;
}

/** Catálogo de áreas (`/api/seguridad/areas`) para selectores y gestión en el diseñador. */
@Injectable({ providedIn: 'root' })
export class AreasApiService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBackendUrl.replace(/\/$/, '')}/api/seguridad/areas`;

  list(): Observable<AreaDto[]> {
    return this.http.get<AreaDto[]>(this.base).pipe(map((areas) => (areas ?? []).map((a) => this.normalizeArea(a))));
  }

  crear(body: AreaUpsertBody): Observable<AreaDto> {
    return this.http.post<AreaDto>(this.base, body).pipe(map((a) => this.normalizeArea(a)));
  }

  actualizar(id: string, body: AreaUpsertBody): Observable<AreaDto> {
    return this.http
      .put<AreaDto>(`${this.base}/${encodeURIComponent(id)}`, body)
      .pipe(map((a) => this.normalizeArea(a)));
  }

  eliminar(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${encodeURIComponent(id)}`);
  }

  private normalizeArea(a: AreaDto): AreaDto {
    return {
      ...a,
      id: this.asStringId(a.id),
    };
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
