import { Component, OnInit, inject } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '@core/auth/auth.service';

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

  ngOnInit(): void {
    const data = this.route.snapshot.data as { portalRol: string; portalTitulo: string };
    this.portalRol = data.portalRol;
    this.portalTitulo = data.portalTitulo;
  }

  submit(): void {
    this.errorMsg = '';
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
