package com.plataforma.tramites.modules.planificador.client;

import com.plataforma.tramites.modules.planificador.dto.SugerenciaPoliticaResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class FastApiMlClient {

    private static final Logger log = LoggerFactory.getLogger(FastApiMlClient.class);

    private final WebClient fastApiWebClient;

    public FastApiMlClient(WebClient fastApiWebClient) {
        this.fastApiWebClient = fastApiWebClient;
    }

    public Optional<SugerenciaPoliticaResponse> sugerirPolitica(String tramiteId) {
        try {
            Map<String, Object> body = Map.of("tramite_id", tramiteId);
            @SuppressWarnings("unchecked")
            Map<String, Object> raw = fastApiWebClient.post()
                    .uri("/api/ml/policies/predict")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .onErrorResume(e -> {
                        log.warn("FastAPI ML no disponible: {}", e.getMessage());
                        return Mono.empty();
                    })
                    .block();

            if (raw == null) {
                return Optional.empty();
            }

            SugerenciaPoliticaResponse resp = new SugerenciaPoliticaResponse();
            resp.setPolicyId((String) raw.getOrDefault("policy_id", ""));
            resp.setScoreConfianza(toDouble(raw.get("score_confianza")));
            resp.setExplanation((String) raw.getOrDefault("explanation", null));
            resp.setHumanReviewRequired(Boolean.TRUE.equals(raw.get("human_review_required")));
            return Optional.of(resp);
        } catch (Exception e) {
            log.warn("Error consultando FastAPI ML: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private static double toDouble(Object value) {
        if (value instanceof Number n) return n.doubleValue();
        return 0.0;
    }

    /** Analiza cuellos de botella via FastAPI. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> analizarCuellosBotella(String politicaId) {
        Map<String, Object> body = Map.of("politica_id", politicaId);
        return fastApiWebClient.post()
                .uri("/api/ml/bottlenecks/analyze")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .onErrorResume(e -> {
                    log.warn("FastAPI bottleneck no disponible: {}", e.getMessage());
                    return Mono.just(Map.of(
                            "status", "error",
                            "signals", List.of(),
                            "summary", "Servicio IA no disponible"
                    ));
                })
                .block();
    }
}
