import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';
import type { SalidaFlujoDto } from '@features/responsable-area/models/salida-flujo.model';
import type { TramiteDto, TramitePageDto } from '@features/responsable-area/models/tramite.model';

@Injectable({ providedIn: 'root' })
export class TramitesApiService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBackendUrl.replace(/\/$/, '')}/api/tramites`;

  list(page = 0, size = 20): Observable<TramitePageDto> {
    const params = new HttpParams().set('page', String(page)).set('size', String(size));
    return this.http.get<TramitePageDto>(this.base, { params });
  }

  colaFifo(estado: string, prioridad?: string | null): Observable<TramiteDto[]> {
    let params = new HttpParams().set('estado', estado);
    if (prioridad != null && prioridad.trim().length > 0) {
      params = params.set('prioridad', prioridad.trim());
    }
    return this.http.get<TramiteDto[]>(`${this.base}/cola/fifo`, { params });
  }

  getSalidas(tramiteId: string): Observable<SalidaFlujoDto[]> {
    return this.http.get<SalidaFlujoDto[]>(`${this.base}/${encodeURIComponent(tramiteId)}/flujo/salidas`);
  }

  avanzarFlujo(tramiteId: string, idConexion: string, observacion?: string): Observable<TramiteDto> {
    return this.http.post<TramiteDto>(`${this.base}/${encodeURIComponent(tramiteId)}/flujo/avanzar`, {
      idConexion,
      observacion: observacion?.trim() || undefined,
    });
  }

  aprobarRamaParalela(tramiteId: string, nodoRamaId: string): Observable<TramiteDto> {
    return this.http.post<TramiteDto>(
      `${this.base}/${encodeURIComponent(tramiteId)}/flujo/aprobar-rama-paralela`,
      { nodoRamaId },
    );
  }
}
