import type {
  PoliticaAsignacionDto,
  PoliticaConexionDto,
  PoliticaNegocioDto,
  PoliticaUpsertBody,
} from '@features/disenador-politicas/models/politica-negocio.model';

const BPMN_NS = 'http://www.omg.org/spec/BPMN/20100524/MODEL';
const BPMNDI_NS = 'http://www.omg.org/spec/BPMN/20100524/DI';
const OMGDC_NS = 'http://www.omg.org/spec/DD/20100524/DC';
const OMGDI_NS = 'http://www.omg.org/spec/DD/20100524/DI';

type NodeType = 'INICIO' | 'ACTIVIDAD' | 'DECISION' | 'PARALELO' | 'FIN' | 'RECHAZO';

interface NodeMeta {
  areaId?: string | null;
  carrilBpmn?: string | null;
  formularioExternoUrl?: string | null;
  asignacionesResponsable?: PoliticaAsignacionDto[];
}

interface ParsedNode {
  id: string;
  name: string;
  type: NodeType;
  x: number;
  y: number;
  meta: NodeMeta;
}

interface DiagramBounds {
  x: number;
  y: number;
  width: number;
  height: number;
}

const DEFAULT_BPMN_XML = `<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
  xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
  xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
  id="Definitions_1"
  targetNamespace="http://tramites.local/bpmn">
  <bpmn:process id="Process_1" isExecutable="false">
    <bpmn:startEvent id="n_inicio" name="Inicio" />
    <bpmn:userTask id="n_actividad" name="Actividad (definir departamento)" />
    <bpmn:endEvent id="n_fin" name="Fin" />
    <bpmn:sequenceFlow id="c_1" sourceRef="n_inicio" targetRef="n_actividad" />
    <bpmn:sequenceFlow id="c_2" sourceRef="n_actividad" targetRef="n_fin" />
  </bpmn:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Process_1">
      <bpmndi:BPMNShape id="Shape_n_inicio" bpmnElement="n_inicio">
        <dc:Bounds x="140" y="220" width="36" height="36" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Shape_n_actividad" bpmnElement="n_actividad">
        <dc:Bounds x="250" y="195" width="140" height="90" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Shape_n_fin" bpmnElement="n_fin">
        <dc:Bounds x="470" y="220" width="36" height="36" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNEdge id="Edge_c_1" bpmnElement="c_1">
        <di:waypoint x="176" y="238" />
        <di:waypoint x="250" y="238" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Edge_c_2" bpmnElement="c_2">
        <di:waypoint x="390" y="238" />
        <di:waypoint x="470" y="238" />
      </bpmndi:BPMNEdge>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn:definitions>`;

export interface BpmnValidationIssue {
  level: 'error' | 'warning';
  message: string;
}

function parseDocumentationMeta(doc: Element | null): NodeMeta {
  if (!doc || !doc.textContent) {
    return {};
  }
  const raw = doc.textContent.trim();
  if (!raw) {
    return {};
  }
  try {
    const parsed = JSON.parse(raw) as NodeMeta;
    return {
      areaId: parsed.areaId ?? null,
      carrilBpmn: parsed.carrilBpmn ?? null,
      formularioExternoUrl: parsed.formularioExternoUrl ?? null,
      asignacionesResponsable: Array.isArray(parsed.asignacionesResponsable) ? parsed.asignacionesResponsable : [],
    };
  } catch {
    return {};
  }
}

function nodeTypeFromElement(el: Element): NodeType {
  const local = el.localName;
  if (local === 'startEvent') return 'INICIO';
  if (local === 'exclusiveGateway') return 'DECISION';
  if (local === 'parallelGateway') return 'PARALELO';
  if (local === 'endEvent') {
    const meta = parseDocumentationMeta(el.querySelector('documentation'));
    if ((meta.carrilBpmn ?? '').toUpperCase() === 'RECHAZO' || /rechazo/i.test(el.getAttribute('name') ?? '')) {
      return 'RECHAZO';
    }
    return 'FIN';
  }
  return 'ACTIVIDAD';
}

