package com.plataforma.tramites.modules.tramites.controller;

import com.plataforma.tramites.modules.tramites.dto.SalidaFlujoDto;
import com.plataforma.tramites.modules.tramites.dto.TramiteAprobarRamaParalelaRequest;
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

import java.util.List;

@RestController
@RequestMapping("/api/tramites/{tramiteId}/flujo")
public class TramiteFlujoController {

    private final TramiteFlujoService tramiteFlujoService;

    public TramiteFlujoController(TramiteFlujoService tramiteFlujoService) {
        this.tramiteFlujoService = tramiteFlujoService;
    }

    @GetMapping("/salidas")
    public List<SalidaFlujoDto> salidas(@PathVariable String tramiteId) {
        return tramiteFlujoService.listarSalidas(tramiteId);
    }

    @PostMapping("/aprobar-rama-paralela")
    public TramiteResponse aprobarRamaParalela(
            @PathVariable String tramiteId, @Valid @RequestBody TramiteAprobarRamaParalelaRequest body) {
        String usuarioId = SecurityContextHolder.getContext().getAuthentication().getName();
        return tramiteFlujoService.aprobarRamaParalela(tramiteId, body.getNodoRamaId(), usuarioId);
    }
}
