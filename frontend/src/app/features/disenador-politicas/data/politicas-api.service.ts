import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';
import type { PoliticaNegocioDto, PoliticaPageDto, PoliticaUpsertBody } from '@features/disenador-politicas/models/politica-negocio.model';

@Injectable({ providedIn: 'root' })
export class PoliticasApiService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBackendUrl.replace(/\/$/, '')}/api/politicas`;

  list(page = 0, size = 20): Observable<PoliticaPageDto> {
    const params = new HttpParams().set('page', String(page)).set('size', String(size));
    return this.http.get<PoliticaPageDto>(this.base, { params });
  }

  getById(id: string): Observable<PoliticaNegocioDto> {
    return this.http.get<PoliticaNegocioDto>(`${this.base}/${id}`);
  }

  create(body: PoliticaUpsertBody): Observable<PoliticaNegocioDto> {
    return this.http.post<PoliticaNegocioDto>(this.base, body);
  }

  update(id: string, body: PoliticaUpsertBody): Observable<PoliticaNegocioDto> {
    return this.http.put<PoliticaNegocioDto>(`${this.base}/${id}`, body);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
