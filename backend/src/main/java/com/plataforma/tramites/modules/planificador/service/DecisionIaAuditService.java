package com.plataforma.tramites.modules.planificador.service;

import com.plataforma.tramites.modules.planificador.dto.RegistrarDecisionIaRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DecisionIaAuditService {

    private static final Logger log = LoggerFactory.getLogger(DecisionIaAuditService.class);

    private final ConcurrentHashMap<String, List<Map<String, Object>>> auditoria = new ConcurrentHashMap<>();

    public void registrar(String tramiteId, RegistrarDecisionIaRequest body, String usuarioId) {
        Map<String, Object> entry = Map.of(
                "tramiteId", tramiteId,
                "usuarioId", usuarioId,
                "policyIdSugerida", body.getPolicyIdSugerida(),
                "scoreConfianza", body.getScoreConfianza(),
                "aceptada", body.isAceptada(),
                "fecha", Instant.now().toString()
        );
        auditoria.computeIfAbsent(tramiteId, k -> new ArrayList<>()).add(entry);
        log.info("Decision IA registrada: tramite={} usuario={} sugerida={} aceptada={} score={}",
                tramiteId, usuarioId, body.getPolicyIdSugerida(), body.isAceptada(), body.getScoreConfianza());
    }

    public List<Map<String, Object>> listarPorTramite(String tramiteId) {
        return auditoria.getOrDefault(tramiteId, List.of());
    }
}
