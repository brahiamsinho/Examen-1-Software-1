package com.plataforma.tramites.modules.documentos.service;

import com.plataforma.tramites.modules.documentos.repository.DocumentoTramiteRepository;
import com.plataforma.tramites.shared.dto.ModuleStatusResponse;
import org.springframework.stereotype.Service;

@Service
public class DocumentosService {

    private final DocumentoTramiteRepository documentoTramiteRepository;

    public DocumentosService(DocumentoTramiteRepository documentoTramiteRepository) {
        this.documentoTramiteRepository = documentoTramiteRepository;
    }

    public ModuleStatusResponse moduleStatus() {
        return new ModuleStatusResponse("documentos", "bootstrap");
    }
}
