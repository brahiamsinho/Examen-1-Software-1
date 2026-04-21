package com.plataforma.tramites.modules.seguridad.controller;

import com.plataforma.tramites.modules.seguridad.service.SeguridadService;
import com.plataforma.tramites.shared.dto.InfraHealthResponse;
import com.plataforma.tramites.shared.dto.ModuleStatusResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seguridad")
public class SeguridadController {

    private final SeguridadService seguridadService;

    public SeguridadController(SeguridadService seguridadService) {
        this.seguridadService = seguridadService;
    }

    @GetMapping("/status")
    public ModuleStatusResponse status() {
        return seguridadService.moduleStatus();
    }

    /** Diagnóstico de MongoDB y Redis (variables {@code spring.data.mongodb.*} y {@code spring.data.redis.*}). */
    @GetMapping("/infra")
    public InfraHealthResponse infra() {
        return seguridadService.checkInfra();
    }
}
