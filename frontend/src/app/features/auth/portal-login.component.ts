import { Component, OnInit, inject } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '@core/auth/auth.service';
import { environment } from '@environments/environment';

@Component({
  selector: 'app-portal-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './portal-login.component.html',
  styleUrl: './portal-login.component.scss',
})
export class PortalLoginComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly auth = inject(AuthService);

  readonly form = this.fb.group({
    correo: ['', [Validators.required, Validators.email]],
    contrasena: ['', [Validators.required, Validators.minLength(4)]],
  });

  portalTitulo = '';
  portalRol = '';
  errorMsg = '';
  loading = false;

  /** Credenciales de semilla dev (solo cuando `environment.production` es falso). */
  get devSeedHint(): string | null {
    if (environment.production) {
      return null;
    }
    switch (this.portalRol) {
      case 'DISENADOR_POLITICAS':
        return 'Semilla dev: politicas@tramites.local — contraseña demo123 (Spring + Mongo en marcha; `ng serve` con proxy a /backend).';
      case 'ADMINISTRADOR':
        return 'Semilla dev: admin@tramites.local — demo123.';
      case 'RESPONSABLE_AREA':
        return 'Semilla dev: area@tramites.local o legal@tramites.local — demo123 (ambos con área «Departamento legal»). Entrá desde Acceso → Responsable de área.';
      default:
        return null;
    }
  }

  ngOnInit(): void {
    const merged = this.mergeRouteData(this.route.root);
    this.portalRol = String(merged['portalRol'] ?? '');
    this.portalTitulo = String(merged['portalTitulo'] ?? 'Portal');
  }

  /**
   * Une `data` de la raíz hasta la ruta hoja (p. ej. /acceso/politicas) para no perder `portalRol`
   * si el árbol de activación difiere entre entornos o versiones del router.
   */
  private mergeRouteData(route: ActivatedRoute): Record<string, unknown> {
    const out: Record<string, unknown> = {};
    let current: ActivatedRoute | null = route;
    while (current) {
      Object.assign(out, current.snapshot.data);
      current = current.firstChild;
    }
    return out;
  }

  submit(): void {
    this.errorMsg = '';
    if (!this.portalRol) {
      this.errorMsg = 'Configuración de portal incompleta. Volvé al hub de acceso e intentá de nuevo.';
      return;
    }
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    const { correo, contrasena } = this.form.getRawValue();
    this.auth.login(correo, contrasena, this.portalRol).subscribe({
      next: () => {
        this.loading = false;
        void this.router.navigateByUrl(this.auth.portalHomeUrl());
      },
      error: (err: { error?: { message?: string } }) => {
        this.loading = false;
        const body = err?.error;
        this.errorMsg =
          typeof body?.message === 'string'
            ? body.message
            : 'No se pudo iniciar sesión. Revisá correo, contraseña y portal.';
      },
    });
  }
}
