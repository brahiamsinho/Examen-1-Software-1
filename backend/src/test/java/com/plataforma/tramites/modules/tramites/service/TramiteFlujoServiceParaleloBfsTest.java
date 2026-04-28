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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TramiteFlujoServiceParaleloBfsTest {

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
    void aprobarRamaParalela_whenAllRamasDone_convergeInMultiHopJoin() {
        TramiteDocument t = tramiteBase();
        PoliticaNegocioDocument p = politicaConJoinMultiHop();
        ObjectId areaA = new ObjectId();
        ObjectId areaB = new ObjectId();
        ObjectId areaJoin = new ObjectId();

        when(tramiteRepository.findById(t.getId())).thenAnswer(inv -> Optional.of(t));
        when(politicaNegocioRepository.findById(t.getPoliticaId())).thenReturn(Optional.of(p));
        when(tramiteRepository.save(any(TramiteDocument.class))).thenAnswer(inv -> inv.getArgument(0));
        when(autorizacion.areaDelNodo(p, "n_rama_a")).thenReturn(areaA);
        when(autorizacion.areaDelNodo(p, "n_rama_b")).thenReturn(areaB);
        when(autorizacion.areaDelNodo(p, "n_join")).thenReturn(areaJoin);
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
                        t.getNodoActualId(),
                        t.getAreaActualId() != null ? t.getAreaActualId().toHexString() : null));

        // Primera aprobación: inicializa split y marca rama A
        TramiteResponse r1 = service.aprobarRamaParalela(t.getId().toHexString(), "n_rama_a", new ObjectId().toHexString());
        assertNotNull(r1);
        assertEquals("n_split", t.getParaleloSplitNodoId());
        assertEquals(List.of("n_rama_a", "n_rama_b"), t.getParaleloRamasPendientes());
        assertEquals(List.of("n_rama_a"), t.getParaleloRamasAprobadas());
        assertEquals("n_join", t.getParaleloJoinNodoId());
        assertEquals("n_split", t.getNodoActualId());

        // Segunda aprobación: completa ramas y converge a join
        TramiteResponse r2 = service.aprobarRamaParalela(t.getId().toHexString(), "n_rama_b", new ObjectId().toHexString());
        assertNotNull(r2);
        assertEquals("n_join", t.getNodoActualId());
        assertEquals(areaJoin, t.getAreaActualId());
        assertEquals(null, t.getParaleloSplitNodoId());
        assertEquals(null, t.getParaleloJoinNodoId());
        assertEquals(List.of(), t.getParaleloRamasPendientes());
        assertEquals(List.of(), t.getParaleloRamasAprobadas());

        ArgumentCaptor<TramiteDocument> saves = ArgumentCaptor.forClass(TramiteDocument.class);
        verify(tramiteRepository, org.mockito.Mockito.atLeast(3)).save(saves.capture());
    }

    @Test
    void aprobarRamaParalela_whenNoCommonConvergence_throws422() {
        TramiteDocument t = tramiteBase();
        PoliticaNegocioDocument p = politicaSinConvergencia();

        when(tramiteRepository.findById(t.getId())).thenAnswer(inv -> Optional.of(t));
        when(politicaNegocioRepository.findById(t.getPoliticaId())).thenReturn(Optional.of(p));
        when(tramiteRepository.save(any(TramiteDocument.class))).thenAnswer(inv -> inv.getArgument(0));

        ApiException ex = assertThrows(
                ApiException.class,
                () -> service.aprobarRamaParalela(t.getId().toHexString(), "n_rama_a", new ObjectId().toHexString()));

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getStatus());
        assertEquals(true, ex.getMessage().contains("CONVERGENCIA_NO_ENCONTRADA"));
    }

    private static TramiteDocument tramiteBase() {
        TramiteDocument t = new TramiteDocument();
        t.setId(new ObjectId());
        t.setCodigo("TRM-PAR-1");
        t.setAsunto("Paralelo");
        t.setDescripcion("Prueba");
        t.setEstado("ACTIVO");
        t.setPrioridad("MEDIA");
        t.setNumeroTurno(1);
        t.setPoliticaId(new ObjectId());
        t.setNodoActualId("n_split");
        t.setParaleloRamasPendientes(new ArrayList<>());
        t.setParaleloRamasAprobadas(new ArrayList<>());
        return t;
    }

    private static PoliticaNegocioDocument politicaConJoinMultiHop() {
        PoliticaNegocioDocument p = new PoliticaNegocioDocument();
        p.setId(new ObjectId());
        p.setConexiones(List.of(
                c("c-a", "n_split", "n_rama_a", "PARALELO", null),
                c("c-b", "n_split", "n_rama_b", "PARALELO", null),
                c("c-a2", "n_rama_a", "n_mid_a", "SECUENCIAL", null),
                c("c-b2", "n_rama_b", "n_mid_b", "SECUENCIAL", null),
                c("c-a3", "n_mid_a", "n_join", "SECUENCIAL", null),
                c("c-b3", "n_mid_b", "n_join", "SECUENCIAL", null)));
        return p;
    }

    private static PoliticaNegocioDocument politicaSinConvergencia() {
        PoliticaNegocioDocument p = new PoliticaNegocioDocument();
        p.setId(new ObjectId());
        p.setConexiones(List.of(
                c("c-a", "n_split", "n_rama_a", "PARALELO", null),
                c("c-b", "n_split", "n_rama_b", "PARALELO", null),
                c("c-a2", "n_rama_a", "n_x", "SECUENCIAL", null),
                c("c-b2", "n_rama_b", "n_y", "SECUENCIAL", null)));
        return p;
    }

    private static ConexionFlujoEmbeddable c(
            String id, String origen, String destino, String tipo, String condicion) {
        ConexionFlujoEmbeddable x = new ConexionFlujoEmbeddable();
        x.setIdConexion(id);
        x.setOrigenNodoId(origen);
        x.setDestinoNodoId(destino);
        x.setTipoFlujo(tipo);
        x.setCondicion(condicion);
        return x;
    }
}
