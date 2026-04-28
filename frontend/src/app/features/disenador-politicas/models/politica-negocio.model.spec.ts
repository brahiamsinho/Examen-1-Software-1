import { politicaDtoToUpsertBody, type PoliticaNegocioDto } from './politica-negocio.model';

describe('politicaDtoToUpsertBody', () => {
  it('normaliza IDs de ObjectId-like a string y evita [object Object]', () => {
    const dto = {
      id: 'p1',
      nombre: 'Politica prueba',
      descripcion: 'desc',
      version: 1,
      lockVersion: 2,
      estado: 'BORRADOR',
      fechaCreacion: '2026-01-01T00:00:00Z',
      nodos: [
        {
          idNodo: 'n1',
          nombre: 'Nodo 1',
          tipoNodo: 'ACTIVIDAD',
          orden: 0,
          esInicial: true,
          esFinal: false,
          areaId: { $oid: '665f1a2b3c4d5e6f7a8b9c0d' },
          asignacionesResponsable: [
            {
              usuarioId: { hexString: '665f1a2b3c4d5e6f7a8b9c0e' },
              areaId: {
                toString: () => '665f1a2b3c4d5e6f7a8b9c0f',
              },
              fechaAsignacion: '2026-01-01T00:00:00Z',
              estado: true,
            },
          ],
        },
      ],
      conexiones: [],
    } as unknown as PoliticaNegocioDto;

    const body = politicaDtoToUpsertBody(dto);

    expect(body.nodos[0].areaId).toBe('665f1a2b3c4d5e6f7a8b9c0d');
    expect(body.nodos[0].asignacionesResponsable?.[0].usuarioId).toBe('665f1a2b3c4d5e6f7a8b9c0e');
    expect(body.nodos[0].asignacionesResponsable?.[0].areaId).toBe('665f1a2b3c4d5e6f7a8b9c0f');
  });
});
