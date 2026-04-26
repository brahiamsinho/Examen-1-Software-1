import { Injectable, inject } from '@angular/core';
import { Subject } from 'rxjs';
import { environment } from '@environments/environment';
import { AuthService } from '@core/auth/auth.service';

const WS_PATH = '/ws/politicas';
const QUERY_TOKEN = 'access_token';

export interface PoliticaCollabPeer {
  sessionId: string;
  displayName: string;
}

export type PoliticaCollabInbound =
  | { type: 'SESSION_ACK'; sessionId: string }
  | { type: 'PRESENCE_SYNC'; politicaId: string; peers: PoliticaCollabPeer[] }
  | { type: 'GRAPH_UPDATE'; politicaId: string; revision: number; sourceSessionId: string; cells: unknown[] }
  | {
      type: 'POINTER';
      politicaId: string;
      sourceSessionId: string;
      displayName: string;
      visible: boolean;
      gx?: number;
      gy?: number;
      selectedIds?: string[];
    }
  | { type: 'ERROR'; message: string };

/**
 * WebSocket colaborativo del editor X6 (salas por política). JWT en query {@link QUERY_TOKEN} (limitación del API WebSocket del navegador).
 */
@Injectable({ providedIn: 'root' })
export class PoliticasCollaborationService {
  private readonly auth = inject(AuthService);

  private ws: WebSocket | null = null;
  private roomId: string | null = null;
  private pendingJoinPoliticaId: string | null = null;

  readonly inbound$ = new Subject<PoliticaCollabInbound>();

  /** Id de sesión asignado por el servidor (SESSION_ACK). */
  private localSessionId: string | null = null;

  getLocalSessionId(): string | null {
    return this.localSessionId;
  }

  private backendWsBaseUrl(): string {
    const loc = window.location;
    const wsProto = loc.protocol === 'https:' ? 'wss:' : 'ws:';
    const base = environment.apiBackendUrl.replace(/\/$/, '');
    return `${wsProto}//${loc.host}${base}${WS_PATH}`;
  }

  /** Abre el socket si hace falta y entra a la sala de la política. */
  joinPolitica(politicaId: string): void {
    const token = this.auth.getToken();
    if (!token) {
      return;
    }
    this.pendingJoinPoliticaId = politicaId;
    if (!this.ws || this.ws.readyState === WebSocket.CLOSED) {
      const url = `${this.backendWsBaseUrl()}?${QUERY_TOKEN}=${encodeURIComponent(token)}`;
      this.ws = new WebSocket(url);
      this.ws.onopen = () => this.flushJoinAndPresence();
      this.ws.onmessage = (ev) => this.handleMessage(ev.data as string);
      this.ws.onerror = () => {
        /* el cierre o ERROR del servidor informará */
      };
      this.ws.onclose = () => {
        this.ws = null;
        this.roomId = null;
        this.localSessionId = null;
      };
    } else if (this.ws.readyState === WebSocket.OPEN) {
      this.flushJoinAndPresence();
    }
  }

  leavePolitica(): void {
    this.pendingJoinPoliticaId = null;
    if (this.ws?.readyState === WebSocket.OPEN && this.roomId) {
      this.safeSend(JSON.stringify({ type: 'LEAVE' }));
    }
    this.roomId = null;
  }

  disconnect(): void {
    this.pendingJoinPoliticaId = null;
    this.roomId = null;
    this.localSessionId = null;
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
  }

  pushPresence(displayName: string): void {
    if (this.ws?.readyState !== WebSocket.OPEN || !this.roomId) {
      return;
    }
    this.safeSend(JSON.stringify({ type: 'PRESENCE', politicaId: this.roomId, displayName }));
  }

  pushGraphCells(cells: unknown[]): void {
    if (this.ws?.readyState !== WebSocket.OPEN || !this.roomId) {
      return;
    }
    this.safeSend(JSON.stringify({ type: 'GRAPH_UPDATE', politicaId: this.roomId, cells }));
  }

