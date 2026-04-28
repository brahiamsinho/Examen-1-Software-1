package com.plataforma.tramites.modules.tramites.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plataforma.tramites.modules.documentos.document.FormularioTramiteDocument;
import com.plataforma.tramites.modules.documentos.repository.FormularioTramiteRepository;
import com.plataforma.tramites.modules.politicas.document.PoliticaNegocioDocument;
import com.plataforma.tramites.modules.politicas.model.ConexionFlujoEmbeddable;
import com.plataforma.tramites.modules.politicas.model.NodoPoliticaEmbeddable;
import com.plataforma.tramites.modules.politicas.repository.PoliticaNegocioRepository;
import com.plataforma.tramites.modules.seguimiento.service.ClienteTramiteEventoNotificacionService;
import com.plataforma.tramites.modules.tramites.document.TramiteDocument;
import com.plataforma.tramites.modules.tramites.dto.TramiteResponse;
import com.plataforma.tramites.modules.tramites.repository.TramiteRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@DataMongoTest
@Testcontainers
class TramiteFlujoServiceMongoIntegrationTest {

    @Container
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7.0");

    @DynamicPropertySource
    static void mongoProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO::getReplicaSetUrl);
    }

    @Autowired
    private TramiteRepository tramiteRepository;
    @Autowired
    private PoliticaNegocioRepository politicaNegocioRepository;
    @Autowired
    private FormularioTramiteRepository formularioTramiteRepository;

    private TramiteFlujoAutorizacionService autorizacion;
    private TramitesService tramitesService;
    private TramiteFlujoService service;

    @BeforeEach
    void setUp() {
        autorizacion = Mockito.mock(TramiteFlujoAutorizacionService.class);
        tramitesService = Mockito.mock(TramitesService.class);
        service = new TramiteFlujoService(
                tramiteRepository,
                politicaNegocioRepository,
                autorizacion,
                tramitesService,
                formularioTramiteRepository,
                new TramiteFlujoCondicionEvaluator(),
                Mockito.mock(ClienteTramiteEventoNotificacionService.class),
                new ObjectMapper());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new ObjectId().toHexString(), "n/a", List.of()));
    }

    @AfterEach
    void tearDown() {
        tramiteRepository.deleteAll();
        politicaNegocioRepository.deleteAll();
        formularioTramiteRepository.deleteAll();
        SecurityContextHolder.clearContext();
    }

    @Test
    void avanzar_persisteNodoActualYAreaEnMongo() {
        ObjectId area = new ObjectId();
        ObjectId areaDestino = new ObjectId();
        TramiteDocument tramite = buildTramite(area);
        PoliticaNegocioDocument politica = politicaSecuencialConCondicion(tramite.getPoliticaId(), area, areaDestino);
        tramiteRepository.save(tramite);
        politicaNegocioRepository.save(politica);

        doNothing().when(autorizacion).assertFlujoActor(any());
        doNothing().when(autorizacion).assertResponsablePuedeActuarEnNodo(any(), any(), any());
        when(autorizacion.areaDelNodo(politica, "n_destino")).thenReturn(areaDestino);
        when(tramitesService.obtenerInterno(tramite.getId().toHexString()))
                .thenReturn(new TramiteResponse(
                        tramite.getId().toHexString(),
                        tramite.getCodigo(),
                        tramite.getAsunto(),
                        tramite.getDescripcion(),
                        tramite.getFechaRegistro(),
                        tramite.getPrioridad(),
                        tramite.getEstado(),
                        tramite.getNumeroTurno(),
                        tramite.getPoliticaId().toHexString(),
                        null,
                        "n_destino",
                        areaDestino.toHexString()));

        service.avanzar(tramite.getId().toHexString(), "c-ok", "ok", new ObjectId().toHexString());

        TramiteDocument persisted = tramiteRepository.findById(tramite.getId()).orElseThrow();
        assertEquals("n_destino", persisted.getNodoActualId());
        assertEquals(areaDestino, persisted.getAreaActualId());
    }

    @Test
    void aprobarRamaParalela_cuandoCompletaTodas_persisteJoinYLimpiaEstadoParalelo() {
        ObjectId area = new ObjectId();
        TramiteDocument tramite = buildTramite(area);
        tramite.setNodoActualId("n_split");
        tramite.setParaleloRamasPendientes(new ArrayList<>());
        tramite.setParaleloRamasAprobadas(new ArrayList<>());

        PoliticaNegocioDocument politica = politicaParalelaMultiHop(tramite.getPoliticaId(), area);
        tramiteRepository.save(tramite);
        politicaNegocioRepository.save(politica);

        doNothing().when(autorizacion).assertFlujoActor(any());
        doNothing().when(autorizacion).assertResponsablePuedeActuarEnNodo(any(), any(), any());
        when(autorizacion.areaDelNodo(politica, "n_rama_a")).thenReturn(area);
        when(autorizacion.areaDelNodo(politica, "n_rama_b")).thenReturn(area);
        when(autorizacion.areaDelNodo(politica, "n_join")).thenReturn(area);
        when(tramitesService.obtenerInterno(tramite.getId().toHexString()))
                .thenAnswer(inv -> {
                    TramiteDocument db = tramiteRepository.findById(tramite.getId()).orElseThrow();
                    return new TramiteResponse(
                            db.getId().toHexString(),
                            db.getCodigo(),
                            db.getAsunto(),
                            db.getDescripcion(),
                            db.getFechaRegistro(),
                            db.getPrioridad(),
                            db.getEstado(),
                            db.getNumeroTurno(),
                            db.getPoliticaId().toHexString(),
                            null,
                            db.getNodoActualId(),
                            db.getAreaActualId() != null ? db.getAreaActualId().toHexString() : null);
                });

        service.aprobarRamaParalela(tramite.getId().toHexString(), "n_rama_a", new ObjectId().toHexString());
        service.aprobarRamaParalela(tramite.getId().toHexString(), "n_rama_b", new ObjectId().toHexString());

        TramiteDocument persisted = tramiteRepository.findById(tramite.getId()).orElseThrow();
        assertEquals("n_join", persisted.getNodoActualId());
        assertEquals(List.of(), persisted.getParaleloRamasPendientes());
        assertEquals(List.of(), persisted.getParaleloRamasAprobadas());
        assertEquals(null, persisted.getParaleloSplitNodoId());
        assertEquals(null, persisted.getParaleloJoinNodoId());
    }

    @Test
    void aprobarRamaParalela_ramaRepetida_esIdempotenteEnMongo() {
        ObjectId area = new ObjectId();
        TramiteDocument tramite = buildTramite(area);
        tramite.setNodoActualId("n_split");
        tramite.setParaleloRamasPendientes(new ArrayList<>());
        tramite.setParaleloRamasAprobadas(new ArrayList<>());

        PoliticaNegocioDocument politica = politicaParalelaMultiHop(tramite.getPoliticaId(), area);
        tramiteRepository.save(tramite);
        politicaNegocioRepository.save(politica);

        doNothing().when(autorizacion).assertFlujoActor(any());
        doNothing().when(autorizacion).assertResponsablePuedeActuarEnNodo(any(), any(), any());
        when(autorizacion.areaDelNodo(politica, "n_rama_a")).thenReturn(area);
        when(autorizacion.areaDelNodo(politica, "n_rama_b")).thenReturn(area);
        when(autorizacion.areaDelNodo(politica, "n_join")).thenReturn(area);
        when(tramitesService.obtenerInterno(tramite.getId().toHexString()))
                .thenAnswer(inv -> {
                    TramiteDocument db = tramiteRepository.findById(tramite.getId()).orElseThrow();
                    return new TramiteResponse(
                            db.getId().toHexString(),
                            db.getCodigo(),
                            db.getAsunto(),
                            db.getDescripcion(),
                            db.getFechaRegistro(),
                            db.getPrioridad(),
                            db.getEstado(),
                            db.getNumeroTurno(),
                            db.getPoliticaId().toHexString(),
                            null,
                            db.getNodoActualId(),
                            db.getAreaActualId() != null ? db.getAreaActualId().toHexString() : null);
                });

        service.aprobarRamaParalela(tramite.getId().toHexString(), "n_rama_a", new ObjectId().toHexString());
        service.aprobarRamaParalela(tramite.getId().toHexString(), "n_rama_a", new ObjectId().toHexString());

        TramiteDocument persisted = tramiteRepository.findById(tramite.getId()).orElseThrow();
        assertEquals("n_split", persisted.getNodoActualId());
        assertEquals(List.of("n_rama_a"), persisted.getParaleloRamasAprobadas());
        assertEquals(List.of("n_rama_a", "n_rama_b"), persisted.getParaleloRamasPendientes());
    }

    @Test
    void aprobarRamaParalela_sinConvergencia_lanza422() {
        ObjectId area = new ObjectId();
        TramiteDocument tramite = buildTramite(area);
        tramite.setNodoActualId("n_split");
        tramite.setParaleloRamasPendientes(new ArrayList<>());
        tramite.setParaleloRamasAprobadas(new ArrayList<>());

        PoliticaNegocioDocument politica = politicaParalelaSinJoin(tramite.getPoliticaId(), area);
        tramiteRepository.save(tramite);
        politicaNegocioRepository.save(politica);

        doNothing().when(autorizacion).assertFlujoActor(any());
        doNothing().when(autorizacion).assertResponsablePuedeActuarEnNodo(any(), any(), any());

        var ex = assertThrows(
                com.plataforma.tramites.shared.exception.ApiException.class,
                () -> service.aprobarRamaParalela(
                        tramite.getId().toHexString(), "n_rama_a", new ObjectId().toHexString()));

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getStatus());
    }

    @Test
    void avanzar_conCondicionPorFormulario_formTipo_persisteDestinoCorrecto() {
        ObjectId area = new ObjectId();
        ObjectId areaDestino = new ObjectId();
        TramiteDocument tramite = buildTramite(area);
        PoliticaNegocioDocument politica =
                politicaSecuencialConCondicion(tramite.getPoliticaId(), area, areaDestino, "form.tipo == TECNICO");
        tramiteRepository.save(tramite);
        politicaNegocioRepository.save(politica);

        FormularioTramiteDocument formulario = new FormularioTramiteDocument();
        formulario.setId(new ObjectId());
        formulario.setTramiteId(tramite.getId());
        formulario.setNodoId("n_origen");
        formulario.setTitulo("Formulario técnico");
        formulario.setTipo("TECNICO");
        formulario.setContenido("{\"campo\":\"valor\"}");
        formulario.setFechaRegistro(Instant.now());
        formularioTramiteRepository.save(formulario);

        doNothing().when(autorizacion).assertFlujoActor(any());
        doNothing().when(autorizacion).assertResponsablePuedeActuarEnNodo(any(), any(), any());
        when(autorizacion.areaDelNodo(politica, "n_destino")).thenReturn(areaDestino);
        when(tramitesService.obtenerInterno(tramite.getId().toHexString()))
                .thenReturn(new TramiteResponse(
                        tramite.getId().toHexString(),
                        tramite.getCodigo(),
                        tramite.getAsunto(),
                        tramite.getDescripcion(),
                        tramite.getFechaRegistro(),
                        tramite.getPrioridad(),
                        tramite.getEstado(),
                        tramite.getNumeroTurno(),
                        tramite.getPoliticaId().toHexString(),
                        null,
                        "n_destino",
                        areaDestino.toHexString()));

        service.avanzar(tramite.getId().toHexString(), "c-ok", "ok", new ObjectId().toHexString());

        TramiteDocument persisted = tramiteRepository.findById(tramite.getId()).orElseThrow();
        assertEquals("n_destino", persisted.getNodoActualId());
        assertEquals(areaDestino, persisted.getAreaActualId());
    }

    private static TramiteDocument buildTramite(ObjectId area) {
        TramiteDocument t = new TramiteDocument();
        t.setId(new ObjectId());
        t.setCodigo("TRM-MONGO-1");
        t.setAsunto("Asunto");
        t.setDescripcion("Desc");
        t.setEstado("ACTIVO");
        t.setPrioridad("BAJA");
        t.setNumeroTurno(1);
        t.setPoliticaId(new ObjectId());
        t.setNodoActualId("n_origen");
        t.setAreaActualId(area);
        return t;
    }

    private static PoliticaNegocioDocument politicaSecuencialConCondicion(
            ObjectId politicaId, ObjectId areaOrigen, ObjectId areaDestino) {
        return politicaSecuencialConCondicion(politicaId, areaOrigen, areaDestino, "prioridad == BAJA");
    }

    private static PoliticaNegocioDocument politicaSecuencialConCondicion(
            ObjectId politicaId, ObjectId areaOrigen, ObjectId areaDestino, String condicion) {
        NodoPoliticaEmbeddable nOrigen = nodo("n_origen", areaOrigen);
        NodoPoliticaEmbeddable nDestino = nodo("n_destino", areaDestino);
        ConexionFlujoEmbeddable c = edge("c-ok", "n_origen", "n_destino", "SECUENCIAL", condicion);

        PoliticaNegocioDocument p = new PoliticaNegocioDocument();
        p.setId(politicaId);
        p.setNodos(List.of(nOrigen, nDestino));
        p.setConexiones(List.of(c));
        return p;
    }

    private static PoliticaNegocioDocument politicaParalelaMultiHop(ObjectId politicaId, ObjectId area) {
        PoliticaNegocioDocument p = new PoliticaNegocioDocument();
        p.setId(politicaId);
        p.setNodos(List.of(
                nodo("n_split", area),
                nodo("n_rama_a", area),
                nodo("n_rama_b", area),
                nodo("n_mid_a", area),
                nodo("n_mid_b", area),
                nodo("n_join", area)));
        p.setConexiones(List.of(
                edge("c-a", "n_split", "n_rama_a", "PARALELO", null),
                edge("c-b", "n_split", "n_rama_b", "PARALELO", null),
                edge("c-a2", "n_rama_a", "n_mid_a", "SECUENCIAL", null),
                edge("c-b2", "n_rama_b", "n_mid_b", "SECUENCIAL", null),
                edge("c-a3", "n_mid_a", "n_join", "SECUENCIAL", null),
                edge("c-b3", "n_mid_b", "n_join", "SECUENCIAL", null)));
        return p;
    }

    private static PoliticaNegocioDocument politicaParalelaSinJoin(ObjectId politicaId, ObjectId area) {
        PoliticaNegocioDocument p = new PoliticaNegocioDocument();
        p.setId(politicaId);
        p.setNodos(List.of(
                nodo("n_split", area),
                nodo("n_rama_a", area),
                nodo("n_rama_b", area),
                nodo("n_mid_a", area),
                nodo("n_mid_b", area),
                nodo("n_join_a", area),
                nodo("n_join_b", area)));
        p.setConexiones(List.of(
                edge("c-a", "n_split", "n_rama_a", "PARALELO", null),
                edge("c-b", "n_split", "n_rama_b", "PARALELO", null),
                edge("c-a2", "n_rama_a", "n_mid_a", "SECUENCIAL", null),
                edge("c-b2", "n_rama_b", "n_mid_b", "SECUENCIAL", null),
                edge("c-a3", "n_mid_a", "n_join_a", "SECUENCIAL", null),
                edge("c-b3", "n_mid_b", "n_join_b", "SECUENCIAL", null)));
        return p;
    }

    private static NodoPoliticaEmbeddable nodo(String id, ObjectId area) {
        NodoPoliticaEmbeddable n = new NodoPoliticaEmbeddable();
        n.setIdNodo(id);
        n.setAreaId(area);
        return n;
    }

    private static ConexionFlujoEmbeddable edge(
            String id, String origen, String destino, String tipoFlujo, String condicion) {
        ConexionFlujoEmbeddable c = new ConexionFlujoEmbeddable();
        c.setIdConexion(id);
        c.setOrigenNodoId(origen);
        c.setDestinoNodoId(destino);
        c.setTipoFlujo(tipoFlujo);
        c.setCondicion(condicion);
        return c;
    }
}
