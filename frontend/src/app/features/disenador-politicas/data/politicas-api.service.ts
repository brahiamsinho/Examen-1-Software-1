import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '@environments/environment';
import type { PoliticaNegocioDto, PoliticaPageDto, PoliticaUpsertBody } from '@features/disenador-politicas/models/politica-negocio.model';

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

  private normalizePolitica(p: PoliticaNegocioDto): PoliticaNegocioDto {
    return {
      ...p,
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
      const oid = rec['$oid'];
      if (typeof oid === 'string') {
        return oid;
      }
      const hex = rec['hexString'];
      if (typeof hex === 'string') {
        return hex;
      }
      const str = rec['toString'];
      if (typeof str === 'function') {
        const resolved = String((str as () => unknown).call(value));
        if (resolved && resolved !== '[object Object]') {
          return resolved;
        }
      }
    }
    return String(value);
  }
}
