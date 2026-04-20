package com.plataforma.tramites.modules.analitica.controller;

import com.plataforma.tramites.modules.analitica.service.AnaliticaService;
import com.plataforma.tramites.shared.dto.ModuleStatusResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analitica")
public class AnaliticaController {

    private final AnaliticaService analiticaService;

    public AnaliticaController(AnaliticaService analiticaService) {
        this.analiticaService = analiticaService;
    }

    @GetMapping("/status")
    public ModuleStatusResponse status() {
        return analiticaService.moduleStatus();
    }
}
