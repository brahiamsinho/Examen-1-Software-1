import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AdminApiService } from '@features/admin/admin-api.service';
import type { PermisoDto } from '@features/admin/admin.models';

@Component({
  selector: 'app-admin-permissions-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-permissions-page.component.html',
  styleUrl: './admin-permissions-page.component.scss',
})
export class AdminPermissionsPageComponent implements OnInit {
  private readonly api = inject(AdminApiService);

  permisos: PermisoDto[] = [];
  loading = false;
  errorMsg = '';

  showCreate = false;
  create = { codigo: '', nombre: '', modulo: 'seguridad', descripcion: '' };

  editing: PermisoDto | null = null;
  editDraft = { nombre: '', descripcion: '', modulo: '' };

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.errorMsg = '';
    this.api.listPermisos().subscribe({
      next: (p) => {
        this.permisos = p;
        this.loading = false;
      },
      error: (e) => {
        this.loading = false;
        this.errorMsg = this.msg(e);
      },
    });
  }

  toggleCreate(): void {
    this.showCreate = !this.showCreate;
  }

  submitCreate(): void {
    this.api
      .createPermiso({
        codigo: this.create.codigo.trim(),
        nombre: this.create.nombre.trim(),
        modulo: this.create.modulo.trim(),
        descripcion: this.create.descripcion.trim(),
      })
      .subscribe({
        next: () => {
          this.create = { codigo: '', nombre: '', modulo: 'seguridad', descripcion: '' };
          this.showCreate = false;
          this.load();
        },
        error: (e) => (this.errorMsg = this.msg(e)),
      });
  }

  startEdit(p: PermisoDto): void {
    this.editing = p;
    this.editDraft = { nombre: p.nombre, descripcion: p.descripcion ?? '', modulo: p.modulo };
  }

  cancelEdit(): void {
    this.editing = null;
  }

  saveEdit(): void {
    if (!this.editing) {
      return;
    }
    this.api
      .patchPermiso(this.editing.id, {
        nombre: this.editDraft.nombre,
        descripcion: this.editDraft.descripcion,
        modulo: this.editDraft.modulo,
      })
      .subscribe({
        next: () => {
          this.editing = null;
          this.load();
        },
        error: (e) => (this.errorMsg = this.msg(e)),
      });
  }

  remove(p: PermisoDto): void {
    if (!window.confirm(`¿Eliminar permiso ${p.codigo}?`)) {
      return;
    }
    this.api.deletePermiso(p.id).subscribe({
      next: () => this.load(),
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
