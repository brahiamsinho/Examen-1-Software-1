import { Component, OnInit, inject } from '@angular/core';
import { ApiService } from '@core/services/api.service';
import type { HealthResponseDto } from '@core/models/health-response.model';

type ServiceProbeState = 'idle' | 'loading' | 'ok' | 'error';

interface ModulePreview {
  readonly title: string;
  readonly description: string;
  readonly tag: string;
}

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [],
  templateUrl: './dashboard-page.component.html',
})
export class DashboardPageComponent implements OnInit {
  private readonly api = inject(ApiService);

  springState: ServiceProbeState = 'idle';
  fastApiState: ServiceProbeState = 'idle';
  springDetail: HealthResponseDto | null = null;
  fastApiDetail: HealthResponseDto | null = null;
  readonly modules: readonly ModulePreview[] = [
    {
      title: 'Seguridad',
      description: 'Autenticacion, roles y control de acceso para portales internos y cliente movil.',
      tag: 'Portal y JWT',
    },
    {
      title: 'Politicas',
      description: 'Reglas de negocio y criterios de priorizacion para decisiones del flujo.',
      tag: 'Reglas activas',
    },
    {
      title: 'Tramites',
      description: 'Gestion del ciclo de vida de solicitudes y sus estados operativos.',
      tag: 'Core de negocio',
    },
    {
      title: 'Documentos',
      description: 'Carga, validacion y trazabilidad de archivos asociados a cada tramite.',
      tag: 'Expediente digital',
    },
    {
      title: 'Seguimiento',
      description: 'Linea de tiempo del recorrido por areas y responsables involucrados.',
      tag: 'Trazabilidad',
    },
    {
      title: 'Analitica',
      description: 'Indicadores de desempeno para cuellos de botella y decisiones de mejora.',
      tag: 'Insight operativo',
    },
  ];

  ngOnInit(): void {
    this.probeServices();
  }

  probeServices(): void {
    this.springState = 'loading';
    this.fastApiState = 'loading';
    this.springDetail = null;
    this.fastApiDetail = null;

    this.api.checkBackendHealth().subscribe({
      next: (res) => {
        this.springDetail = res;
        this.springState = 'ok';
      },
      error: () => {
        this.springState = 'error';
      },
    });

    this.api.checkFastApiHealth().subscribe({
      next: (res) => {
        this.fastApiDetail = res;
        this.fastApiState = 'ok';
      },
      error: () => {
        this.fastApiState = 'error';
      },
    });
  }

  getProbeLabel(state: ServiceProbeState): string {
    if (state === 'loading') {
      return 'Comprobando...';
    }
    if (state === 'ok') {
      return 'Disponible';
    }
    if (state === 'error') {
      return 'No disponible';
    }
    return 'Sin revisar';
  }
}
