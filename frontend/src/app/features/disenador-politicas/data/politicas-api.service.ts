import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '@environments/environment';
import type {
  PoliticaNegocioDto,
  PoliticaPageDto,
  PoliticaUpsertBody,
} from '@features/disenador-politicas/models/politica-negocio.model';

/** Metadatos de una revisión guardada (listado). */
export interface PoliticaRevisionResumenDto {
  revision: number;
  guardadoEn: string;
  nombre: string;
  version: number;
  estado: string;
}

export interface PoliticaRevisionPageDto {
  content: PoliticaRevisionResumenDto[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

@Injectable({ providedIn: 'root' })
export class PoliticasApiService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBackendUrl.replace(/\/$/, '')}/api/politicas`;

  list(page = 0, size = 20): Observable<PoliticaPageDto> {
    const params = new HttpParams().set('page', String(page)).set('size', String(size));
    return this.http.get<PoliticaPageDto>(this.base, { params }).pipe(
      map((res) => ({
        ...res,
        content: (res.content ?? []).map((p) => this.normalizePolitica(p)),
      })),
    );
  }

  getById(id: string): Observable<PoliticaNegocioDto> {
    return this.http
      .get<PoliticaNegocioDto>(`${this.base}/${id}`)
      .pipe(map((p) => this.normalizePolitica(p)));
  }

  create(body: PoliticaUpsertBody): Observable<PoliticaNegocioDto> {
    return this.http
      .post<PoliticaNegocioDto>(this.base, body)
      .pipe(map((p) => this.normalizePolitica(p)));
  }

  update(id: string, body: PoliticaUpsertBody): Observable<PoliticaNegocioDto> {
    return this.http
      .put<PoliticaNegocioDto>(`${this.base}/${id}`, body)
      .pipe(map((p) => this.normalizePolitica(p)));
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }

  listRevisiones(id: string, page = 0, size = 20): Observable<PoliticaRevisionPageDto> {
    const params = new HttpParams().set('page', String(page)).set('size', String(size));
    return this.http.get<PoliticaRevisionPageDto>(`${this.base}/${encodeURIComponent(id)}/revisiones`, { params });
  }

  getRevision(id: string, revision: number): Observable<PoliticaNegocioDto> {
    return this.http
      .get<PoliticaNegocioDto>(`${this.base}/${encodeURIComponent(id)}/revisiones/${revision}`)
      .pipe(map((p) => this.normalizePolitica(p)));
  }

  private normalizePolitica(p: PoliticaNegocioDto): PoliticaNegocioDto {
    return {
      ...p,
      bpmnXml: p.bpmnXml ?? null,
      nodos: (p.nodos ?? []).map((n) => ({
        ...n,
        areaId: this.asStringId(n.areaId),
        asignacionesResponsable: (n.asignacionesResponsable ?? []).map((a) => ({
          ...a,
          usuarioId: this.asStringId(a.usuarioId) ?? '',
          areaId: this.asStringId(a.areaId) ?? '',
        })),
      })),
    };
  }

  private asStringId(value: unknown): string | null {
    if (value == null) {
      return null;
    }
    if (typeof value === 'string') {
      return value;
    }
    if (typeof value === 'object') {
      const rec = value as Record<string, unknown>;
      const directKeys = ['$oid', 'oid', 'hexString', 'id', '_id'] as const;
      for (const key of directKeys) {
        const candidate = rec[key];
        if (typeof candidate === 'string' && candidate.trim()) {
          return candidate;
        }
      }
      const toHex = rec['toHexString'];
      if (typeof toHex === 'function') {
        const resolved = String((toHex as () => unknown).call(value));
        if (resolved && resolved !== '[object Object]') {
          return resolved;
        }
      }
      const toStr = rec['toString'];
      if (typeof toStr === 'function') {
        const resolved = String((toStr as () => unknown).call(value));
        if (resolved && resolved !== '[object Object]') {
          return resolved;
        }
      }
    }
    const fallback = String(value);
    return fallback === '[object Object]' ? null : fallback;
  }
}
