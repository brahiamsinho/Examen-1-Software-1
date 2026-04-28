import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-bpmn-toolbar',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="bpmn-toolbar">
      <button type="button" class="admin-secondary-btn" (click)="newRequested.emit()">Nuevo BPMN</button>
      <button type="button" class="admin-secondary-btn" (click)="lanesRequested.emit()">Carriles por departamento</button>
      <button type="button" class="admin-secondary-btn" (click)="importRequested.emit()">Importar XML</button>
      <button type="button" class="admin-secondary-btn" (click)="exportRequested.emit()">Exportar XML</button>
      <button type="button" class="admin-secondary-btn" (click)="validateRequested.emit()">Validar</button>
      <button type="button" class="admin-primary-btn" [disabled]="saving" (click)="saveRequested.emit()">
        {{ saving ? 'Guardando…' : 'Guardar política BPMN' }}
      </button>
    </div>
  `,
  styles: [
    `
      .bpmn-toolbar {
        display: flex;
        flex-wrap: wrap;
        gap: 0.55rem;
        margin-bottom: 0.75rem;
        padding: 0.75rem;
        border-radius: 12px;
        border: 1px solid #dbeafe;
        background: linear-gradient(180deg, #f8fbff 0%, #eef6ff 100%);
      }
    `,
  ],
})
export class BpmnToolbarComponent {
  @Input() saving = false;
  @Output() readonly newRequested = new EventEmitter<void>();
  @Output() readonly lanesRequested = new EventEmitter<void>();
  @Output() readonly importRequested = new EventEmitter<void>();
  @Output() readonly exportRequested = new EventEmitter<void>();
  @Output() readonly validateRequested = new EventEmitter<void>();
  @Output() readonly saveRequested = new EventEmitter<void>();
}

