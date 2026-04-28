/** Alineado a `SalidaFlujoDto` del backend. */
export interface SalidaFlujoDto {
  idConexion: string;
  destinoNodoId: string;
  tipoFlujo: string;
  condicion: string | null;
}

/** Respuesta de `GET .../flujo/salidas` (salidas + contexto del nodo actual). */
export interface FlujoSalidasDto {
  salidas: SalidaFlujoDto[];
  nodoActualId: string | null;
  nodoActualNombre: string | null;
  nodoActualCarrilBpmn: string | null;
  formularioExternoUrl: string | null;
}