function ensureHttpsOrNull(value: string | null | undefined): string | null {
  const t = value?.trim();
  if (!t) return null;
  return t.startsWith('https://') ? t : null;
}

function normalizeTipoFlujo(sourceType: NodeType, condicion: string | null): string {
  if (sourceType === 'PARALELO') {
    return 'PARALELO';
  }
  if (condicion && condicion.trim()) {
    return 'ALTERNATIVO';
  }
  return 'SECUENCIAL';
}

function getShapeCenterMap(xmlDoc: XMLDocument): Map<string, { x: number; y: number }> {
  const map = new Map<string, { x: number; y: number }>();
  const shapes = Array.from(xmlDoc.getElementsByTagNameNS(BPMNDI_NS, 'BPMNShape'));
  for (const s of shapes) {
    const id = s.getAttribute('bpmnElement');
    if (!id) continue;
    const bounds = s.getElementsByTagNameNS(OMGDC_NS, 'Bounds').item(0);
    if (!bounds) continue;
    const x = Number(bounds.getAttribute('x') ?? '0');
    const y = Number(bounds.getAttribute('y') ?? '0');
    const w = Number(bounds.getAttribute('width') ?? '0');
    const h = Number(bounds.getAttribute('height') ?? '0');
    map.set(id, { x: x + w / 2, y: y + h / 2 });
  }
  return map;
}

export function policyToBpmnXml(politica: PoliticaNegocioDto): string {
  if (politica.bpmnXml && politica.bpmnXml.trim()) {
    return politica.bpmnXml;
  }
  const nodes = [...(politica.nodos ?? [])].sort((a, b) => a.orden - b.orden);
  if (!nodes.length) {
    return DEFAULT_BPMN_XML;
  }
  const escapeXml = (value: string): string =>
    value.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;').replace(/'/g, '&apos;');
  const nodeTagById = new Map<string, string>();
  const centers = new Map<string, { x: number; y: number }>();
  const shapeById = new Map<string, DiagramBounds>();
  const y = 220;
  nodes.forEach((n, idx) => {
    const tag =
      n.tipoNodo === 'INICIO'
        ? 'startEvent'
        : n.tipoNodo === 'DECISION'
          ? 'exclusiveGateway'
          : n.tipoNodo === 'PARALELO'
            ? 'parallelGateway'
            : n.tipoNodo === 'FIN' || n.tipoNodo === 'RECHAZO'
              ? 'endEvent'
              : 'userTask';
    nodeTagById.set(n.idNodo, tag);
    const width = tag === 'userTask' ? 140 : tag.includes('Gateway') ? 50 : 36;
    const height = tag === 'userTask' ? 90 : tag.includes('Gateway') ? 50 : 36;
    const x = 120 + idx * 180;
    shapeById.set(n.idNodo, { x, y: y - height / 2, width, height });
    centers.set(n.idNodo, { x: x + width / 2, y });
  });
  const nodeXml = nodes
    .map((n) => {
      const tag = nodeTagById.get(n.idNodo) ?? 'userTask';
      const meta: NodeMeta = {
        areaId: n.areaId ?? null,
        carrilBpmn: n.carrilBpmn ?? null,
        formularioExternoUrl: n.formularioExternoUrl ?? null,
        asignacionesResponsable: n.asignacionesResponsable ?? [],
      };
      const metaJson = JSON.stringify(meta).replace(/</g, '&lt;');
      return `    <bpmn:${tag} id="${escapeXml(n.idNodo)}" name="${escapeXml(n.nombre ?? '')}"><bpmn:documentation>${metaJson}</bpmn:documentation></bpmn:${tag}>`;
    })
    .join('\n');
  const conexiones = politica.conexiones ?? [];
  const flowXml = (politica.conexiones ?? [])
    .map((c) => {
      const cond = c.condicion?.trim()
        ? `<bpmn:conditionExpression xsi:type="bpmn:tFormalExpression">${escapeXml(c.condicion)}</bpmn:conditionExpression>`
        : '';
      return `    <bpmn:sequenceFlow id="${escapeXml(c.idConexion)}" sourceRef="${escapeXml(c.origenNodoId)}" targetRef="${escapeXml(c.destinoNodoId)}">${cond}</bpmn:sequenceFlow>`;
    })
    .join('\n');
  const shapesXml = nodes
    .map((n) => {
      const b = shapeById.get(n.idNodo);
      if (!b) return '';
      return `      <bpmndi:BPMNShape id="Shape_${escapeXml(n.idNodo)}" bpmnElement="${escapeXml(n.idNodo)}">
        <dc:Bounds x="${b.x}" y="${b.y}" width="${b.width}" height="${b.height}" />
      </bpmndi:BPMNShape>`;
    })
    .join('\n');
  const edgesXml = conexiones
    .map((c) => {
      const src = centers.get(c.origenNodoId);
      const dst = centers.get(c.destinoNodoId);
      if (!src || !dst) return '';
      return `      <bpmndi:BPMNEdge id="Edge_${escapeXml(c.idConexion)}" bpmnElement="${escapeXml(c.idConexion)}">
        <di:waypoint x="${src.x}" y="${src.y}" />
        <di:waypoint x="${dst.x}" y="${dst.y}" />
      </bpmndi:BPMNEdge>`;
    })
    .join('\n');
  return `<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:bpmn="${BPMN_NS}"
  xmlns:bpmndi="${BPMNDI_NS}"
  xmlns:dc="${OMGDC_NS}"
  xmlns:di="${OMGDI_NS}"
  id="Definitions_${politica.id}"
  targetNamespace="http://tramites.local/bpmn">
  <bpmn:process id="Process_${politica.id}" isExecutable="false">
${nodeXml}
${flowXml}
  </bpmn:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_${politica.id}">
    <bpmndi:BPMNPlane id="BPMNPlane_${politica.id}" bpmnElement="Process_${politica.id}">
${shapesXml}
${edgesXml}
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn:definitions>`;
}

