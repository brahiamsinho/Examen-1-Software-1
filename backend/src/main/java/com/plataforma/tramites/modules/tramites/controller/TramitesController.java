package com.plataforma.tramites.modules.tramites.controller;

import com.plataforma.tramites.modules.tramites.service.TramitesService;
import com.plataforma.tramites.shared.dto.ModuleStatusResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tramites")
public class TramitesController {

    private final TramitesService tramitesService;

    public TramitesController(TramitesService tramitesService) {
        this.tramitesService = tramitesService;
    }

    @GetMapping("/status")
    public ModuleStatusResponse status() {
        return tramitesService.moduleStatus();
    }
}
