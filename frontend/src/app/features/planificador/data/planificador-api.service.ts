import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';
import type { TramiteDto, TramitePageDto } from '@features/responsable-area/models/tramite.model';

@Injectable({ providedIn: 'root' })
export class PlanificadorApiService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBackendUrl.replace(/\/$/, '')}/api/planificador/tramites`;

  listarPendientesPolitica(page = 0, size = 20): Observable<TramitePageDto> {
    const params = new HttpParams().set('page', String(page)).set('size', String(size));
    return this.http.get<TramitePageDto>(`${this.base}/pendientes-politica`, { params });
  }

  asignarPolitica(tramiteId: string, politicaId: string): Observable<TramiteDto> {
    return this.http.post<TramiteDto>(`${this.base}/${tramiteId}/asignar-politica`, { politicaId });
  }
}
