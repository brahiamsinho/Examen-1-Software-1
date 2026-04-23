package com.plataforma.tramites.modules.politicas.service;

import com.plataforma.tramites.modules.politicas.document.PoliticaNegocioDocument;
import com.plataforma.tramites.modules.politicas.dto.PoliticaNegocioResponse;
import com.plataforma.tramites.modules.politicas.dto.PoliticaUpsertRequest;
import com.plataforma.tramites.modules.politicas.model.AsignacionResponsableEmbeddable;
import com.plataforma.tramites.modules.politicas.model.ConexionFlujoEmbeddable;
import com.plataforma.tramites.modules.politicas.model.NodoPoliticaEmbeddable;
import com.plataforma.tramites.modules.politicas.repository.PoliticaNegocioRepository;
import com.plataforma.tramites.shared.exception.ApiException;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private static final Set<String> TIPOS_NODO = Set.of("INICIO", "ACTIVIDAD", "DECISION", "PARALELO", "FIN");
    private static final Set<String> TIPOS_FLUJO = Set.of("SECUENCIAL", "ALTERNATIVO", "PARALELO", "MULTILINEAL");

    private final PoliticaNegocioRepository politicaNegocioRepository;

    public PoliticasDominioService(PoliticaNegocioRepository politicaNegocioRepository) {
        this.politicaNegocioRepository = politicaNegocioRepository;
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
        doc.setNodos(mapearNodos(body.getNodos()));
        doc.setConexiones(mapearConexiones(body.getConexiones()));
        return toResponse(politicaNegocioRepository.save(doc));
    }

    public PoliticaNegocioResponse reemplazar(String id, PoliticaUpsertRequest body) {
        validarDominio(body);
        PoliticaNegocioDocument doc = politicaNegocioRepository
                .findById(parseObjectId(id))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Política no encontrada."));
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
        doc.setNodos(mapearNodos(body.getNodos()));
        doc.setConexiones(mapearConexiones(body.getConexiones()));
        return toResponse(politicaNegocioRepository.save(doc));
    }

    public void eliminar(String id) {
        ObjectId oid = parseObjectId(id);
        if (!politicaNegocioRepository.existsById(oid)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Política no encontrada.");
        }
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
        return new PoliticaNegocioResponse(
                d.getId().toHexString(),
                d.getNombre(),
                d.getDescripcion(),
                d.getVersion(),
                lock,
                d.getEstado(),
                d.getFechaCreacion(),
                d.getNodos(),
                d.getConexiones());
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
}
