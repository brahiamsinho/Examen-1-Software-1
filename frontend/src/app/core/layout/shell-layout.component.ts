import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { environment } from '@environments/environment';
import { AuthService } from '@core/auth/auth.service';

@Component({
  selector: 'app-shell-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './shell-layout.component.html',
})
export class ShellLayoutComponent {
  readonly appTitle = environment.appTitle;
  readonly auth = inject(AuthService);

  logout(): void {
    this.auth.logout();
  }
}
