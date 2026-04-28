import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import type { AreaDto } from '@features/disenador-politicas/models/area.model';
import type { UsuarioAreaDto } from '@features/disenador-politicas/models/usuario-area.model';

export interface BpmnBusinessProperties {
  areaId: string | null;
  responsableId: string | null;
  carrilBpmn: string | null;
  formularioExternoUrl: string | null;
}

@Component({
  selector: 'app-bpmn-properties-panel',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <aside class="bpmn-props">
      <h3 class="bpmn-props-title">Propiedades</h3>
      <p class="bpmn-props-kicker">{{ selectedElementLabel || 'Sin selección' }}</p>

      @if (isTaskLikeSelected) {
        <div class="admin-form-grid bpmn-props-fields">
          <label class="admin-field">
            <span>Carril BPMN (Departamento)</span>
            <input [(ngModel)]="localCarrilBpmn" maxlength="160" placeholder="Se autocompleta según área" />
          </label>
          <label class="admin-field">
            <span>Formulario externo (HTTPS)</span>
            <input
              type="url"
              [(ngModel)]="localFormularioUrl"
              maxlength="2048"
              placeholder="https://docs.google.com/forms/..."
            />
          </label>
          <label class="admin-field">
            <span>Área</span>
            <select [(ngModel)]="localAreaId" (change)="areaChanged.emit(localAreaId || null)">
              <option value="">— Sin área —</option>
              @for (a of areas; track a.id) {
                <option [value]="a.id">{{ a.nombre }}</option>
              }
            </select>
          </label>
          <label class="admin-field">
            <span>Responsable</span>
            <select [(ngModel)]="localResponsableId" [disabled]="!localAreaId">
              <option value="">— Sin responsable —</option>
              @for (u of usuariosDelArea; track u.id) {
                <option [value]="u.id">{{ u.nombres }} {{ u.apellidos }} — {{ u.correo }}</option>
              }
            </select>
          </label>
        </div>
        <button type="button" class="admin-primary-btn bpmn-props-save" (click)="emitApply()">Aplicar propiedades</button>
      } @else {
        <p class="bpmn-props-empty">Seleccioná un nodo de tipo tarea/gateway/evento para editar metadatos de negocio.</p>
      }
    </aside>
  `,
  styles: [
    `
      .bpmn-props {
        border: 1px solid #dbeafe;
        border-radius: 14px;
        padding: 1rem;
        background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
        box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
      }
      .bpmn-props-title {
        margin: 0;
        color: #1d4ed8;
        font-size: 1rem;
      }
      .bpmn-props-kicker {
        margin: 0.3rem 0 0.7rem;
        font-size: 0.78rem;
        color: #0f766e;
      }
      .bpmn-props-fields {
        margin-bottom: 0.75rem;
      }
      .bpmn-props-save {
        width: 100%;
      }
      .bpmn-props-empty {
        margin: 0;
        color: #64748b;
        font-size: 0.88rem;
      }
    `,
  ],
})
export class BpmnPropertiesPanelComponent {
  @Input() selectedElementLabel = '';
  @Input() isTaskLikeSelected = false;
  @Input() areas: AreaDto[] = [];
  @Input() usuariosDelArea: UsuarioAreaDto[] = [];
  @Input() set props(value: BpmnBusinessProperties | null) {
    this.localAreaId = value?.areaId ?? '';
    this.localResponsableId = value?.responsableId ?? '';
    this.localCarrilBpmn = value?.carrilBpmn ?? '';
    this.localFormularioUrl = value?.formularioExternoUrl ?? '';
  }

  @Output() readonly areaChanged = new EventEmitter<string | null>();
  @Output() readonly apply = new EventEmitter<BpmnBusinessProperties>();

  localAreaId = '';
  localResponsableId = '';
  localCarrilBpmn = '';
  localFormularioUrl = '';

  emitApply(): void {
    this.apply.emit({
      areaId: this.localAreaId || null,
      responsableId: this.localResponsableId || null,
      carrilBpmn: this.localCarrilBpmn.trim() || null,
      formularioExternoUrl: this.localFormularioUrl.trim() || null,
    });
  }
}

