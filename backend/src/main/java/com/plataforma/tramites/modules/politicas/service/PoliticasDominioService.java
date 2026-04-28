package com.plataforma.tramites.modules.politicas.service;

import com.plataforma.tramites.modules.politicas.document.PoliticaNegocioDocument;
import com.plataforma.tramites.modules.politicas.document.PoliticaNegocioRevisionDocument;
import com.plataforma.tramites.modules.politicas.dto.PoliticaNegocioResponse;
import com.plataforma.tramites.modules.politicas.dto.PoliticaRevisionResumenResponse;
import com.plataforma.tramites.modules.politicas.dto.PoliticaUpsertRequest;
import com.plataforma.tramites.modules.politicas.model.AsignacionResponsableEmbeddable;
import com.plataforma.tramites.modules.politicas.model.ConexionFlujoEmbeddable;
import com.plataforma.tramites.modules.politicas.model.NodoPoliticaEmbeddable;
import com.plataforma.tramites.modules.politicas.repository.PoliticaNegocioRepository;
import com.plataforma.tramites.modules.politicas.repository.PoliticaNegocioRevisionRepository;
import com.plataforma.tramites.shared.exception.ApiException;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PoliticasDominioService {

    private static final Set<String> ESTADOS = Set.of("BORRADOR", "PUBLICADA", "INACTIVA");
    private static final Set<String> TIPOS_NODO =
            Set.of("INICIO", "ACTIVIDAD", "DECISION", "PARALELO", "FIN", "RECHAZO");
    private static final Set<String> TIPOS_FLUJO = Set.of("SECUENCIAL", "ALTERNATIVO", "PARALELO", "MULTILINEAL");

    private final PoliticaNegocioRepository politicaNegocioRepository;
    private final PoliticaNegocioRevisionRepository politicaNegocioRevisionRepository;
    private final MongoTemplate mongoTemplate;

    public PoliticasDominioService(
            PoliticaNegocioRepository politicaNegocioRepository,
            PoliticaNegocioRevisionRepository politicaNegocioRevisionRepository,
            MongoTemplate mongoTemplate) {
        this.politicaNegocioRepository = politicaNegocioRepository;
        this.politicaNegocioRevisionRepository = politicaNegocioRevisionRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public Page<PoliticaNegocioResponse> listar(Pageable pageable) {
        return politicaNegocioRepository.findAllByOrderByFechaCreacionDesc(pageable).map(this::toResponse);
    }

    public PoliticaNegocioResponse obtener(String id) {
        PoliticaNegocioDocument doc = politicaNegocioRepository
                .findById(parseObjectId(id))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Política no encontrada."));
        return toResponse(doc);
    }

    public PoliticaNegocioResponse crear(PoliticaUpsertRequest body) {
        validarDominio(body);
        if (politicaNegocioRepository
                .findByNombreIgnoreCaseAndVersion(body.getNombre().trim(), body.getVersion())
                .isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe una política con ese nombre y versión.");
        }
        PoliticaNegocioDocument doc = new PoliticaNegocioDocument();
        doc.setNombre(body.getNombre().trim());
        doc.setDescripcion(body.getDescripcion().trim());
        doc.setVersion(body.getVersion());
        doc.setEstado(body.getEstado().trim());
        doc.setFechaCreacion(Instant.now());
        doc.setBpmnXml(trimToNull(body.getBpmnXml()));
        doc.setNodos(mapearNodos(body.getNodos()));
        doc.setConexiones(mapearConexiones(body.getConexiones()));
        PoliticaNegocioDocument guardada = politicaNegocioRepository.save(doc);
        registrarSnapshotRevision(guardada);
        return toResponse(guardada);
    }

    public PoliticaNegocioResponse reemplazar(String id, PoliticaUpsertRequest body) {
        validarDominio(body);
        PoliticaNegocioDocument doc = politicaNegocioRepository
                .findById(parseObjectId(id))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Política no encontrada."));
        inicializarLockVersionLegacySiHaceFalta(doc);
        if (body.getLockVersion() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "lockVersion es obligatorio al actualizar una política.");
        }
        long serverLock = doc.getLockVersion() == null ? 0L : doc.getLockVersion();
        if (!body.getLockVersion().equals(serverLock)) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "La política fue modificada por otro usuario o en otra pestaña. Recargá y volvé a intentar.");
        }
        politicaNegocioRepository
                .findByNombreIgnoreCaseAndVersion(body.getNombre().trim(), body.getVersion())
                .filter(other -> !other.getId().equals(doc.getId()))
                .ifPresent(x -> {
                    throw new ApiException(HttpStatus.CONFLICT, "Ya existe otra política con ese nombre y versión.");
                });
        doc.setNombre(body.getNombre().trim());
        doc.setDescripcion(body.getDescripcion().trim());
        doc.setVersion(body.getVersion());
        doc.setEstado(body.getEstado().trim());
        doc.setBpmnXml(trimToNull(body.getBpmnXml()));
        doc.setNodos(mapearNodos(body.getNodos()));
        doc.setConexiones(mapearConexiones(body.getConexiones()));
        PoliticaNegocioDocument guardada = politicaNegocioRepository.save(doc);
        registrarSnapshotRevision(guardada);
        return toResponse(guardada);
    }

    public Page<PoliticaRevisionResumenResponse> listarRevisiones(String politicaId, Pageable pageable) {
        ObjectId oid = parseObjectId(politicaId);
        if (!politicaNegocioRepository.existsById(oid)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Política no encontrada.");
        }
        return politicaNegocioRevisionRepository
                .findAllByPoliticaIdOrderByRevisionDesc(oid, pageable)
                .map(r -> new PoliticaRevisionResumenResponse(
                        r.getRevision(), r.getGuardadoEn(), r.getNombre(), r.getVersionNegocio(), r.getEstado()));
    }

    public PoliticaNegocioResponse obtenerRevision(String politicaId, long revision) {
        ObjectId oid = parseObjectId(politicaId);
        if (!politicaNegocioRepository.existsById(oid)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Política no encontrada.");
        }
        PoliticaNegocioRevisionDocument rev = politicaNegocioRevisionRepository
                .findByPoliticaIdAndRevision(oid, revision)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Revisión no encontrada."));
        return toResponseDesdeRevision(rev);
    }

    public void eliminar(String id) {
        ObjectId oid = parseObjectId(id);
        if (!politicaNegocioRepository.existsById(oid)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Política no encontrada.");
        }
        politicaNegocioRevisionRepository.deleteAllByPoliticaId(oid);
        politicaNegocioRepository.deleteById(oid);
    }

    private void validarDominio(PoliticaUpsertRequest body) {
        if (!ESTADOS.contains(body.getEstado().trim())) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "estado inválido. Valores: " + String.join(", ", ESTADOS));
        }
        for (PoliticaUpsertRequest.NodoPoliticaRequest n : body.getNodos()) {
            if (!TIPOS_NODO.contains(n.getTipoNodo())) {
                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "tipoNodo inválido en nodo " + n.getIdNodo() + ". Valores: " + TIPOS_NODO);
            }
            validarFormularioExternoOpcional(n.getFormularioExternoUrl(), n.getIdNodo());
            validarCarrilBpmnOpcional(n.getCarrilBpmn(), n.getIdNodo());
        }
        for (PoliticaUpsertRequest.ConexionFlujoRequest c : body.getConexiones()) {
            if (!TIPOS_FLUJO.contains(c.getTipoFlujo())) {
                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "tipoFlujo inválido en conexión " + c.getIdConexion() + ". Valores: " + TIPOS_FLUJO);
            }
        }
        Set<String> idsNodos =
                body.getNodos().stream().map(PoliticaUpsertRequest.NodoPoliticaRequest::getIdNodo).collect(Collectors.toSet());
        if (idsNodos.size() != body.getNodos().size()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "idNodo duplicado en la lista de nodos.");
        }
        for (PoliticaUpsertRequest.ConexionFlujoRequest c : body.getConexiones()) {
            if (!idsNodos.contains(c.getOrigenNodoId()) || !idsNodos.contains(c.getDestinoNodoId())) {
                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "Conexión " + c.getIdConexion() + " referencia nodos inexistentes.");
            }
        }
        long iniciales = body.getNodos().stream().filter(PoliticaUpsertRequest.NodoPoliticaRequest::isEsInicial).count();
        if (iniciales != 1) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Debe existir exactamente un nodo inicial (esInicial=true).");
        }
    }

    private List<NodoPoliticaEmbeddable> mapearNodos(List<PoliticaUpsertRequest.NodoPoliticaRequest> nodos) {
        return nodos.stream()
                .map(n -> {
                    NodoPoliticaEmbeddable e = new NodoPoliticaEmbeddable();
                    e.setIdNodo(n.getIdNodo());
                    e.setNombre(n.getNombre());
                    e.setTipoNodo(n.getTipoNodo());
                    e.setOrden(n.getOrden());
                    e.setCondicion(n.getCondicion());
                    e.setEsInicial(n.isEsInicial());
                    e.setEsFinal(n.isEsFinal());
                    if (n.getAreaId() != null && !n.getAreaId().isBlank()) {
                        e.setAreaId(parseId(n.getAreaId(), "areaId de nodo " + n.getIdNodo()));
                    }
                    e.setFormularioExternoUrl(trimToNull(n.getFormularioExternoUrl()));
                    e.setCarrilBpmn(trimToNull(n.getCarrilBpmn()));
                    if (n.getAsignacionesResponsable() != null) {
                        e.setAsignacionesResponsable(n.getAsignacionesResponsable().stream()
                                .map(a -> {
                                    AsignacionResponsableEmbeddable x = new AsignacionResponsableEmbeddable();
                                    x.setUsuarioId(parseId(a.getUsuarioId(), "usuarioId en asignación"));
                                    x.setAreaId(parseId(a.getAreaId(), "areaId en asignación"));
                                    x.setFechaAsignacion(
                                            a.getFechaAsignacion() != null ? a.getFechaAsignacion() : Instant.now());
                                    x.setEstado(a.isEstado());
                                    return x;
                                })
                                .toList());
                    }
                    return e;
                })
                .toList();
    }

    private List<ConexionFlujoEmbeddable> mapearConexiones(List<PoliticaUpsertRequest.ConexionFlujoRequest> conexiones) {
        Set<String> ids = new HashSet<>();
        return conexiones.stream()
                .map(c -> {
                    if (!ids.add(c.getIdConexion())) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "idConexion duplicado.");
                    }
                    ConexionFlujoEmbeddable e = new ConexionFlujoEmbeddable();
                    e.setIdConexion(c.getIdConexion());
                    e.setTipoFlujo(c.getTipoFlujo());
                    e.setCondicion(c.getCondicion());
                    e.setOrigenNodoId(c.getOrigenNodoId());
                    e.setDestinoNodoId(c.getDestinoNodoId());
                    return e;
                })
                .toList();
    }

    private PoliticaNegocioResponse toResponse(PoliticaNegocioDocument d) {
        long lock = d.getLockVersion() == null ? 0L : d.getLockVersion();
        List<PoliticaNegocioResponse.NodoPoliticaResponse> nodos = d.getNodos().stream()
                .map(n -> new PoliticaNegocioResponse.NodoPoliticaResponse(
                        n.getIdNodo(),
                        n.getNombre(),
                        n.getTipoNodo(),
                        n.getOrden(),
                        n.getCondicion(),
                        n.isEsInicial(),
                        n.isEsFinal(),
                        n.getAreaId() != null ? n.getAreaId().toHexString() : null,
                        n.getAsignacionesResponsable().stream()
                                .map(a -> new PoliticaNegocioResponse.AsignacionResponsableResponse(
                                        a.getUsuarioId() != null ? a.getUsuarioId().toHexString() : null,
                                        a.getAreaId() != null ? a.getAreaId().toHexString() : null,
                                        a.getFechaAsignacion(),
                                        a.isEstado()))
                                .toList(),
                        n.getFormularioExternoUrl(),
                        n.getCarrilBpmn()))
                .toList();
        List<PoliticaNegocioResponse.ConexionFlujoResponse> conexiones = d.getConexiones().stream()
                .map(c -> new PoliticaNegocioResponse.ConexionFlujoResponse(
                        c.getIdConexion(),
                        c.getTipoFlujo(),
                        c.getCondicion(),
                        c.getOrigenNodoId(),
                        c.getDestinoNodoId()))
                .toList();
        return new PoliticaNegocioResponse(
                d.getId().toHexString(),
                d.getNombre(),
                d.getDescripcion(),
                d.getVersion(),
                lock,
                d.getEstado(),
                d.getFechaCreacion(),
                d.getBpmnXml(),
                nodos,
                conexiones);
    }

    private static ObjectId parseId(String hex, String contexto) {
        try {
            return new ObjectId(hex);
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "ObjectId inválido (" + contexto + ").");
        }
    }

    private static ObjectId parseObjectId(String id) {
        try {
            return new ObjectId(id);
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Id de política inválido.");
        }
    }

    /**
     * Compatibilidad con documentos creados antes de introducir optimistic locking.
     * Si falta {@code lockVersion}, se inicializa en Mongo a 0 para que el próximo
     * guardado use control de concurrencia en vez de disparar inserciones inválidas.
     */
    private void inicializarLockVersionLegacySiHaceFalta(PoliticaNegocioDocument doc) {
        if (doc.getLockVersion() != null) {
            return;
        }
        Query q = Query.query(
                Criteria.where("_id").is(doc.getId())
                        .and("lockVersion").exists(false));
        mongoTemplate.updateFirst(q, new Update().set("lockVersion", 0L), PoliticaNegocioDocument.class);
        doc.setLockVersion(0L);
    }

    private void registrarSnapshotRevision(PoliticaNegocioDocument doc) {
        long nextRev = politicaNegocioRevisionRepository
                .findFirstByPoliticaIdOrderByRevisionDesc(doc.getId())
                .map(PoliticaNegocioRevisionDocument::getRevision)
                .map(r -> r + 1)
                .orElse(1L);
        PoliticaNegocioRevisionDocument r = new PoliticaNegocioRevisionDocument();
        r.setPoliticaId(doc.getId());
        r.setRevision(nextRev);
        r.setGuardadoEn(Instant.now());
        r.setNombre(doc.getNombre());
        r.setDescripcion(doc.getDescripcion());
        r.setVersionNegocio(doc.getVersion());
        r.setEstado(doc.getEstado());
        r.setBpmnXml(doc.getBpmnXml());
        r.setFechaCreacionPolitica(doc.getFechaCreacion());
        r.setLockVersionAlGuardar(doc.getLockVersion());
        r.setNodos(doc.getNodos().stream().map(this::copiarNodo).toList());
        r.setConexiones(doc.getConexiones().stream().map(this::copiarConexion).toList());
        politicaNegocioRevisionRepository.save(r);
    }

    private NodoPoliticaEmbeddable copiarNodo(NodoPoliticaEmbeddable n) {
        NodoPoliticaEmbeddable e = new NodoPoliticaEmbeddable();
        e.setIdNodo(n.getIdNodo());
        e.setNombre(n.getNombre());
        e.setTipoNodo(n.getTipoNodo());
        e.setOrden(n.getOrden());
        e.setCondicion(n.getCondicion());
        e.setEsInicial(n.isEsInicial());
        e.setEsFinal(n.isEsFinal());
        e.setAreaId(n.getAreaId());
        e.setFormularioExternoUrl(n.getFormularioExternoUrl());
        e.setCarrilBpmn(n.getCarrilBpmn());
        if (n.getAsignacionesResponsable() != null) {
            e.setAsignacionesResponsable(
                    n.getAsignacionesResponsable().stream().map(this::copiarAsignacion).toList());
        }
        return e;
    }

    private AsignacionResponsableEmbeddable copiarAsignacion(AsignacionResponsableEmbeddable a) {
        AsignacionResponsableEmbeddable x = new AsignacionResponsableEmbeddable();
        x.setUsuarioId(a.getUsuarioId());
        x.setAreaId(a.getAreaId());
        x.setFechaAsignacion(a.getFechaAsignacion());
        x.setEstado(a.isEstado());
        return x;
    }

    private ConexionFlujoEmbeddable copiarConexion(ConexionFlujoEmbeddable c) {
        ConexionFlujoEmbeddable e = new ConexionFlujoEmbeddable();
        e.setIdConexion(c.getIdConexion());
        e.setTipoFlujo(c.getTipoFlujo());
        e.setCondicion(c.getCondicion());
        e.setOrigenNodoId(c.getOrigenNodoId());
        e.setDestinoNodoId(c.getDestinoNodoId());
        return e;
    }

    private PoliticaNegocioResponse toResponseDesdeRevision(PoliticaNegocioRevisionDocument rev) {
        PoliticaNegocioDocument d = new PoliticaNegocioDocument();
        d.setId(rev.getPoliticaId());
        d.setNombre(rev.getNombre());
        d.setDescripcion(rev.getDescripcion());
        d.setVersion(rev.getVersionNegocio());
        d.setEstado(rev.getEstado());
        d.setBpmnXml(rev.getBpmnXml());
        d.setFechaCreacion(rev.getFechaCreacionPolitica());
        d.setLockVersion(rev.getLockVersionAlGuardar() == null ? 0L : rev.getLockVersionAlGuardar());
        d.setNodos(rev.getNodos());
        d.setConexiones(rev.getConexiones());
        return toResponse(d);
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static void validarFormularioExternoOpcional(String url, String idNodo) {
        if (url == null || url.isBlank()) {
            return;
        }
        String t = url.trim();
        if (t.length() > 2048) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST, "formularioExternoUrl demasiado largo en nodo " + idNodo + ".");
        }
        if (!t.startsWith("https://")) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "formularioExternoUrl debe comenzar con https:// (nodo " + idNodo + ").");
        }
        String lower = t.toLowerCase();
        if (lower.startsWith("javascript:") || lower.contains("://javascript")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "URL de formulario no permitida en nodo " + idNodo + ".");
        }
    }

    private static void validarCarrilBpmnOpcional(String carril, String idNodo) {
        if (carril == null || carril.isBlank()) {
            return;
        }
        if (carril.length() > 160) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "carrilBpmn demasiado largo en nodo " + idNodo + ".");
        }
    }
}
