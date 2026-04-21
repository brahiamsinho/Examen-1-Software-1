export interface PoliticaAreaResumenDto {
  id: string;
  nombre: string;
  version: number;
  estado: string;
}

export interface ResponsableAreaContextDto {
  tieneArea: boolean;
  areaId: string | null;
  areaNombre: string | null;
  areaDescripcion: string | null;
  politicasEnLasQueParticipaElArea: PoliticaAreaResumenDto[];
}
