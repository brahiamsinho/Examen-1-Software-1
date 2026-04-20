export interface LoginResponseDto {
  tokenType: string;
  accessToken: string;
  expiresInSeconds: number;
  rolCodigo: string;
  nombres: string;
  apellidos: string;
  correo: string;
}
