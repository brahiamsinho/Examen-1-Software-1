import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgFor, NgIf, NgClass } from '@angular/common';
import {
  CuellosBotellaApiService,
  CuelloBotellaSignal,
} from './cuellos-botella-api.service';

@Component({
  selector: 'app-cuellos-botella-dashboard',
  standalone: true,
  imports: [FormsModule, NgFor, NgIf, NgClass],
  template: `
    <div class="cuellos-card">
      <h3>Deteccion de Cuellos de Botella</h3>
      <p class="desc">Analiza tiempos de recorrido por nodo usando IA.</p>

      <div class="input-row">
        <input
          [(ngModel)]="politicaId"
          placeholder="ID de politica (ej: 507f1f77bcf86cd799439011)"
          class="pid-input"
        />
        <button (click)="analizar()" [disabled]="loading || !politicaId.trim()">
          {{ loading ? 'Analizando...' : 'Analizar' }}
        </button>
      </div>

      <div *ngIf="error" class="error-msg">{{ error }}</div>

      <div *ngIf="result" class="results">
        <p class="summary">{{ result.summary }}</p>

        <table *ngIf="result.signals.length > 0">
          <thead>
            <tr>
              <th>Severidad</th>
              <th>Detalle</th>
            </tr>
          </thead>
          <tbody>
            <tr
              *ngFor="let s of result.signals"
              [ngClass]="{
                'row-critical': s.severity === 'critical',
                'row-warning': s.severity === 'warning'
              }"
            >
              <td>
                <span class="badge" [ngClass]="'badge-' + s.severity">
                  {{ s.severity === 'critical' ? 'CRITICA' : 'ADVERTENCIA' }}
                </span>
              </td>
              <td>{{ s.detail }}</td>
            </tr>
          </tbody>
        </table>

        <p *ngIf="result.signals.length === 0" class="clean">
          No se detectaron cuellos de botella.
        </p>
      </div>
    </div>
  `,
  styles: [`
    .cuellos-card {
      background: #fff;
      border-radius: 8px;
      padding: 20px;
      margin-top: 16px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.1);
    }
    h3 { margin: 0 0 4px 0; }
    .desc { color: #666; font-size: 14px; margin: 0 0 12px 0; }
    .input-row { display: flex; gap: 8px; }
    .pid-input { flex: 1; padding: 8px 12px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px; }
    button {
      padding: 8px 20px;
      background: #1976d2;
      color: #fff;
      border: none;
      border-radius: 4px;
      cursor: pointer;
    }
    button:disabled { opacity: 0.5; cursor: not-allowed; }
    .error-msg { color: #d32f2f; margin-top: 8px; font-size: 14px; }
    .results { margin-top: 16px; }
    .summary { font-size: 14px; color: #555; }
    .clean { color: #2e7d32; font-size: 14px; }
    table { width: 100%; border-collapse: collapse; margin-top: 8px; }
    th { text-align: left; padding: 8px; background: #f5f5f5; font-size: 13px; }
    td { padding: 8px; border-bottom: 1px solid #eee; font-size: 13px; }
    .row-critical { background: #fff5f5; }
    .row-warning { background: #fffde7; }
    .badge {
      padding: 2px 10px;
      border-radius: 12px;
      font-size: 12px;
      font-weight: 600;
    }
    .badge-critical { background: #d32f2f; color: #fff; }
    .badge-warning { background: #f57c00; color: #fff; }
  `],
})
export class CuellosBotellaDashboardComponent {
  private readonly api = inject(CuellosBotellaApiService);

  politicaId = '';
  loading = false;
  error = '';
  result: { status: string; signals: CuelloBotellaSignal[]; summary: string } | null = null;

  analizar(): void {
    if (!this.politicaId.trim()) return;
    this.loading = true;
    this.error = '';
    this.result = null;
    this.api.analizar(this.politicaId.trim()).subscribe({
      next: (res) => {
        this.result = res;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error al analizar: ' + (err?.error?.mensaje || err?.message || 'Servicio no disponible');
        this.loading = false;
      },
    });
  }
}
