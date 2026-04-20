package com.plataforma.tramites.modules.documentos.controller;

import com.plataforma.tramites.modules.documentos.service.DocumentosService;
import com.plataforma.tramites.shared.dto.ModuleStatusResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/documentos")
public class DocumentosController {

    private final DocumentosService documentosService;

    public DocumentosController(DocumentosService documentosService) {
        this.documentosService = documentosService;
    }

    @GetMapping("/status")
    public ModuleStatusResponse status() {
        return documentosService.moduleStatus();
    }
}
