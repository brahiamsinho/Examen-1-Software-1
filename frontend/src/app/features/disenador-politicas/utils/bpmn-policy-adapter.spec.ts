import type { PoliticaNegocioDto } from '@features/disenador-politicas/models/politica-negocio.model';
import { bpmnXmlToPolicyUpsertBody, policyToBpmnXml, validateBpmnXml } from './bpmn-policy-adapter';

describe('bpmn-policy-adapter', () => {
  const base: PoliticaNegocioDto = {
    id: 'p1',
    nombre: 'Política ensayo',
    descripcion: 'desc',
    version: 1,
    lockVersion: 0,
    estado: 'BORRADOR',
    fechaCreacion: '2026-01-01T00:00:00Z',
    bpmnXml: null,
    nodos: [
      {
        idNodo: 'n_inicio',
        nombre: 'Inicio',
        tipoNodo: 'INICIO',
        orden: 0,
        esInicial: true,
        esFinal: false,
        areaId: null,
        asignacionesResponsable: [],
        carrilBpmn: 'Cliente',
        formularioExternoUrl: null,
      },
      {
        idNodo: 'n_tarea',
        nombre: 'Realizar pedido',
        tipoNodo: 'ACTIVIDAD',
        orden: 1,
        esInicial: false,
        esFinal: false,
        areaId: null,
        asignacionesResponsable: [],
        carrilBpmn: 'Administrativa',
        formularioExternoUrl: 'https://docs.google.com/forms/d/e/demo',
      },
      {
        idNodo: 'n_fin',
        nombre: 'Fin',
        tipoNodo: 'FIN',
        orden: 2,
        esInicial: false,
        esFinal: true,
        areaId: null,
        asignacionesResponsable: [],
        carrilBpmn: 'Cliente',
        formularioExternoUrl: null,
      },
    ],
    conexiones: [
      {
        idConexion: 'c1',
        tipoFlujo: 'SECUENCIAL',
        condicion: null,
        origenNodoId: 'n_inicio',
        destinoNodoId: 'n_tarea',
      },
      {
        idConexion: 'c2',
        tipoFlujo: 'SECUENCIAL',
        condicion: null,
        origenNodoId: 'n_tarea',
        destinoNodoId: 'n_fin',
      },
    ],
  };

  it('genera XML BPMN desde política y vuelve a body upsert', () => {
    const xml = policyToBpmnXml(base);
    expect(xml).toContain('<bpmn:process');
    expect(xml).toContain('<bpmndi:BPMNDiagram');
    const body = bpmnXmlToPolicyUpsertBody(xml, base);
    expect(body.bpmnXml).toContain('<bpmn:process');
    expect(body.nodos.length).toBeGreaterThan(0);
    const task = body.nodos.find((n) => n.idNodo === 'n_tarea');
    expect(task?.formularioExternoUrl).toContain('https://');
  });

  it('reporta error si no hay start event', () => {
    const badXml = `<?xml version="1.0" encoding="UTF-8"?>
      <bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL">
        <bpmn:process id="P1">
          <bpmn:endEvent id="e1" />
        </bpmn:process>
      </bpmn:definitions>`;
    const issues = validateBpmnXml(badXml);
    expect(issues.some((i) => i.message.includes('StartEvent'))).toBeTrue();
  });
});

