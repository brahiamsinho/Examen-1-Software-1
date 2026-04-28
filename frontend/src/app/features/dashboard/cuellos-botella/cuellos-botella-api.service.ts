import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';

export interface CuelloBotellaSignal {
  code: string;
  severity: string;
  detail: string;
}

export interface CuelloBotellaResponse {
  status: string;
  signals: CuelloBotellaSignal[];
  summary: string;
}

@Injectable({ providedIn: 'root' })
export class CuellosBotellaApiService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBackendUrl.replace(/\/$/, '')}/api/analitica`;

  analizar(politicaId: string): Observable<CuelloBotellaResponse> {
    return this.http.get<CuelloBotellaResponse>(`${this.base}/politicas/${politicaId}/cuellos-botella`);
  }
}
