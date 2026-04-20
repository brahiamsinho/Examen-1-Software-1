import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '@core/auth/auth.service';

@Component({
  selector: 'app-disenador-politicas-shell',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './disenador-politicas-shell.component.html',
  styleUrl: './disenador-politicas-shell.component.scss',
})
export class DisenadorPoliticasShellComponent {
  readonly auth = inject(AuthService);
}
