package com.plataforma.tramites.modules.planificador.controller;

import com.plataforma.tramites.modules.planificador.dto.PlanificadorAsignarPoliticaRequest;
import com.plataforma.tramites.modules.tramites.dto.TramiteResponse;
import com.plataforma.tramites.modules.tramites.service.TramitesService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/planificador/tramites")
public class PlanificadorTramitesController {

    private final TramitesService tramitesService;

    public PlanificadorTramitesController(TramitesService tramitesService) {
        this.tramitesService = tramitesService;
    }

    /** Trámites ingresados sin política de negocio asignada (pendientes de planificación). */
    @GetMapping("/pendientes-politica")
    public Page<TramiteResponse> listarPendientesPolitica(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return tramitesService.listarSinPolitica(PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size))));
    }

    @PostMapping("/{id}/asignar-politica")
    public TramiteResponse asignarPolitica(@PathVariable String id, @Valid @RequestBody PlanificadorAsignarPoliticaRequest body) {
        String usuarioId = SecurityContextHolder.getContext().getAuthentication().getName();
        return tramitesService.asignarPoliticaDesdeIngreso(id, body.getPoliticaId(), usuarioId);
    }
}
