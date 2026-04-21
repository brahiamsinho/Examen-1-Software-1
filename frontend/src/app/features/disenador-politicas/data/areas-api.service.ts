import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
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
    return this.http.get<AreaDto[]>(this.base);
  }

  crear(body: AreaUpsertBody): Observable<AreaDto> {
    return this.http.post<AreaDto>(this.base, body);
  }

  actualizar(id: string, body: AreaUpsertBody): Observable<AreaDto> {
    return this.http.put<AreaDto>(`${this.base}/${encodeURIComponent(id)}`, body);
  }

  eliminar(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${encodeURIComponent(id)}`);
  }
}
