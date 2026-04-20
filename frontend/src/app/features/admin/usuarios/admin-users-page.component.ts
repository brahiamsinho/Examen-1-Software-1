import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AdminApiService } from '@features/admin/admin-api.service';
import type { RolAdminDto, UsuarioAdminDto } from '@features/admin/admin.models';

@Component({
  selector: 'app-admin-users-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-users-page.component.html',
  styleUrl: './admin-users-page.component.scss',
})
export class AdminUsersPageComponent implements OnInit {
  private readonly api = inject(AdminApiService);

  usuarios: UsuarioAdminDto[] = [];
  roles: RolAdminDto[] = [];
  page = 0;
  size = 15;
  totalPages = 0;
  totalElements = 0;
  loading = false;
  errorMsg = '';

  showCreate = false;
  create = {
    correo: '',
    contrasena: '',
    nombres: '',
    apellidos: '',
    telefono: '',
    rolCodigo: 'CLIENTE',
    estado: true,
  };

  editing: UsuarioAdminDto | null = null;
  editDraft = {
    nombres: '',
    apellidos: '',
    telefono: '',
    rolCodigo: '',
    estado: true,
    contrasena: '',
  };

  ngOnInit(): void {
    this.api.listRoles().subscribe({
      next: (r) => (this.roles = r),
      error: (e) => (this.errorMsg = this.msg(e)),
    });
    this.load();
  }

  load(): void {
    this.loading = true;
    this.errorMsg = '';
    this.api.listUsuarios(this.page, this.size).subscribe({
      next: (res) => {
        this.usuarios = res.content;
        this.totalPages = res.totalPages;
        this.totalElements = res.totalElements;
        this.loading = false;
      },
      error: (e) => {
        this.loading = false;
        this.errorMsg = this.msg(e);
      },
    });
  }

  prev(): void {
    if (this.page > 0) {
      this.page--;
      this.load();
    }
  }

  next(): void {
    if (this.page < this.totalPages - 1) {
      this.page++;
      this.load();
    }
  }

  toggleCreate(): void {
    this.showCreate = !this.showCreate;
  }

  submitCreate(): void {
    this.errorMsg = '';
    this.api.createUsuario(this.create).subscribe({
      next: () => {
        this.showCreate = false;
        this.create = {
          correo: '',
          contrasena: '',
          nombres: '',
          apellidos: '',
          telefono: '',
          rolCodigo: 'CLIENTE',
          estado: true,
        };
        this.page = 0;
        this.load();
      },
      error: (e) => (this.errorMsg = this.msg(e)),
    });
  }

  startEdit(u: UsuarioAdminDto): void {
    this.editing = u;
    this.editDraft = {
      nombres: u.nombres,
      apellidos: u.apellidos,
      telefono: u.telefono ?? '',
      rolCodigo: u.rolCodigo,
      estado: u.estado,
      contrasena: '',
    };
  }

  cancelEdit(): void {
    this.editing = null;
  }

  saveEdit(): void {
    if (!this.editing) {
      return;
    }
    const body: Record<string, string | boolean> = {
      nombres: this.editDraft.nombres,
      apellidos: this.editDraft.apellidos,
      telefono: this.editDraft.telefono,
      rolCodigo: this.editDraft.rolCodigo,
      estado: this.editDraft.estado,
    };
    if (this.editDraft.contrasena.trim().length > 0) {
      body['contrasena'] = this.editDraft.contrasena;
    }
    this.api.patchUsuario(this.editing.id, body).subscribe({
      next: () => {
        this.editing = null;
        this.load();
      },
      error: (e) => (this.errorMsg = this.msg(e)),
    });
  }

  remove(u: UsuarioAdminDto): void {
    if (!window.confirm(`¿Eliminar usuario ${u.correo}?`)) {
      return;
    }
    this.api.deleteUsuario(u.id).subscribe({
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
