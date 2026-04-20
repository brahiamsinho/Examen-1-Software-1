import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AdminApiService } from '@features/admin/admin-api.service';

@Component({
  selector: 'app-admin-home',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './admin-home.component.html',
  styleUrl: './admin-home.component.scss',
})
export class AdminHomeComponent implements OnInit {
  private readonly api = inject(AdminApiService);

  totalUsuarios = 0;
  totalRoles = 0;
  totalPermisos = 0;
  totalBitacora = 0;
  loading = true;

  ngOnInit(): void {
    forkJoin({
      u: this.api.listUsuarios(0, 1),
      r: this.api.listRoles(),
      p: this.api.listPermisos(),
      b: this.api.listBitacora(0, 1),
    }).subscribe({
      next: ({ u, r, p, b }) => {
        this.totalUsuarios = u.totalElements;
        this.totalRoles = r.length;
        this.totalPermisos = p.length;
        this.totalBitacora = b.totalElements;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      },
    });
  }
}
