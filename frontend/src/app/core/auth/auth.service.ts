import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '@environments/environment';
import type { LoginResponseDto } from '@core/models/login-response.model';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly tokenKey = 'tramites_access_token';
  private readonly rolKey = 'tramites_rol_codigo';
  private readonly correoKey = 'tramites_correo';
  private readonly backendBase = environment.apiBackendUrl.replace(/\/$/, '');

  getToken(): string | null {
    return sessionStorage.getItem(this.tokenKey);
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  getRolCodigo(): string | null {
    return sessionStorage.getItem(this.rolKey);
  }

  getCorreo(): string | null {
    return sessionStorage.getItem(this.correoKey);
  }

  isAdministrador(): boolean {
    return this.getRolCodigo() === 'ADMINISTRADOR';
  }

  isDisenadorPoliticas(): boolean {
    return this.getRolCodigo() === 'DISENADOR_POLITICAS';
  }

  isResponsableArea(): boolean {
    return this.getRolCodigo() === 'RESPONSABLE_AREA';
  }

  /** Ruta raíz del módulo lazy del actor actual (post-login o guard). */
  portalHomeUrl(): string {
    switch (this.getRolCodigo()) {
      case 'ADMINISTRADOR':
        return '/admin';
      case 'DISENADOR_POLITICAS':
        return '/disenador';
      case 'RESPONSABLE_AREA':
        return '/responsable-area';
      default:
        return '/dashboard';
    }
  }

  login(correo: string, contrasena: string, portalRol: string): Observable<LoginResponseDto> {
    const url = `${this.backendBase}/api/auth/login`;
    return this.http.post<LoginResponseDto>(url, { correo, contrasena, portalRol }).pipe(
      tap((res) => {
        sessionStorage.setItem(this.tokenKey, res.accessToken);
        sessionStorage.setItem(this.rolKey, res.rolCodigo);
        sessionStorage.setItem(this.correoKey, res.correo);
      }),
    );
  }

  logout(): void {
    sessionStorage.removeItem(this.tokenKey);
    sessionStorage.removeItem(this.rolKey);
    sessionStorage.removeItem(this.correoKey);
    void this.router.navigateByUrl('/acceso');
  }
}
