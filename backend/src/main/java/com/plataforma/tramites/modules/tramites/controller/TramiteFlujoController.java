package com.plataforma.tramites.modules.tramites.controller;

import com.plataforma.tramites.modules.tramites.dto.FlujoSalidasResponse;
import com.plataforma.tramites.modules.tramites.dto.TramiteAprobarRamaParalelaRequest;
import com.plataforma.tramites.modules.tramites.dto.TramiteFlujoAvanzarRequest;
import com.plataforma.tramites.modules.tramites.dto.TramiteResponse;
import com.plataforma.tramites.modules.tramites.service.TramiteFlujoService;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tramites/{tramiteId}/flujo")
public class TramiteFlujoController {

    private final TramiteFlujoService tramiteFlujoService;

    public TramiteFlujoController(TramiteFlujoService tramiteFlujoService) {
        this.tramiteFlujoService = tramiteFlujoService;
    }

    @GetMapping("/salidas")
    public FlujoSalidasResponse salidas(@PathVariable String tramiteId) {
        return tramiteFlujoService.listarSalidas(tramiteId);
    }

    /** Avance simple (SECUENCIAL / una salida / arista elegida); no bifurcación PARALELO múltiple. */
    @PostMapping("/avanzar")
    public TramiteResponse avanzar(@PathVariable String tramiteId, @Valid @RequestBody TramiteFlujoAvanzarRequest body) {
        String usuarioId = SecurityContextHolder.getContext().getAuthentication().getName();
        return tramiteFlujoService.avanzar(tramiteId, body.getIdConexion(), body.getObservacion(), usuarioId);
    }

    @PostMapping("/aprobar-rama-paralela")
    public TramiteResponse aprobarRamaParalela(
            @PathVariable String tramiteId, @Valid @RequestBody TramiteAprobarRamaParalelaRequest body) {
        String usuarioId = SecurityContextHolder.getContext().getAuthentication().getName();
        return tramiteFlujoService.aprobarRamaParalela(tramiteId, body.getNodoRamaId(), usuarioId);
    }
}
