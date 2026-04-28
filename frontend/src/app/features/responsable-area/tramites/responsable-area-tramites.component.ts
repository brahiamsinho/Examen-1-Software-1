import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { AuthService } from '@core/auth/auth.service';
import { ResponsableAreaContextService } from '@features/responsable-area/data/responsable-area-context.service';
import { TramitesApiService } from '@features/responsable-area/data/tramites-api.service';
import type { FlujoSalidasDto, SalidaFlujoDto } from '@features/responsable-area/models/salida-flujo.model';
import {
  TRAMITE_ESTADOS,
  TRAMITE_PRIORIDADES,
  type TramiteDto,
} from '@features/responsable-area/models/tramite.model';

type VistaBandeja = 'todos' | 'cola';

@Component({
  selector: 'app-responsable-area-tramites',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './responsable-area-tramites.component.html',
  styleUrl: './responsable-area-tramites.component.scss',
})
export class ResponsableAreaTramitesComponent implements OnInit {
  private readonly api = inject(TramitesApiService);
  readonly ctx = inject(ResponsableAreaContextService);
  readonly auth = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);

  readonly estados = TRAMITE_ESTADOS;
  readonly prioridades = TRAMITE_PRIORIDADES;

  vista: VistaBandeja = 'todos';
  tramites: TramiteDto[] = [];
  page = 0;
  size = 15;
  totalPages = 0;
  totalElements = 0;
  loading = false;
  errorMsg = '';

  colaEstado = 'EN_PROCESO';
  colaPrioridad = '';

  /** Panel de acciones de flujo (salidas / paralelo). */
  flujoTramite: TramiteDto | null = null;
  flujoSalidas: FlujoSalidasDto | null = null;
  salidas: SalidaFlujoDto[] = [];
  loadingFlujo = false;
  flujoError = '';
  flujoObs = '';
  workingKey = '';

  ngOnInit(): void {
    if (this.ctx.context()?.tieneArea && !this.ctx.loading()) {
      this.load();
    }
    this.ctx.afterContextLoad$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      if (this.ctx.context()?.tieneArea) {
        this.load();
      } else {
        this.tramites = [];
        this.totalElements = 0;
        this.totalPages = 0;
        this.loading = false;
      }
    });
  }

  setVista(v: VistaBandeja): void {
    this.vista = v;
    this.page = 0;
    this.errorMsg = '';
    this.load();
  }

  load(): void {
    if (!this.ctx.context()?.tieneArea) {
      this.tramites = [];
      this.totalElements = 0;
      this.totalPages = 0;
      this.loading = false;
      return;
    }
    this.loading = true;
    this.errorMsg = '';
    if (this.vista === 'cola') {
      this.api.colaFifo(this.colaEstado, this.colaPrioridad || null).subscribe({
        next: (rows) => {
          this.tramites = rows;
          this.totalElements = rows.length;
          this.totalPages = 1;
          this.loading = false;
        },
        error: (e) => {
          this.loading = false;
          this.errorMsg = this.msg(e);
          this.tramites = [];
        },
      });
      return;
    }
    this.api.list(this.page, this.size).subscribe({
      next: (res) => {
        this.tramites = res.content;
        this.totalPages = res.totalPages;
        this.totalElements = res.totalElements;
        this.loading = false;
      },
      error: (e) => {
        this.loading = false;
        this.errorMsg = this.msg(e);
        this.tramites = [];
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

  puedeOperarFlujo(t: TramiteDto): boolean {
    if (!t.politicaId) {
      return false;
    }
    if (this.auth.isAdministrador()) {
      return true;
    }
    const c = this.ctx.context();
    if (!c?.tieneArea || !c.areaId) {
      return false;
    }
    return c.areaId === t.areaActualId;
  }

  openFlujo(t: TramiteDto): void {
    this.flujoTramite = t;
    this.flujoSalidas = null;
    this.salidas = [];
    this.flujoError = '';
    this.flujoObs = '';
    this.loadingFlujo = true;
    this.api.getSalidas(t.id).subscribe({
      next: (resp) => {
        this.flujoSalidas = resp;
        this.salidas = resp.salidas ?? [];
        this.loadingFlujo = false;
      },
      error: (e) => {
        this.loadingFlujo = false;
        this.flujoError = this.msg(e);
      },
    });
  }

  closeFlujo(): void {
    this.flujoTramite = null;
    this.flujoSalidas = null;
    this.salidas = [];
    this.flujoError = '';
    this.flujoObs = '';
    this.loadingFlujo = false;
    this.workingKey = '';
  }

  salidasParalelas(): SalidaFlujoDto[] {
    return this.salidas.filter((s) => (s.tipoFlujo || '').toUpperCase().trim() === 'PARALELO');
  }

  esParaleloMulti(): boolean {
    return this.salidasParalelas().length >= 2;
  }

  avanzar(s: SalidaFlujoDto): void {
    if (!this.flujoTramite) {
      return;
    }
    this.workingKey = `a:${s.idConexion}`;
    this.flujoError = '';
    this.api.avanzarFlujo(this.flujoTramite.id, s.idConexion, this.flujoObs).subscribe({
      next: () => {
        this.workingKey = '';
        this.closeFlujo();
        this.load();
      },
      error: (e) => {
        this.workingKey = '';
        this.flujoError = this.msg(e);
      },
    });
  }

  aprobarParalelo(nodoRamaId: string): void {
    if (!this.flujoTramite) {
      return;
    }
    this.workingKey = `p:${nodoRamaId}`;
    this.flujoError = '';
    this.api.aprobarRamaParalela(this.flujoTramite.id, nodoRamaId).subscribe({
      next: () => {
        this.workingKey = '';
        this.closeFlujo();
        this.load();
      },
      error: (e) => {
        this.workingKey = '';
        this.flujoError = this.msg(e);
      },
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
