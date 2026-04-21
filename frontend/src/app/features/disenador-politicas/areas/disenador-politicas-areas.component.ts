import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AreasApiService } from '@features/disenador-politicas/data/areas-api.service';
import type { AreaDto } from '@features/disenador-politicas/models/area.model';

@Component({
  selector: 'app-disenador-politicas-areas',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './disenador-politicas-areas.component.html',
  styleUrl: './disenador-politicas-areas.component.scss',
})
export class DisenadorPoliticasAreasComponent implements OnInit {
  private readonly api = inject(AreasApiService);

  areas: AreaDto[] = [];
  loading = false;
  errorMsg = '';

  showCreate = false;
  create = { nombre: '', descripcion: '', estado: true };

  editing: AreaDto | null = null;
  editDraft = { nombre: '', descripcion: '', estado: true };

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.errorMsg = '';
    this.api.list().subscribe({
      next: (list) => {
        this.areas = [...list].sort((a, b) => a.nombre.localeCompare(b.nombre, 'es'));
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
    const nombre = this.create.nombre.trim();
    const descripcion = this.create.descripcion.trim();
    if (!nombre || !descripcion) {
      this.errorMsg = 'Nombre y descripción son obligatorios.';
      return;
    }
    this.errorMsg = '';
    this.api.crear({ nombre, descripcion, estado: this.create.estado }).subscribe({
      next: () => {
        this.showCreate = false;
        this.create = { nombre: '', descripcion: '', estado: true };
        this.load();
      },
      error: (e) => (this.errorMsg = this.msg(e)),
    });
  }

  startEdit(a: AreaDto): void {
    this.editing = a;
    this.editDraft = { nombre: a.nombre, descripcion: a.descripcion, estado: a.estado };
  }

  cancelEdit(): void {
    this.editing = null;
  }

  saveEdit(): void {
    if (!this.editing) {
      return;
    }
    const nombre = this.editDraft.nombre.trim();
    const descripcion = this.editDraft.descripcion.trim();
    if (!nombre || !descripcion) {
      this.errorMsg = 'Nombre y descripción son obligatorios.';
      return;
    }
    this.errorMsg = '';
    this.api
      .actualizar(this.editing.id, { nombre, descripcion, estado: this.editDraft.estado })
      .subscribe({
        next: () => {
          this.editing = null;
          this.load();
        },
        error: (e) => (this.errorMsg = this.msg(e)),
      });
  }

  remove(a: AreaDto): void {
    if (!window.confirm(`¿Eliminar el área «${a.nombre}»? Si hay usuarios o nodos asociados, revisá el impacto.`)) {
      return;
    }
    this.api.eliminar(a.id).subscribe({
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
