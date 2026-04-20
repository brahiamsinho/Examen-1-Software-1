import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AdminApiService } from '@features/admin/admin-api.service';
import type { PermisoDto, RolAdminDto } from '@features/admin/admin.models';

@Component({
  selector: 'app-admin-roles-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-roles-page.component.html',
  styleUrl: './admin-roles-page.component.scss',
})
export class AdminRolesPageComponent implements OnInit {
  private readonly api = inject(AdminApiService);

  roles: RolAdminDto[] = [];
  permisos: PermisoDto[] = [];
  loading = false;
  errorMsg = '';

  showCreate = false;
  createCodigo = '';
  createNombre = '';

  editing: RolAdminDto | null = null;
  selectedPermisos: Record<string, boolean> = {};

  ngOnInit(): void {
    this.reloadAll();
  }

  reloadAll(): void {
    this.loading = true;
    this.errorMsg = '';
    this.api.listPermisos().subscribe({
      next: (p) => {
        this.permisos = p;
        this.api.listRoles().subscribe({
          next: (r) => {
            this.roles = r;
            this.loading = false;
          },
          error: (e) => this.fail(e),
        });
      },
      error: (e) => this.fail(e),
    });
  }

  private fail(e: unknown): void {
    this.loading = false;
    this.errorMsg = this.msg(e);
  }

  toggleCreate(): void {
    this.showCreate = !this.showCreate;
  }

  submitCreate(): void {
    this.errorMsg = '';
    this.api.createRol({ codigo: this.createCodigo.trim(), nombre: this.createNombre.trim() }).subscribe({
      next: () => {
        this.createCodigo = '';
        this.createNombre = '';
        this.showCreate = false;
        this.reloadAll();
      },
      error: (e) => (this.errorMsg = this.msg(e)),
    });
  }

  startEdit(r: RolAdminDto): void {
    this.editing = r;
    this.selectedPermisos = {};
    for (const p of this.permisos) {
      this.selectedPermisos[p.codigo] = (r.permisoCodigos ?? []).includes(p.codigo);
    }
  }

  cancelEdit(): void {
    this.editing = null;
    this.selectedPermisos = {};
  }

  savePermisos(): void {
    if (!this.editing) {
      return;
    }
    const codes = this.permisos.filter((p) => this.selectedPermisos[p.codigo]).map((p) => p.codigo);
    this.api.patchRol(this.editing.id, { permisoCodigos: codes }).subscribe({
      next: () => {
        this.editing = null;
        this.reloadAll();
      },
      error: (e) => (this.errorMsg = this.msg(e)),
    });
  }

  remove(r: RolAdminDto): void {
    if (!window.confirm(`¿Eliminar rol ${r.codigo}?`)) {
      return;
    }
    this.api.deleteRol(r.id).subscribe({
      next: () => this.reloadAll(),
      error: (e) => (this.errorMsg = this.msg(e)),
    });
  }

  private msg(err: unknown): string {
    if (err instanceof HttpErrorResponse) {
      const b = err.error as { message?: string } | null;
      if (b && typeof b.message === 'string') {
        return b.message;
      }
      return err.message;
    }
    return 'Error de red o servidor.';
  }
}
