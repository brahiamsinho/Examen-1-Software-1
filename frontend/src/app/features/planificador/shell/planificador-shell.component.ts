import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '@core/auth/auth.service';

@Component({
  selector: 'app-planificador-shell',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './planificador-shell.component.html',
  styleUrl: './planificador-shell.component.scss',
})
export class PlanificadorShellComponent {
  readonly auth = inject(AuthService);
}
