package com.plataforma.tramites.modules.analitica.controller;

import com.plataforma.tramites.modules.analitica.dto.AnalisisRendimientoCreateRequest;
import com.plataforma.tramites.modules.analitica.dto.AnalisisRendimientoResponse;
import com.plataforma.tramites.modules.analitica.dto.RecomendacionPoliticaCreateRequest;
import com.plataforma.tramites.modules.analitica.dto.RecomendacionPoliticaResponse;
import com.plataforma.tramites.modules.analitica.service.AnaliticaService;
import com.plataforma.tramites.modules.planificador.client.FastApiMlClient;
import com.plataforma.tramites.shared.dto.ModuleStatusResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analitica")
public class AnaliticaController {

    private final AnaliticaService analiticaService;
    private final FastApiMlClient fastApiMlClient;

    public AnaliticaController(AnaliticaService analiticaService, FastApiMlClient fastApiMlClient) {
        this.analiticaService = analiticaService;
        this.fastApiMlClient = fastApiMlClient;
    }

    @GetMapping("/status")
    public ModuleStatusResponse status() {
        return analiticaService.moduleStatus();
    }

    @GetMapping("/politicas/{politicaId}/analisis")
    public List<AnalisisRendimientoResponse> analisisPorPolitica(@PathVariable String politicaId) {
        return analiticaService.listarAnalisisPorPolitica(politicaId);
    }

    @PostMapping("/analisis")
    @ResponseStatus(HttpStatus.CREATED)
    public AnalisisRendimientoResponse crearAnalisis(@Valid @RequestBody AnalisisRendimientoCreateRequest body) {
        return analiticaService.crearAnalisis(body);
    }

    @GetMapping("/politicas/{politicaId}/recomendaciones")
    public List<RecomendacionPoliticaResponse> recomendacionesPorPolitica(@PathVariable String politicaId) {
        return analiticaService.listarRecomendacionesPorPolitica(politicaId);
    }

    @PostMapping("/recomendaciones")
    @ResponseStatus(HttpStatus.CREATED)
    public RecomendacionPoliticaResponse crearRecomendacion(
            @Valid @RequestBody RecomendacionPoliticaCreateRequest body) {
        return analiticaService.crearRecomendacion(body);
    }

    /** Cuellos de botella detectados por IA (FastAPI) para una politica. */
    @GetMapping("/politicas/{politicaId}/cuellos-botella")
    public Map<String, Object> cuellosBotella(@PathVariable String politicaId) {
        return fastApiMlClient.analizarCuellosBotella(politicaId);
    }
}
