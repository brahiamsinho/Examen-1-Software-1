import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';
import { ApiEndpoints } from '@core/constants/api-endpoints';
import type { HealthResponseDto } from '@core/models/health-response.model';

@Injectable({
  providedIn: 'root',
})
export class ApiService {
  private readonly http = inject(HttpClient);

  private readonly backendBase = environment.apiBackendUrl.replace(/\/$/, '');
  private readonly fastApiBase = environment.apiFastApiUrl.replace(/\/$/, '');

  /** GET JSON al backend Spring (ruta absoluta desde el prefijo proxy, ej. `/health`). */
  getFromBackend<T>(path: string): Observable<T> {
    return this.http.get<T>(this.join(this.backendBase, path));
  }

  /** GET JSON al microservicio FastAPI (ruta absoluta desde la raíz del servicio). */
  getFromFastApi<T>(path: string): Observable<T> {
    return this.http.get<T>(this.join(this.fastApiBase, path));
  }

  checkBackendHealth(): Observable<HealthResponseDto> {
    return this.getFromBackend<HealthResponseDto>(ApiEndpoints.springBoot.health);
  }

  checkFastApiHealth(): Observable<HealthResponseDto> {
    return this.getFromFastApi<HealthResponseDto>(ApiEndpoints.fastApi.health);
  }

  private join(base: string, path: string): string {
    const normalized = path.startsWith('/') ? path : `/${path}`;
    return `${base}${normalized}`;
  }
}
