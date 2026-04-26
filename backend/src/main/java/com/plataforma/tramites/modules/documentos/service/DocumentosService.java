package com.plataforma.tramites.modules.documentos.service;

import com.plataforma.tramites.modules.documentos.document.DocumentoTramiteDocument;
import com.plataforma.tramites.modules.documentos.document.FormularioTramiteDocument;
import com.plataforma.tramites.modules.documentos.dto.DocumentoTramiteCreateRequest;
import com.plataforma.tramites.modules.documentos.dto.DocumentoTramiteDto;
import com.plataforma.tramites.modules.documentos.dto.FormularioTramiteCreateRequest;
import com.plataforma.tramites.modules.documentos.dto.FormularioTramiteResponse;
import com.plataforma.tramites.modules.documentos.repository.DocumentoTramiteRepository;
import com.plataforma.tramites.modules.documentos.repository.FormularioTramiteRepository;
import com.plataforma.tramites.modules.politicas.document.PoliticaNegocioDocument;
import com.plataforma.tramites.modules.politicas.repository.PoliticaNegocioRepository;
import com.plataforma.tramites.modules.politicas.support.PoliticaNodoInicialResolver;
import com.plataforma.tramites.modules.politicas.support.PoliticaNodoInicialResolver.NodoInicioPolitica;
import com.plataforma.tramites.modules.tramites.document.TramiteDocument;
import com.plataforma.tramites.modules.tramites.dto.RecorridoTramiteRequest;
import com.plataforma.tramites.modules.tramites.dto.TramiteActualizarRequest;
import com.plataforma.tramites.modules.tramites.repository.TramiteRepository;
import com.plataforma.tramites.modules.tramites.service.TramitesService;
import com.plataforma.tramites.shared.dto.ModuleStatusResponse;
import com.plataforma.tramites.shared.exception.ApiException;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class DocumentosService {

    private static final String RECORRIDO_ESTADO = "ACTIVO";

    private final DocumentoTramiteRepository documentoTramiteRepository;
    private final FormularioTramiteRepository formularioTramiteRepository;
    private final TramiteRepository tramiteRepository;
    private final PoliticaNegocioRepository politicaNegocioRepository;
    private final PoliticaNodoInicialResolver politicaNodoInicialResolver;
    private final TramitesService tramitesService;

    public DocumentosService(
            DocumentoTramiteRepository documentoTramiteRepository,
            FormularioTramiteRepository formularioTramiteRepository,
            TramiteRepository tramiteRepository,
            PoliticaNegocioRepository politicaNegocioRepository,
            PoliticaNodoInicialResolver politicaNodoInicialResolver,
            TramitesService tramitesService) {
        this.documentoTramiteRepository = documentoTramiteRepository;
        this.formularioTramiteRepository = formularioTramiteRepository;
        this.tramiteRepository = tramiteRepository;
        this.politicaNegocioRepository = politicaNegocioRepository;
        this.politicaNodoInicialResolver = politicaNodoInicialResolver;
        this.tramitesService = tramitesService;
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

    /**
     * Si {@code nodoId} viene vacío: se usa el nodo actual del trámite si ya existe; si no, el nodo inicial de la
     * política ({@code esInicial}) y se actualiza el trámite + recorrido para ubicar el expediente en ese nodo.
     */
    public DocumentoTramiteDto crearDocumento(DocumentoTramiteCreateRequest body) {
        ObjectId tid = parseOid(body.getTramiteId(), "tramiteId");
        TramiteDocument tramite = tramiteRepository
                .findById(tid)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Trámite no encontrado."));

        final String nodoIdFinal;
        if (body.getNodoId() != null && !body.getNodoId().isBlank()) {
            nodoIdFinal = body.getNodoId().trim();
        } else if (tramite.getNodoActualId() != null && !tramite.getNodoActualId().isBlank()) {
            nodoIdFinal = tramite.getNodoActualId().trim();
        } else {
            if (tramite.getPoliticaId() == null) {
                throw new ApiException(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "El trámite aún no tiene política asignada ni nodo actual. Debe pasar por planificación.");
            }
            PoliticaNegocioDocument politica = politicaNegocioRepository
                    .findById(tramite.getPoliticaId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Política del trámite no encontrada."));
            NodoInicioPolitica inicio = politicaNodoInicialResolver.resolver(politica);
            ubicarTramiteEnNodoInicial(tramite.getId().toHexString(), inicio);
            nodoIdFinal = inicio.idNodo();
        }

        DocumentoTramiteDocument d = new DocumentoTramiteDocument();
        d.setTramiteId(tid);
        d.setNodoId(nodoIdFinal);
        d.setNombreArchivo(body.getNombreArchivo().trim());
        d.setTipoArchivo(body.getTipoArchivo().trim());
        d.setRutaArchivo(body.getRutaArchivo().trim());
        d.setFechaCarga(Instant.now());
        d.setEstado(body.getEstado().trim());
        return toDto(documentoTramiteRepository.save(d));
    }

    private void ubicarTramiteEnNodoInicial(String tramiteIdHex, NodoInicioPolitica inicio) {
        TramiteActualizarRequest patch = new TramiteActualizarRequest();
        patch.setEstado("EN_PROCESO");
        patch.setNodoActualId(inicio.idNodo());
        if (inicio.areaId() != null) {
            patch.setAreaActualId(inicio.areaId().toHexString());
        }
        tramitesService.actualizar(tramiteIdHex, patch);

        RecorridoTramiteRequest rec = new RecorridoTramiteRequest();
        rec.setNodoId(inicio.idNodo());
        if (inicio.areaId() != null) {
            rec.setAreaId(inicio.areaId().toHexString());
        }
        currentUserId().ifPresent(rec::setUsuarioId);
        rec.setEstado(RECORRIDO_ESTADO);
        rec.setObservacion("Ubicación en nodo inicial por alta de documento sin nodo explícito.");
        tramitesService.registrarRecorrido(tramiteIdHex, rec);
    }

    private static Optional<String> currentUserId() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated()) {
            return Optional.empty();
        }
        Object p = a.getPrincipal();
        if (p instanceof String s && !"anonymousUser".equals(s)) {
            return Optional.of(s);
        }
        return Optional.empty();
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
