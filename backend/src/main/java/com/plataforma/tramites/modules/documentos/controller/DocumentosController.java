package com.plataforma.tramites.modules.documentos.controller;

import com.plataforma.tramites.modules.documentos.dto.DocumentoTramiteCreateRequest;
import com.plataforma.tramites.modules.documentos.dto.DocumentoTramiteDto;
import com.plataforma.tramites.modules.documentos.dto.FormularioTramiteCreateRequest;
import com.plataforma.tramites.modules.documentos.dto.FormularioTramiteResponse;
import com.plataforma.tramites.modules.documentos.service.DocumentosService;
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

    @GetMapping("/tramites/{tramiteId}/archivos")
    public List<DocumentoTramiteDto> listarArchivos(@PathVariable String tramiteId) {
        return documentosService.listarDocumentosPorTramite(tramiteId);
    }

    @PostMapping("/tramites/archivos")
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentoTramiteDto crearArchivo(@Valid @RequestBody DocumentoTramiteCreateRequest body) {
        return documentosService.crearDocumento(body);
    }

    @GetMapping("/tramites/{tramiteId}/formularios")
    public List<FormularioTramiteResponse> listarFormularios(@PathVariable String tramiteId) {
        return documentosService.listarFormulariosPorTramite(tramiteId);
    }

    @PostMapping("/tramites/formularios")
    @ResponseStatus(HttpStatus.CREATED)
    public FormularioTramiteResponse crearFormulario(@Valid @RequestBody FormularioTramiteCreateRequest body) {
        return documentosService.crearFormulario(body);
    }
}