export function bpmnXmlToPolicyUpsertBody(xml: string, politica: PoliticaNegocioDto): PoliticaUpsertBody {
  const xmlDoc = new DOMParser().parseFromString(xml, 'text/xml');
  const process = xmlDoc.getElementsByTagNameNS(BPMN_NS, 'process').item(0);
  if (!process) {
    throw new Error('El XML BPMN no contiene un <process>.');
  }
  const centers = getShapeCenterMap(xmlDoc);
  const nodes: ParsedNode[] = [];
  for (const el of Array.from(process.children)) {
    const local = el.localName;
    if (!['startEvent', 'userTask', 'task', 'serviceTask', 'exclusiveGateway', 'parallelGateway', 'endEvent'].includes(local)) {
      continue;
    }
    const id = el.getAttribute('id');
    if (!id) continue;
    const c = centers.get(id) ?? { x: nodes.length * 180 + 100, y: 220 };
    nodes.push({
      id,
      name: el.getAttribute('name')?.trim() || id,
      type: nodeTypeFromElement(el),
      x: c.x,
      y: c.y,
      meta: parseDocumentationMeta(el.querySelector('documentation')),
    });
  }
  nodes.sort((a, b) => a.y - b.y || a.x - b.x);
  const typeById = new Map(nodes.map((n) => [n.id, n.type]));
  const conexiones: PoliticaConexionDto[] = [];
  const flows = Array.from(process.getElementsByTagNameNS(BPMN_NS, 'sequenceFlow'));
  for (const f of flows) {
    const id = f.getAttribute('id');
    const src = f.getAttribute('sourceRef');
    const dst = f.getAttribute('targetRef');
    if (!id || !src || !dst) continue;
    const cond = f.getElementsByTagNameNS(BPMN_NS, 'conditionExpression').item(0)?.textContent?.trim() ?? null;
    conexiones.push({
      idConexion: id,
      origenNodoId: src,
      destinoNodoId: dst,
      condicion: cond,
      tipoFlujo: normalizeTipoFlujo(typeById.get(src) ?? 'ACTIVIDAD', cond),
    });
  }
  return {
    nombre: politica.nombre,
    descripcion: politica.descripcion,
    version: politica.version,
    lockVersion: politica.lockVersion ?? 0,
    estado: politica.estado,
    bpmnXml: xml,
    nodos: nodes.map((n, i) => ({
      idNodo: n.id,
      nombre: n.name,
      tipoNodo: n.type,
      orden: i,
      condicion: null,
      esInicial: n.type === 'INICIO',
      esFinal: n.type === 'FIN' || n.type === 'RECHAZO',
      areaId: n.meta.areaId ?? null,
      asignacionesResponsable: (n.meta.asignacionesResponsable ?? []).map((a) => ({
        usuarioId: a.usuarioId,
        areaId: a.areaId,
        fechaAsignacion: a.fechaAsignacion,
        estado: a.estado,
      })),
      formularioExternoUrl: ensureHttpsOrNull(n.meta.formularioExternoUrl),
      carrilBpmn: n.meta.carrilBpmn?.trim() || null,
    })),
    conexiones,
  };
}

