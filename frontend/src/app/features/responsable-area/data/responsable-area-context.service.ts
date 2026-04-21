import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Subject } from 'rxjs';
import { environment } from '@environments/environment';
import type { ResponsableAreaContextDto } from '@features/responsable-area/data/responsable-area-context.model';

@Injectable({ providedIn: 'root' })
export class ResponsableAreaContextService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiBackendUrl.replace(/\/$/, '')}/api/seguridad/responsable/contexto`;
  private readonly contextLoaded = new Subject<void>();

  /** Emite cuando terminó un intento de carga del contexto (éxito o error). */
  readonly afterContextLoad$ = this.contextLoaded.asObservable();

  readonly context = signal<ResponsableAreaContextDto | null>(null);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  refresh(): void {
    this.loading.set(true);
    this.error.set(null);
    this.http.get<ResponsableAreaContextDto>(this.url).subscribe({
      next: (c) => {
        this.context.set(c);
        this.loading.set(false);
        this.contextLoaded.next();
      },
      error: () => {
        this.context.set(null);
        this.loading.set(false);
        this.error.set('No se pudo cargar tu área y políticas. Intentá actualizar la página.');
        this.contextLoaded.next();
      },
    });
  }
}
