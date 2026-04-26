import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { PoliticasApiService } from '@features/disenador-politicas/data/politicas-api.service';
import type { PoliticaNegocioDto } from '@features/disenador-politicas/models/politica-negocio.model';
import { PlanificadorApiService } from '@features/planificador/data/planificador-api.service';
import type { TramiteDto } from '@features/responsable-area/models/tramite.model';

@Component({
  selector: 'app-planificador-pendientes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './planificador-pendientes.component.html',
  styleUrl: './planificador-pendientes.component.scss',
})
export class PlanificadorPendientesComponent implements OnInit {
  private readonly planApi = inject(PlanificadorApiService);
  private readonly polApi = inject(PoliticasApiService);
  private readonly destroyRef = inject(DestroyRef);

  tramites: TramiteDto[] = [];
  politicas: PoliticaNegocioDto[] = [];
  /** politicaId elegida por fila (tramite.id) */
  seleccionPolitica: Record<string, string> = {};
  loading = false;
  errorMsg = '';
  page = 0;
  size = 15;
  totalPages = 0;
  totalElements = 0;
  assigning: Record<string, boolean> = {};

  ngOnInit(): void {
    this.loadPoliticas();
    this.loadTramites();
  }

  loadPoliticas(): void {
    this.polApi.list(0, 100).subscribe({
      next: (p) => {
        this.politicas = p.content;
      },
      error: () => {
        this.politicas = [];
      },
    });
  }

  loadTramites(): void {
    this.loading = true;
    this.errorMsg = '';
    this.planApi
      .listarPendientesPolitica(this.page, this.size)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.tramites = res.content;
          this.totalPages = res.totalPages;
          this.totalElements = res.totalElements;
          this.loading = false;
          for (const t of this.tramites) {
            if (this.seleccionPolitica[t.id] == null && this.politicas.length > 0) {
              this.seleccionPolitica[t.id] = this.politicas[0].id;
            }
          }
        },
        error: (e) => {
          this.loading = false;
          this.errorMsg = this.msg(e);
          this.tramites = [];
        },
      });
  }

  setPage(p: number): void {
    this.page = Math.max(0, Math.min(p, Math.max(0, this.totalPages - 1)));
    this.loadTramites();
  }

  asignar(t: TramiteDto): void {
    const pid = this.seleccionPolitica[t.id];
    if (!pid) {
      this.errorMsg = 'Elegí una política para el trámite ' + t.codigo + '.';
      return;
    }
    this.assigning[t.id] = true;
    this.errorMsg = '';
    this.planApi.asignarPolitica(t.id, pid).subscribe({
      next: () => {
        this.assigning[t.id] = false;
        delete this.seleccionPolitica[t.id];
        this.loadTramites();
      },
      error: (e) => {
        this.assigning[t.id] = false;
        this.errorMsg = this.msg(e);
      },
    });
  }

  private msg(e: unknown): string {
    if (e instanceof HttpErrorResponse) {
      const body = e.error as { message?: string } | null;
      if (body?.message) return body.message;
      return `Error ${e.status}`;
    }
    return 'Error de red';
  }
}
