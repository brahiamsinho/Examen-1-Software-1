import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '@core/auth/auth.service';

@Component({
  selector: 'app-responsable-area-shell',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './responsable-area-shell.component.html',
  styleUrl: './responsable-area-shell.component.scss',
})
export class ResponsableAreaShellComponent {
  readonly auth = inject(AuthService);
}
