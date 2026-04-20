package com.plataforma.tramites.modules.seguimiento.controller;

import com.plataforma.tramites.modules.seguimiento.service.SeguimientoService;
import com.plataforma.tramites.shared.dto.ModuleStatusResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seguimiento")
public class SeguimientoController {

    private final SeguimientoService seguimientoService;

    public SeguimientoController(SeguimientoService seguimientoService) {
        this.seguimientoService = seguimientoService;
    }

    @GetMapping("/status")
    public ModuleStatusResponse status() {
        return seguimientoService.moduleStatus();
    }
}
