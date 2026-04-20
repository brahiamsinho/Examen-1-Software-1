package com.plataforma.tramites.modules.politicas.controller;

import com.plataforma.tramites.modules.politicas.service.PoliticasService;
import com.plataforma.tramites.shared.dto.ModuleStatusResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/politicas")
public class PoliticasController {

    private final PoliticasService politicasService;

    public PoliticasController(PoliticasService politicasService) {
        this.politicasService = politicasService;
    }

    @GetMapping("/status")
    public ModuleStatusResponse status() {
        return politicasService.moduleStatus();
    }
}
