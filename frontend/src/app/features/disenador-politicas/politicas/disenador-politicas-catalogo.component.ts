import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { PoliticasApiService } from '@features/disenador-politicas/data/politicas-api.service';
import {
  buildDefaultPoliticaUpsert,
  type PoliticaNegocioDto,
} from '@features/disenador-politicas/models/politica-negocio.model';

@Component({
  selector: 'app-disenador-politicas-catalogo',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './disenador-politicas-catalogo.component.html',
  styleUrl: './disenador-politicas-catalogo.component.scss',
})
export class DisenadorPoliticasCatalogoComponent implements OnInit {
  private readonly api = inject(PoliticasApiService);
  private readonly router = inject(Router);

  politicas: PoliticaNegocioDto[] = [];
  page = 0;
  size = 15;
  totalPages = 0;
  totalElements = 0;
  loading = false;
  errorMsg = '';

  showCreate = false;
  create = {
    nombre: '',
    descripcion: '',
    version: 1,
    estado: 'BORRADOR',
  };

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.errorMsg = '';
    this.api.list(this.page, this.size).subscribe({
      next: (res) => {
        this.politicas = res.content;
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
    if (!this.create.nombre.trim() || !this.create.descripcion.trim()) {
      this.errorMsg = 'Nombre y descripción son obligatorios.';
      return;
    }
    this.errorMsg = '';
    const body = buildDefaultPoliticaUpsert(
      this.create.nombre,
      this.create.descripcion,
      this.create.version,
      this.create.estado,
    );
    this.api.create(body).subscribe({
      next: (p) => {
        this.showCreate = false;
        this.create = { nombre: '', descripcion: '', version: 1, estado: 'BORRADOR' };
        this.page = 0;
        this.load();
        void this.router.navigate(['/disenador/modelado'], { queryParams: { politicaId: p.id } });
      },
      error: (e) => (this.errorMsg = this.msg(e)),
    });
  }

  remove(p: PoliticaNegocioDto): void {
    if (!window.confirm(`¿Eliminar la política «${p.nombre}» (v${p.version})?`)) {
      return;
    }
    this.api.delete(p.id).subscribe({
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
