package com.plataforma.tramites.modules.documentos.service;

import com.plataforma.tramites.modules.documentos.document.DocumentoTramiteDocument;
import com.plataforma.tramites.modules.documentos.document.FormularioTramiteDocument;
import com.plataforma.tramites.modules.documentos.dto.DocumentoTramiteCreateRequest;
import com.plataforma.tramites.modules.documentos.dto.DocumentoTramiteDto;
import com.plataforma.tramites.modules.documentos.dto.FormularioTramiteCreateRequest;
import com.plataforma.tramites.modules.documentos.dto.FormularioTramiteResponse;
import com.plataforma.tramites.modules.documentos.repository.DocumentoTramiteRepository;
import com.plataforma.tramites.modules.documentos.repository.FormularioTramiteRepository;
import com.plataforma.tramites.shared.dto.ModuleStatusResponse;
import com.plataforma.tramites.shared.exception.ApiException;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class DocumentosService {

    private final DocumentoTramiteRepository documentoTramiteRepository;
    private final FormularioTramiteRepository formularioTramiteRepository;

    public DocumentosService(
            DocumentoTramiteRepository documentoTramiteRepository,
            FormularioTramiteRepository formularioTramiteRepository) {
        this.documentoTramiteRepository = documentoTramiteRepository;
        this.formularioTramiteRepository = formularioTramiteRepository;
    }

    public ModuleStatusResponse moduleStatus() {
        return new ModuleStatusResponse("documentos", "bootstrap");
    }

    public List<DocumentoTramiteDto> listarDocumentosPorTramite(String tramiteId) {
        ObjectId tid = parseOid(tramiteId, "tramiteId");
        return documentoTramiteRepository.findByTramiteIdOrderByFechaCargaDesc(tid).stream()
                .map(this::toDto)
                .toList();
    }

    public DocumentoTramiteDto crearDocumento(DocumentoTramiteCreateRequest body) {
        ObjectId tid = parseOid(body.getTramiteId(), "tramiteId");
        DocumentoTramiteDocument d = new DocumentoTramiteDocument();
        d.setTramiteId(tid);
        d.setNodoId(body.getNodoId());
        d.setNombreArchivo(body.getNombreArchivo().trim());
        d.setTipoArchivo(body.getTipoArchivo().trim());
        d.setRutaArchivo(body.getRutaArchivo().trim());
        d.setFechaCarga(Instant.now());
        d.setEstado(body.getEstado().trim());
        return toDto(documentoTramiteRepository.save(d));
    }

    public List<FormularioTramiteResponse> listarFormulariosPorTramite(String tramiteId) {
        ObjectId tid = parseOid(tramiteId, "tramiteId");
        return formularioTramiteRepository.findByTramiteIdOrderByFechaRegistroDesc(tid).stream()
                .map(this::toFormResponse)
                .toList();
    }

    public FormularioTramiteResponse crearFormulario(FormularioTramiteCreateRequest body) {
        ObjectId tid = parseOid(body.getTramiteId(), "tramiteId");
        FormularioTramiteDocument f = new FormularioTramiteDocument();
        f.setTramiteId(tid);
        f.setNodoId(body.getNodoId());
        f.setTitulo(body.getTitulo().trim());
        f.setTipo(body.getTipo().trim());
        f.setContenido(body.getContenido());
        f.setFechaRegistro(Instant.now());
        return toFormResponse(formularioTramiteRepository.save(f));
    }

    private DocumentoTramiteDto toDto(DocumentoTramiteDocument d) {
        return new DocumentoTramiteDto(
                d.getId().toHexString(),
                d.getTramiteId().toHexString(),
                d.getNodoId(),
                d.getNombreArchivo(),
                d.getTipoArchivo(),
                d.getRutaArchivo(),
                d.getEstado(),
                d.getFechaCarga());
    }

    private FormularioTramiteResponse toFormResponse(FormularioTramiteDocument f) {
        return new FormularioTramiteResponse(
                f.getId().toHexString(),
                f.getTramiteId().toHexString(),
                f.getNodoId(),
                f.getTitulo(),
                f.getTipo(),
                f.getContenido(),
                f.getFechaRegistro());
    }

    private static ObjectId parseOid(String hex, String ctx) {
        try {
            return new ObjectId(hex);
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "ObjectId inválido: " + ctx);
        }
    }
}
