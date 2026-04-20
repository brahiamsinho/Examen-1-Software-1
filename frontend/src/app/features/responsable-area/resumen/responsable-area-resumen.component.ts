import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '@core/auth/auth.service';

@Component({
  selector: 'app-responsable-area-resumen',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './responsable-area-resumen.component.html',
  styleUrl: './responsable-area-resumen.component.scss',
})
export class ResponsableAreaResumenComponent {
  readonly auth = inject(AuthService);
}
