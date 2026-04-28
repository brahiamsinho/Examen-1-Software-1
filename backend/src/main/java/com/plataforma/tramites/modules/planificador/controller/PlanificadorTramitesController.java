package com.plataforma.tramites.modules.planificador.controller;

import com.plataforma.tramites.modules.planificador.client.FastApiMlClient;
import com.plataforma.tramites.modules.planificador.dto.PlanificadorAsignarPoliticaRequest;
import com.plataforma.tramites.modules.planificador.dto.RegistrarDecisionIaRequest;
import com.plataforma.tramites.modules.planificador.dto.SugerenciaPoliticaResponse;
import com.plataforma.tramites.modules.planificador.service.DecisionIaAuditService;
import com.plataforma.tramites.modules.tramites.dto.TramiteResponse;
import com.plataforma.tramites.modules.tramites.service.TramitesService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/planificador/tramites")
public class PlanificadorTramitesController {

    private final TramitesService tramitesService;
    private final FastApiMlClient fastApiMlClient;
    private final DecisionIaAuditService decisionIaAuditService;

    public PlanificadorTramitesController(TramitesService tramitesService,
                                          FastApiMlClient fastApiMlClient,
                                          DecisionIaAuditService decisionIaAuditService) {
        this.tramitesService = tramitesService;
        this.fastApiMlClient = fastApiMlClient;
        this.decisionIaAuditService = decisionIaAuditService;
    }

    /** Trámites ingresados sin política de negocio asignada (pendientes de planificación). */
    @GetMapping("/pendientes-politica")
    public Page<TramiteResponse> listarPendientesPolitica(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return tramitesService.listarSinPolitica(PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size))));
    }

    /** Sugerencia IA de politica para tramite pendiente. FastAPI caido → 503. */
    @GetMapping("/{id}/sugerir-politica")
    public ResponseEntity<?> sugerirPolitica(@PathVariable String id) {
        Optional<SugerenciaPoliticaResponse> sugerencia = fastApiMlClient.sugerirPolitica(id);
        return sugerencia
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(503)
                        .body(Map.of("error", "servicio_ia_no_disponible",
                                "mensaje", "FastAPI ML no responde. El planificador sigue funcionando.")));
    }

    /** Registrar decision humana sobre sugerencia IA (aceptada/rechazada). */
    @PostMapping("/{id}/registrar-decision-ia")
    public ResponseEntity<Map<String, Object>> registrarDecisionIa(
            @PathVariable String id, @Valid @RequestBody RegistrarDecisionIaRequest body) {
        String usuarioId = SecurityContextHolder.getContext().getAuthentication().getName();
        decisionIaAuditService.registrar(id, body, usuarioId);
        return ResponseEntity.ok(Map.of("status", "registrado",
                "tramiteId", id, "aceptada", body.isAceptada()));
    }

    @PostMapping("/{id}/asignar-politica")
    public TramiteResponse asignarPolitica(@PathVariable String id, @Valid @RequestBody PlanificadorAsignarPoliticaRequest body) {
        String usuarioId = SecurityContextHolder.getContext().getAuthentication().getName();
        return tramitesService.asignarPoliticaDesdeIngreso(id, body.getPoliticaId(), usuarioId);
    }
}
