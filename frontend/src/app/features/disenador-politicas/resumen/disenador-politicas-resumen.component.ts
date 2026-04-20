import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '@core/auth/auth.service';

@Component({
  selector: 'app-disenador-politicas-resumen',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './disenador-politicas-resumen.component.html',
  styleUrl: './disenador-politicas-resumen.component.scss',
})
export class DisenadorPoliticasResumenComponent {
  readonly auth = inject(AuthService);
}
