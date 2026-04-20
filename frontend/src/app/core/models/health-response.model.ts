/** Alineado con `HealthResponse` de Spring Boot y el esquema de FastAPI (campos extra ignorables). */
export interface HealthResponseDto {
  status: string;
  service: string;
  timestamp: string;
  environment?: string;
}
