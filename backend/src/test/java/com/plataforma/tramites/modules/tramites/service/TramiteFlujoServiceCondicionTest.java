package com.plataforma.tramites.modules.tramites.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plataforma.tramites.modules.documentos.repository.FormularioTramiteRepository;
import com.plataforma.tramites.modules.politicas.document.PoliticaNegocioDocument;
import com.plataforma.tramites.modules.politicas.model.ConexionFlujoEmbeddable;
import com.plataforma.tramites.modules.politicas.repository.PoliticaNegocioRepository;
import com.plataforma.tramites.modules.seguimiento.service.ClienteTramiteEventoNotificacionService;
import com.plataforma.tramites.modules.tramites.document.TramiteDocument;
import com.plataforma.tramites.modules.tramites.dto.TramiteResponse;
import com.plataforma.tramites.modules.tramites.repository.TramiteRepository;
import com.plataforma.tramites.shared.exception.ApiException;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TramiteFlujoServiceCondicionTest {

    @Mock
    private TramiteRepository tramiteRepository;
    @Mock
    private PoliticaNegocioRepository politicaNegocioRepository;
    @Mock
    private TramiteFlujoAutorizacionService autorizacion;
    @Mock
    private TramitesService tramitesService;
    @Mock
    private FormularioTramiteRepository formularioTramiteRepository;
    @Mock
    private ClienteTramiteEventoNotificacionService clienteTramiteEventoNotificacionService;

    private TramiteFlujoService service;

    @BeforeEach
    void setUp() {
        service = new TramiteFlujoService(
                tramiteRepository,
                politicaNegocioRepository,
                autorizacion,
                tramitesService,
                formularioTramiteRepository,
                new TramiteFlujoCondicionEvaluator(),
                clienteTramiteEventoNotificacionService,
                new ObjectMapper());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new ObjectId().toHexString(), "n/a", List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void avanzar_whenCondicionCumple_updatesNodoActual() {
        TramiteDocument t = tramite("BAJA");
        PoliticaNegocioDocument p = politica(new ConexionFlujoEmbeddable[] {
            conexion("c-ok", "n_origen", "n_destino", "prioridad == BAJA", "SECUENCIAL")
        });
        ObjectId areaDestino = new ObjectId();
        when(tramiteRepository.findById(t.getId())).thenReturn(Optional.of(t));
        when(politicaNegocioRepository.findById(t.getPoliticaId())).thenReturn(Optional.of(p));
        when(formularioTramiteRepository.findByTramiteIdOrderByFechaRegistroDesc(t.getId())).thenReturn(List.of());
        when(autorizacion.areaDelNodo(p, "n_destino")).thenReturn(areaDestino);
        when(tramiteRepository.save(any(TramiteDocument.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tramitesService.obtenerInterno(t.getId().toHexString()))
                .thenReturn(new TramiteResponse(
                        t.getId().toHexString(),
                        t.getCodigo(),
                        t.getAsunto(),
                        t.getDescripcion(),
                        t.getFechaRegistro(),
                        t.getPrioridad(),
                        t.getEstado(),
                        t.getNumeroTurno(),
                        t.getPoliticaId().toHexString(),
                        null,
                        "n_destino",
                        areaDestino.toHexString()));

        TramiteResponse out = service.avanzar(t.getId().toHexString(), "c-ok", "obs", new ObjectId().toHexString());

        assertEquals("n_destino", out.nodoActualId());
        assertEquals(areaDestino.toHexString(), out.areaActualId());
        ArgumentCaptor<TramiteDocument> captor = ArgumentCaptor.forClass(TramiteDocument.class);
        verify(tramiteRepository).save(captor.capture());
        assertEquals("n_destino", captor.getValue().getNodoActualId());
        assertEquals(areaDestino, captor.getValue().getAreaActualId());
    }

    @Test
    void avanzar_whenNingunaCondicionCumple_throwsSinSalidaValida() {
        TramiteDocument t = tramite("BAJA");
        PoliticaNegocioDocument p = politica(new ConexionFlujoEmbeddable[] {
            conexion("c1", "n_origen", "n_a", "prioridad == ALTA", "SECUENCIAL"),
            conexion("c2", "n_origen", "n_b", "tramite.estado == CERRADO", "SECUENCIAL")
        });
        when(tramiteRepository.findById(t.getId())).thenReturn(Optional.of(t));
        when(politicaNegocioRepository.findById(t.getPoliticaId())).thenReturn(Optional.of(p));
        when(formularioTramiteRepository.findByTramiteIdOrderByFechaRegistroDesc(t.getId())).thenReturn(List.of());

        ApiException ex = assertThrows(
                ApiException.class,
                () -> service.avanzar(t.getId().toHexString(), "c1", null, new ObjectId().toHexString()));

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getStatus());
        assertEquals(true, ex.getMessage().contains("SIN_SALIDA_VALIDA"));
    }

    @Test
    void avanzar_whenCondicionInvalida_throwsBadRequest() {
        TramiteDocument t = tramite("BAJA");
        PoliticaNegocioDocument p = politica(new ConexionFlujoEmbeddable[] {
            conexion("c1", "n_origen", "n_a", "prioridad >=", "SECUENCIAL")
        });
        when(tramiteRepository.findById(t.getId())).thenReturn(Optional.of(t));
        when(politicaNegocioRepository.findById(t.getPoliticaId())).thenReturn(Optional.of(p));
        when(formularioTramiteRepository.findByTramiteIdOrderByFechaRegistroDesc(t.getId())).thenReturn(List.of());

        ApiException ex = assertThrows(
                ApiException.class,
                () -> service.avanzar(t.getId().toHexString(), "c1", null, new ObjectId().toHexString()));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertEquals(true, ex.getMessage().contains("CONDICION_INVALIDA"));
    }

    private static TramiteDocument tramite(String prioridad) {
        TramiteDocument t = new TramiteDocument();
        t.setId(new ObjectId());
        t.setCodigo("TRM-1");
        t.setAsunto("Asunto");
        t.setDescripcion("Desc");
        t.setEstado("ACTIVO");
        t.setPrioridad(prioridad);
        t.setNumeroTurno(1);
        t.setPoliticaId(new ObjectId());
        t.setNodoActualId("n_origen");
        return t;
    }

    private static PoliticaNegocioDocument politica(ConexionFlujoEmbeddable[] conexiones) {
        PoliticaNegocioDocument p = new PoliticaNegocioDocument();
        p.setId(new ObjectId());
        p.setConexiones(List.of(conexiones));
        return p;
    }

    private static ConexionFlujoEmbeddable conexion(
            String id, String origen, String destino, String condicion, String tipoFlujo) {
        ConexionFlujoEmbeddable c = new ConexionFlujoEmbeddable();
        c.setIdConexion(id);
        c.setOrigenNodoId(origen);
        c.setDestinoNodoId(destino);
        c.setCondicion(condicion);
        c.setTipoFlujo(tipoFlujo);
        return c;
    }
}