export function validateBpmnXml(xml: string): BpmnValidationIssue[] {
  const issues: BpmnValidationIssue[] = [];
  const xmlDoc = new DOMParser().parseFromString(xml, 'text/xml');
  const process = xmlDoc.getElementsByTagNameNS(BPMN_NS, 'process').item(0);
  if (!process) {
    return [{ level: 'error', message: 'El BPMN no contiene proceso principal.' }];
  }
  const starts = process.getElementsByTagNameNS(BPMN_NS, 'startEvent').length;
  const ends = process.getElementsByTagNameNS(BPMN_NS, 'endEvent').length;
  if (starts !== 1) {
    issues.push({ level: 'error', message: 'Debe existir exactamente un StartEvent.' });
  }
  if (ends < 1) {
    issues.push({ level: 'error', message: 'Debe existir al menos un EndEvent.' });
  }
  const nodeIds = new Set<string>();
  for (const el of Array.from(process.children)) {
    const local = el.localName;
    if (!['startEvent', 'userTask', 'task', 'serviceTask', 'exclusiveGateway', 'parallelGateway', 'endEvent'].includes(local)) {
      continue;
    }
    const id = el.getAttribute('id');
    if (!id) {
      issues.push({ level: 'error', message: `Elemento ${local} sin id.` });
      continue;
    }
    nodeIds.add(id);
    const meta = parseDocumentationMeta(el.querySelector('documentation'));
    if (meta.formularioExternoUrl && !meta.formularioExternoUrl.startsWith('https://')) {
      issues.push({ level: 'error', message: `Nodo ${id}: formularioExternoUrl debe iniciar con https://` });
    }
  }
  const sequenceFlows = Array.from(process.getElementsByTagNameNS(BPMN_NS, 'sequenceFlow'));
  for (const sf of sequenceFlows) {
    const src = sf.getAttribute('sourceRef');
    const dst = sf.getAttribute('targetRef');
    if (!src || !dst || !nodeIds.has(src) || !nodeIds.has(dst)) {
      issues.push({ level: 'error', message: `SequenceFlow ${sf.getAttribute('id') ?? '(sin id)'} referencia nodos inválidos.` });
    }
  }
  if (nodeIds.size === 0) {
    issues.push({ level: 'error', message: 'El proceso no contiene nodos BPMN soportados.' });
  }
  return issues;
}

export function defaultBpmnXml(): string {
  return DEFAULT_BPMN_XML;
}