  /** Puntero y selección en coordenadas de grafo X6 (como Miro). */
  pushPointer(gx: number, gy: number, selectedIds: string[], visible: boolean): void {
    if (this.ws?.readyState !== WebSocket.OPEN || !this.roomId) {
      return;
    }
    if (!visible) {
      this.safeSend(
        JSON.stringify({
          type: 'POINTER',
          politicaId: this.roomId,
          visible: false,
        }),
      );
      return;
    }
    this.safeSend(
      JSON.stringify({
        type: 'POINTER',
        politicaId: this.roomId,
        visible: true,
        gx,
        gy,
        selectedIds,
      }),
    );
  }

  private flushJoinAndPresence(): void {
    const target = this.pendingJoinPoliticaId;
    if (!target || !this.ws || this.ws.readyState !== WebSocket.OPEN) {
      return;
    }
    if (this.roomId && this.roomId !== target) {
      this.safeSend(JSON.stringify({ type: 'LEAVE' }));
    }
    this.safeSend(JSON.stringify({ type: 'JOIN', politicaId: target }));
    this.roomId = target;
    const correo = this.auth.getCorreo()?.trim();
    if (correo) {
      this.safeSend(JSON.stringify({ type: 'PRESENCE', politicaId: target, displayName: correo }));
    }
  }

  private safeSend(raw: string): void {
    try {
      this.ws?.send(raw);
    } catch {
      /* socket roto */
    }
  }

  private handleMessage(raw: string): void {
    let parsed: unknown;
    try {
      parsed = JSON.parse(raw) as Record<string, unknown>;
    } catch {
      return;
    }
    if (!parsed || typeof parsed !== 'object') {
      return;
    }
    const o = parsed as Record<string, unknown>;
    const type = o['type'];
    if (type === 'SESSION_ACK' && typeof o['sessionId'] === 'string') {
      this.localSessionId = o['sessionId'];
      this.inbound$.next({ type: 'SESSION_ACK', sessionId: o['sessionId'] });
      return;
    }
    if (type === 'PRESENCE_SYNC' && typeof o['politicaId'] === 'string' && Array.isArray(o['peers'])) {
      const peers = (o['peers'] as unknown[])
        .map((p) => {
          if (!p || typeof p !== 'object') {
            return null;
          }
          const q = p as Record<string, unknown>;
          if (typeof q['sessionId'] !== 'string') {
            return null;
          }
          return {
            sessionId: q['sessionId'],
            displayName: typeof q['displayName'] === 'string' ? q['displayName'] : '',
          } satisfies PoliticaCollabPeer;
        })
        .filter((x): x is PoliticaCollabPeer => x != null);
      this.inbound$.next({ type: 'PRESENCE_SYNC', politicaId: o['politicaId'], peers });
      return;
    }
    if (
      type === 'GRAPH_UPDATE' &&
      typeof o['politicaId'] === 'string' &&
      typeof o['revision'] === 'number' &&
      typeof o['sourceSessionId'] === 'string' &&
      Array.isArray(o['cells'])
    ) {
      this.inbound$.next({
        type: 'GRAPH_UPDATE',
        politicaId: o['politicaId'],
        revision: o['revision'],
        sourceSessionId: o['sourceSessionId'],
        cells: o['cells'] as unknown[],
      });
      return;
    }
    if (
      type === 'POINTER' &&
      typeof o['politicaId'] === 'string' &&
      typeof o['sourceSessionId'] === 'string' &&
      typeof o['visible'] === 'boolean'
    ) {
      const visible = o['visible'] as boolean;
      const base = {
        type: 'POINTER' as const,
        politicaId: o['politicaId'] as string,
        sourceSessionId: o['sourceSessionId'] as string,
        displayName: typeof o['displayName'] === 'string' ? o['displayName'] : '',
        visible,
      };
      if (!visible) {
        this.inbound$.next(base);
        return;
      }
      const gx = o['gx'];
      const gy = o['gy'];
      if (typeof gx !== 'number' || typeof gy !== 'number') {
        return;
      }
      const selectedIds: string[] = [];
      if (Array.isArray(o['selectedIds'])) {
        for (const id of o['selectedIds'] as unknown[]) {
          if (typeof id === 'string' && id.trim()) {
            selectedIds.push(id.trim());
          }
        }
      }
      this.inbound$.next({
        ...base,
        gx,
        gy,
        selectedIds,
      });
      return;
    }
    if (type === 'ERROR' && typeof o['message'] === 'string') {
      this.inbound$.next({ type: 'ERROR', message: o['message'] });
    }
  }
}
