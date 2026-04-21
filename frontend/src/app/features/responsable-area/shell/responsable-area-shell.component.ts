import { Component, OnInit, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '@core/auth/auth.service';
import { ResponsableAreaContextService } from '@features/responsable-area/data/responsable-area-context.service';

@Component({
  selector: 'app-responsable-area-shell',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './responsable-area-shell.component.html',
  styleUrl: './responsable-area-shell.component.scss',
})
export class ResponsableAreaShellComponent implements OnInit {
  readonly auth = inject(AuthService);
  readonly ctx = inject(ResponsableAreaContextService);

  ngOnInit(): void {
    this.ctx.refresh();
  }
}
