/** Alineado a `SalidaFlujoDto` del backend. */
export interface SalidaFlujoDto {
  idConexion: string;
  destinoNodoId: string;
  tipoFlujo: string;
  condicion: string | null;
}
