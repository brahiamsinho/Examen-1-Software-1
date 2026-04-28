import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import type { BpmnValidationIssue } from '@features/disenador-politicas/utils/bpmn-policy-adapter';

@Component({
  selector: 'app-bpmn-validation-panel',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section class="bpmn-validation">
      <h3 class="bpmn-validation-title">Validación BPMN</h3>
      @if (!issues.length) {
        <p class="bpmn-validation-ok">Sin hallazgos. El diagrama cumple validaciones base.</p>
      } @else {
        <ul class="bpmn-validation-list">
          @for (i of issues; track $index) {
            <li [class.bpmn-validation-item--error]="i.level === 'error'" class="bpmn-validation-item">
              <strong>{{ i.level === 'error' ? 'Error' : 'Aviso' }}:</strong> {{ i.message }}
            </li>
          }
        </ul>
      }
    </section>
  `,
  styles: [
    `
      .bpmn-validation {
        border: 1px solid #e2e8f0;
        border-radius: 12px;
        background: #fff;
        padding: 0.75rem 0.9rem;
      }
      .bpmn-validation-title {
        margin: 0 0 0.5rem;
        color: #1e1b4b;
        font-size: 0.95rem;
      }
      .bpmn-validation-ok {
        margin: 0;
        color: #166534;
        font-size: 0.86rem;
      }
      .bpmn-validation-list {
        margin: 0;
        padding-left: 1rem;
      }
      .bpmn-validation-item {
        color: #334155;
        font-size: 0.84rem;
        margin: 0.25rem 0;
      }
      .bpmn-validation-item--error {
        color: #991b1b;
      }
    `,
  ],
})
export class BpmnValidationPanelComponent {
  @Input() issues: BpmnValidationIssue[] = [];
}

