package com.plataforma.tramites.modules.tramites.controller;

import com.plataforma.tramites.modules.documentos.repository.FormularioTramiteRepository;
import com.plataforma.tramites.modules.politicas.document.PoliticaNegocioDocument;
import com.plataforma.tramites.modules.politicas.model.ConexionFlujoEmbeddable;
import com.plataforma.tramites.modules.politicas.model.NodoPoliticaEmbeddable;
import com.plataforma.tramites.modules.politicas.repository.PoliticaNegocioRepository;
import com.plataforma.tramites.modules.seguridad.document.UsuarioDocument;
import com.plataforma.tramites.modules.seguridad.repository.UsuarioRepository;
import com.plataforma.tramites.modules.seguridad.service.JwtService;
import com.plataforma.tramites.modules.tramites.document.TramiteDocument;
import com.plataforma.tramites.modules.tramites.dto.TramiteResponse;
import com.plataforma.tramites.modules.tramites.repository.TramiteRepository;
import com.plataforma.tramites.modules.tramites.service.TramitesService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TramiteFlujoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private TramiteRepository tramiteRepository;
    @MockBean
    private PoliticaNegocioRepository politicaNegocioRepository;
    @MockBean
    private UsuarioRepository usuarioRepository;
    @MockBean
    private FormularioTramiteRepository formularioTramiteRepository;
    @MockBean
    private TramitesService tramitesService;

    @Test
    void avanzar_whenCondicionCumple_returns200() throws Exception {
        Fixture f = fixture("BAJA", "prioridad == BAJA");
        String token = jwtService.generateToken(
                f.user.getId().toHexString(), "resp@tramites.local", "RESPONSABLE_AREA");

        when(tramiteRepository.findById(f.tramite.getId())).thenReturn(Optional.of(f.tramite));
        when(politicaNegocioRepository.findById(f.tramite.getPoliticaId())).thenReturn(Optional.of(f.politica));
        when(usuarioRepository.findById(f.user.getId())).thenReturn(Optional.of(f.user));
        when(formularioTramiteRepository.findByTramiteIdOrderByFechaRegistroDesc(f.tramite.getId()))
                .thenReturn(List.of());
        when(tramiteRepository.save(any(TramiteDocument.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tramitesService.obtenerInterno(f.tramite.getId().toHexString()))
                .thenReturn(new TramiteResponse(
                        f.tramite.getId().toHexString(),
                        f.tramite.getCodigo(),
                        f.tramite.getAsunto(),
                        f.tramite.getDescripcion(),
                        f.tramite.getFechaRegistro(),
                        f.tramite.getPrioridad(),
                        f.tramite.getEstado(),
                        f.tramite.getNumeroTurno(),
                        f.tramite.getPoliticaId().toHexString(),
                        null,
                        "n_destino",
                        f.nodeArea.toHexString()));

        mockMvc.perform(post("/api/tramites/{tramiteId}/flujo/avanzar", f.tramite.getId().toHexString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content("{\"idConexion\":\"c-ok\",\"observacion\":\"ok\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nodoActualId").value("n_destino"));
    }

    @Test
    void avanzar_whenCondicionNoCumple_returns422() throws Exception {
        Fixture f = fixture("BAJA", "prioridad == ALTA");
        String token = jwtService.generateToken(
                f.user.getId().toHexString(), "resp@tramites.local", "RESPONSABLE_AREA");

        when(tramiteRepository.findById(f.tramite.getId())).thenReturn(Optional.of(f.tramite));
        when(politicaNegocioRepository.findById(f.tramite.getPoliticaId())).thenReturn(Optional.of(f.politica));
        when(usuarioRepository.findById(f.user.getId())).thenReturn(Optional.of(f.user));
        when(formularioTramiteRepository.findByTramiteIdOrderByFechaRegistroDesc(f.tramite.getId()))
                .thenReturn(List.of());

        mockMvc.perform(post("/api/tramites/{tramiteId}/flujo/avanzar", f.tramite.getId().toHexString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content("{\"idConexion\":\"c-ok\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("SIN_SALIDA_VALIDA")));
    }

    @Test
    void avanzar_whenCondicionInvalida_returns400() throws Exception {
        Fixture f = fixture("BAJA", "prioridad >=");
        String token = jwtService.generateToken(
                f.user.getId().toHexString(), "resp@tramites.local", "RESPONSABLE_AREA");

        when(tramiteRepository.findById(f.tramite.getId())).thenReturn(Optional.of(f.tramite));
        when(politicaNegocioRepository.findById(f.tramite.getPoliticaId())).thenReturn(Optional.of(f.politica));
        when(usuarioRepository.findById(f.user.getId())).thenReturn(Optional.of(f.user));
        when(formularioTramiteRepository.findByTramiteIdOrderByFechaRegistroDesc(f.tramite.getId()))
                .thenReturn(List.of());

        mockMvc.perform(post("/api/tramites/{tramiteId}/flujo/avanzar", f.tramite.getId().toHexString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content("{\"idConexion\":\"c-ok\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("CONDICION_INVALIDA")))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Operadores soportados")))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Ejemplos")));
    }

    @Test
    void avanzar_whenCondicionCampoInvalido_returns400WithGuidedMessage() throws Exception {
        Fixture f = fixture("BAJA", "1prioridad == BAJA");
        String token = jwtService.generateToken(
                f.user.getId().toHexString(), "resp@tramites.local", "RESPONSABLE_AREA");

        when(tramiteRepository.findById(f.tramite.getId())).thenReturn(Optional.of(f.tramite));
        when(politicaNegocioRepository.findById(f.tramite.getPoliticaId())).thenReturn(Optional.of(f.politica));
        when(usuarioRepository.findById(f.user.getId())).thenReturn(Optional.of(f.user));
        when(formularioTramiteRepository.findByTramiteIdOrderByFechaRegistroDesc(f.tramite.getId()))
                .thenReturn(List.of());

        mockMvc.perform(post("/api/tramites/{tramiteId}/flujo/avanzar", f.tramite.getId().toHexString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content("{\"idConexion\":\"c-ok\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("CONDICION_INVALIDA")))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Campo inválido")))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Ejemplos")));
    }

    @Test
    void avanzar_whenCondicionInListEmpty_returns400WithGuidedMessage() throws Exception {
        Fixture f = fixture("BAJA", "prioridad in []");
        String token = jwtService.generateToken(
                f.user.getId().toHexString(), "resp@tramites.local", "RESPONSABLE_AREA");

        when(tramiteRepository.findById(f.tramite.getId())).thenReturn(Optional.of(f.tramite));
        when(politicaNegocioRepository.findById(f.tramite.getPoliticaId())).thenReturn(Optional.of(f.politica));
        when(usuarioRepository.findById(f.user.getId())).thenReturn(Optional.of(f.user));
        when(formularioTramiteRepository.findByTramiteIdOrderByFechaRegistroDesc(f.tramite.getId()))
                .thenReturn(List.of());

        mockMvc.perform(post("/api/tramites/{tramiteId}/flujo/avanzar", f.tramite.getId().toHexString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content("{\"idConexion\":\"c-ok\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("CONDICION_INVALIDA")))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("lista de in no puede ser vacía")))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Ejemplos")));
    }

    @Test
    void avanzar_whenResponsableAreaMismatch_returns403() throws Exception {
        Fixture f = fixture("BAJA", "prioridad == BAJA");
        ObjectId otherArea = new ObjectId();
        f.user.setAreaId(otherArea);
        String token = jwtService.generateToken(
                f.user.getId().toHexString(), "resp@tramites.local", "RESPONSABLE_AREA");

        when(tramiteRepository.findById(f.tramite.getId())).thenReturn(Optional.of(f.tramite));
        when(politicaNegocioRepository.findById(f.tramite.getPoliticaId())).thenReturn(Optional.of(f.politica));
        when(usuarioRepository.findById(f.user.getId())).thenReturn(Optional.of(f.user));
        when(formularioTramiteRepository.findByTramiteIdOrderByFechaRegistroDesc(f.tramite.getId()))
                .thenReturn(List.of());

        mockMvc.perform(post("/api/tramites/{tramiteId}/flujo/avanzar", f.tramite.getId().toHexString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content("{\"idConexion\":\"c-ok\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void avanzar_whenPlanificadorRole_returns403() throws Exception {
        Fixture f = fixture("BAJA", "prioridad == BAJA");
        String token = jwtService.generateToken(
                f.user.getId().toHexString(), "plan@tramites.local", "PLANIFICADOR");

        when(tramiteRepository.findById(f.tramite.getId())).thenReturn(Optional.of(f.tramite));

        mockMvc.perform(post("/api/tramites/{tramiteId}/flujo/avanzar", f.tramite.getId().toHexString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content("{\"idConexion\":\"c-ok\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void avanzar_whenNoToken_returns401() throws Exception {
        Fixture f = fixture("BAJA", "prioridad == BAJA");

        mockMvc.perform(post("/api/tramites/{tramiteId}/flujo/avanzar", f.tramite.getId().toHexString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idConexion\":\"c-ok\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void avanzar_whenInvalidToken_returns401() throws Exception {
        Fixture f = fixture("BAJA", "prioridad == BAJA");

        mockMvc.perform(post("/api/tramites/{tramiteId}/flujo/avanzar", f.tramite.getId().toHexString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-invalido")
                        .content("{\"idConexion\":\"c-ok\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void aprobarRamaParalela_whenCompletesBothBranches_convergesJoin200() throws Exception {
        ParallelFixture f = parallelFixtureWithJoin();
        String token = jwtService.generateToken(
                f.user.getId().toHexString(), "resp@tramites.local", "RESPONSABLE_AREA");
        when(tramiteRepository.findById(f.tramite.getId())).thenAnswer(inv -> Optional.of(f.tramite));
        when(politicaNegocioRepository.findById(f.tramite.getPoliticaId())).thenReturn(Optional.of(f.politica));
        when(usuarioRepository.findById(f.user.getId())).thenReturn(Optional.of(f.user));
        when(tramiteRepository.save(any(TramiteDocument.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tramitesService.obtenerInterno(f.tramite.getId().toHexString()))
                .thenAnswer(inv -> new TramiteResponse(
                        f.tramite.getId().toHexString(),
                        f.tramite.getCodigo(),
                        f.tramite.getAsunto(),
                        f.tramite.getDescripcion(),
                        f.tramite.getFechaRegistro(),
                        f.tramite.getPrioridad(),
                        f.tramite.getEstado(),
                        f.tramite.getNumeroTurno(),
                        f.tramite.getPoliticaId().toHexString(),
                        null,
                        f.tramite.getNodoActualId(),
                        f.tramite.getAreaActualId() != null ? f.tramite.getAreaActualId().toHexString() : null));

        mockMvc.perform(post("/api/tramites/{tramiteId}/flujo/aprobar-rama-paralela", f.tramite.getId().toHexString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content("{\"nodoRamaId\":\"n_rama_a\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/tramites/{tramiteId}/flujo/aprobar-rama-paralela", f.tramite.getId().toHexString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content("{\"nodoRamaId\":\"n_rama_b\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nodoActualId").value("n_join"));
    }

    @Test
    void aprobarRamaParalela_whenNoConvergence_returns422() throws Exception {
        ParallelFixture f = parallelFixtureWithoutJoin();
        String token = jwtService.generateToken(
                f.user.getId().toHexString(), "resp@tramites.local", "RESPONSABLE_AREA");
        when(tramiteRepository.findById(f.tramite.getId())).thenAnswer(inv -> Optional.of(f.tramite));
        when(politicaNegocioRepository.findById(f.tramite.getPoliticaId())).thenReturn(Optional.of(f.politica));
        when(usuarioRepository.findById(f.user.getId())).thenReturn(Optional.of(f.user));
        when(tramiteRepository.save(any(TramiteDocument.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/api/tramites/{tramiteId}/flujo/aprobar-rama-paralela", f.tramite.getId().toHexString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content("{\"nodoRamaId\":\"n_rama_a\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("CONVERGENCIA_NO_ENCONTRADA")));
    }

    @Test
    void aprobarRamaParalela_whenSameBranchRepeated_isIdempotent200() throws Exception {
        ParallelFixture f = parallelFixtureWithJoin();
        String token = jwtService.generateToken(
                f.user.getId().toHexString(), "resp@tramites.local", "RESPONSABLE_AREA");
        when(tramiteRepository.findById(f.tramite.getId())).thenAnswer(inv -> Optional.of(f.tramite));
        when(politicaNegocioRepository.findById(f.tramite.getPoliticaId())).thenReturn(Optional.of(f.politica));
        when(usuarioRepository.findById(f.user.getId())).thenReturn(Optional.of(f.user));
        when(tramiteRepository.save(any(TramiteDocument.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tramitesService.obtenerInterno(f.tramite.getId().toHexString()))
                .thenAnswer(inv -> new TramiteResponse(
                        f.tramite.getId().toHexString(),
                        f.tramite.getCodigo(),
                        f.tramite.getAsunto(),
                        f.tramite.getDescripcion(),
                        f.tramite.getFechaRegistro(),
                        f.tramite.getPrioridad(),
                        f.tramite.getEstado(),
                        f.tramite.getNumeroTurno(),
                        f.tramite.getPoliticaId().toHexString(),
                        null,
                        f.tramite.getNodoActualId(),
                        f.tramite.getAreaActualId() != null ? f.tramite.getAreaActualId().toHexString() : null));

        // Primera vez: inicializa split y marca rama A
        mockMvc.perform(post("/api/tramites/{tramiteId}/flujo/aprobar-rama-paralela", f.tramite.getId().toHexString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content("{\"nodoRamaId\":\"n_rama_a\"}"))
                .andExpect(status().isOk());

        // Segunda vez misma rama: no debe fallar ni duplicar estado
        mockMvc.perform(post("/api/tramites/{tramiteId}/flujo/aprobar-rama-paralela", f.tramite.getId().toHexString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .content("{\"nodoRamaId\":\"n_rama_a\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nodoActualId").value("n_split"));
    }

    private static Fixture fixture(String prioridad, String condicionConexion) {
        ObjectId nodeArea = new ObjectId();
        ObjectId userId = new ObjectId();
        ObjectId politicaId = new ObjectId();
        ObjectId tramiteId = new ObjectId();

        NodoPoliticaEmbeddable nOrigen = new NodoPoliticaEmbeddable();
        nOrigen.setIdNodo("n_origen");
        nOrigen.setAreaId(nodeArea);
        NodoPoliticaEmbeddable nDestino = new NodoPoliticaEmbeddable();
        nDestino.setIdNodo("n_destino");
        nDestino.setAreaId(nodeArea);

        ConexionFlujoEmbeddable c = new ConexionFlujoEmbeddable();
        c.setIdConexion("c-ok");
        c.setOrigenNodoId("n_origen");
        c.setDestinoNodoId("n_destino");
        c.setTipoFlujo("SECUENCIAL");
        c.setCondicion(condicionConexion);

        PoliticaNegocioDocument p = new PoliticaNegocioDocument();
        p.setId(politicaId);
        p.setNodos(List.of(nOrigen, nDestino));
        p.setConexiones(List.of(c));

        TramiteDocument t = new TramiteDocument();
        t.setId(tramiteId);
        t.setCodigo("TRM-E2E-1");
        t.setAsunto("Asunto");
        t.setDescripcion("Desc");
        t.setEstado("ACTIVO");
        t.setPrioridad(prioridad);
        t.setNumeroTurno(1);
        t.setPoliticaId(politicaId);
        t.setNodoActualId("n_origen");
        t.setAreaActualId(nodeArea);

        UsuarioDocument u = new UsuarioDocument();
        u.setId(userId);
        u.setAreaId(nodeArea);
        u.setEstado(true);

        return new Fixture(t, p, u, nodeArea);
    }

    private record Fixture(
            TramiteDocument tramite,
            PoliticaNegocioDocument politica,
            UsuarioDocument user,
            ObjectId nodeArea) {}

    private static ParallelFixture parallelFixtureWithJoin() {
        ObjectId area = new ObjectId();
        ObjectId userId = new ObjectId();
        ObjectId politicaId = new ObjectId();
        ObjectId tramiteId = new ObjectId();

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
                edge("c-a", "n_split", "n_rama_a", "PARALELO"),
                edge("c-b", "n_split", "n_rama_b", "PARALELO"),
                edge("c-a2", "n_rama_a", "n_mid_a", "SECUENCIAL"),
                edge("c-b2", "n_rama_b", "n_mid_b", "SECUENCIAL"),
                edge("c-a3", "n_mid_a", "n_join", "SECUENCIAL"),
                edge("c-b3", "n_mid_b", "n_join", "SECUENCIAL")));

        TramiteDocument t = new TramiteDocument();
        t.setId(tramiteId);
        t.setCodigo("TRM-E2E-PAR");
        t.setAsunto("Par");
        t.setDescripcion("Par");
        t.setEstado("ACTIVO");
        t.setPrioridad("MEDIA");
        t.setNumeroTurno(2);
        t.setPoliticaId(politicaId);
        t.setNodoActualId("n_split");
        t.setAreaActualId(area);
        t.setParaleloRamasPendientes(List.of());
        t.setParaleloRamasAprobadas(List.of());

        UsuarioDocument u = new UsuarioDocument();
        u.setId(userId);
        u.setAreaId(area);
        u.setEstado(true);

        return new ParallelFixture(t, p, u);
    }

    private static ParallelFixture parallelFixtureWithoutJoin() {
        ParallelFixture f = parallelFixtureWithJoin();
        f.politica.setConexiones(List.of(
                edge("c-a", "n_split", "n_rama_a", "PARALELO"),
                edge("c-b", "n_split", "n_rama_b", "PARALELO"),
                edge("c-a2", "n_rama_a", "n_mid_a", "SECUENCIAL"),
                edge("c-b2", "n_rama_b", "n_mid_b", "SECUENCIAL"),
                edge("c-a3", "n_mid_a", "n_join_a", "SECUENCIAL"),
                edge("c-b3", "n_mid_b", "n_join_b", "SECUENCIAL")));
        return f;
    }

    private static NodoPoliticaEmbeddable nodo(String id, ObjectId area) {
        NodoPoliticaEmbeddable n = new NodoPoliticaEmbeddable();
        n.setIdNodo(id);
        n.setAreaId(area);
        return n;
    }

    private static ConexionFlujoEmbeddable edge(String id, String origen, String destino, String tipo) {
        ConexionFlujoEmbeddable c = new ConexionFlujoEmbeddable();
        c.setIdConexion(id);
        c.setOrigenNodoId(origen);
        c.setDestinoNodoId(destino);
        c.setTipoFlujo(tipo);
        c.setCondicion(null);
        return c;
    }

    private record ParallelFixture(
            TramiteDocument tramite,
            PoliticaNegocioDocument politica,
            UsuarioDocument user) {}
}
