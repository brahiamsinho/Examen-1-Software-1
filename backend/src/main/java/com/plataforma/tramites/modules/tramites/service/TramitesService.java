package com.plataforma.tramites.modules.tramites.service;

import com.plataforma.tramites.modules.tramites.document.RecorridoTramiteDocument;
import com.plataforma.tramites.modules.tramites.document.TramiteDocument;
import com.plataforma.tramites.modules.tramites.dto.RecorridoTramiteRequest;
import com.plataforma.tramites.modules.tramites.dto.RecorridoTramiteResponse;
import com.plataforma.tramites.modules.tramites.dto.TramiteActualizarRequest;
import com.plataforma.tramites.modules.tramites.dto.TramiteCreateRequest;
import com.plataforma.tramites.modules.tramites.dto.TramiteResponse;
import com.plataforma.tramites.modules.tramites.repository.RecorridoTramiteRepository;
import com.plataforma.tramites.modules.tramites.repository.TramiteRepository;
import com.plataforma.tramites.shared.dto.ModuleStatusResponse;
import com.plataforma.tramites.shared.exception.ApiException;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class TramitesService {

    private static final Set<String> ESTADOS = Set.of(
            "REGISTRADO",
            "EN_PROCESO",
            "OBSERVADO",
            "DERIVADO",
            "APROBADO",
            "RECHAZADO",
            "CERRADO");
    private static final Set<String> PRIORIDADES = Set.of("BAJA", "MEDIA", "ALTA");

    private final TramiteRepository tramiteRepository;
    private final RecorridoTramiteRepository recorridoTramiteRepository;

    public TramitesService(TramiteRepository tramiteRepository, RecorridoTramiteRepository recorridoTramiteRepository) {
        this.tramiteRepository = tramiteRepository;
        this.recorridoTramiteRepository = recorridoTramiteRepository;
    }

    public ModuleStatusResponse moduleStatus() {
        return new ModuleStatusResponse("tramites", "bootstrap");
    }

    public Page<TramiteResponse> listar(Pageable pageable) {
        return tramiteRepository.findAllByOrderByFechaRegistroDesc(pageable).map(this::toResponse);
    }

    public TramiteResponse obtener(String id) {
        return toResponse(buscar(id));
    }

    public TramiteResponse crear(TramiteCreateRequest body) {
        validarPrioridad(body.getPrioridad());
        String codigo = body.getCodigo() == null || body.getCodigo().isBlank()
                ? generarCodigoUnico()
                : body.getCodigo().trim();
        if (tramiteRepository.findByCodigo(codigo).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe un trámite con ese código.");
        }
        int turno = siguienteNumeroTurno();
        TramiteDocument t = new TramiteDocument();
        t.setCodigo(codigo);
        t.setAsunto(body.getAsunto().trim());
        t.setDescripcion(body.getDescripcion().trim());
        t.setFechaRegistro(Instant.now());
        t.setPrioridad(body.getPrioridad().trim());
        t.setEstado("REGISTRADO");
        t.setNumeroTurno(turno);
        t.setPoliticaId(parseObjectId(body.getPoliticaId(), "politicaId"));
        t.setClienteId(parseObjectId(body.getClienteId(), "clienteId"));
        return toResponse(tramiteRepository.save(t));
    }

    public TramiteResponse actualizar(String id, TramiteActualizarRequest body) {
        TramiteDocument t = buscar(id);
        if (!ESTADOS.contains(body.getEstado().trim())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "estado inválido.");
        }
        t.setEstado(body.getEstado().trim());
        if (body.getNodoActualId() != null) {
            t.setNodoActualId(body.getNodoActualId().isBlank() ? null : body.getNodoActualId());
        }
        if (body.getAreaActualId() != null) {
            t.setAreaActualId(body.getAreaActualId().isBlank() ? null : parseObjectId(body.getAreaActualId(), "areaActualId"));
        }
        return toResponse(tramiteRepository.save(t));
    }

    /**
     * Cola FIFO alineada al índice compuesto del script: mismo {@code estado} y misma {@code prioridad},
     * orden por {@code fechaRegistro} y {@code numeroTurno} ascendente.
     */
    public List<TramiteResponse> colaFifo(String estado, String prioridad) {
        if (!ESTADOS.contains(estado)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "estado inválido.");
        }
        if (prioridad == null || prioridad.isBlank()) {
            return tramiteRepository.findByEstadoOrderByFechaRegistroAscNumeroTurnoAsc(estado).stream()
                    .map(this::toResponse)
                    .toList();
        }
        validarPrioridad(prioridad);
        return tramiteRepository
                .findByEstadoAndPrioridadOrderByFechaRegistroAscNumeroTurnoAsc(estado, prioridad.trim())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<RecorridoTramiteResponse> listarRecorridos(String tramiteId) {
        ObjectId tid = parseObjectId(tramiteId, "tramiteId");
        if (!tramiteRepository.existsById(tid)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Trámite no encontrado.");
        }
        return recorridoTramiteRepository.findByTramiteIdOrderByFechaEntradaAsc(tid).stream()
                .map(this::toRecorridoResponse)
                .toList();
    }

    /** Registra un recorrido (entrada a nodo/área). */
    public RecorridoTramiteResponse registrarRecorrido(String tramiteId, RecorridoTramiteRequest body) {
        TramiteDocument t = buscar(tramiteId);
        RecorridoTramiteDocument r = new RecorridoTramiteDocument();
        r.setTramiteId(t.getId());
        r.setNodoId(body.getNodoId());
        if (body.getAreaId() != null && !body.getAreaId().isBlank()) {
            r.setAreaId(parseObjectId(body.getAreaId(), "areaId"));
        }
        if (body.getUsuarioId() != null && !body.getUsuarioId().isBlank()) {
            r.setUsuarioId(parseObjectId(body.getUsuarioId(), "usuarioId"));
        }
        r.setFechaEntrada(body.getFechaEntrada() != null ? body.getFechaEntrada() : Instant.now());
        r.setEstado(body.getEstado());
        r.setObservacion(body.getObservacion());
        return toRecorridoResponse(recorridoTramiteRepository.save(r));
    }

    private TramiteDocument buscar(String id) {
        try {
            return tramiteRepository
                    .findById(new ObjectId(id))
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Trámite no encontrado."));
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Id de trámite inválido.");
        }
    }

    private int siguienteNumeroTurno() {
        return tramiteRepository
                .findTopByOrderByNumeroTurnoDesc()
                .map(d -> d.getNumeroTurno() + 1)
                .orElse(1);
    }

    private String generarCodigoUnico() {
        for (int i = 0; i < 8; i++) {
            String c = "TRM-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
            if (tramiteRepository.findByCodigo(c).isEmpty()) {
                return c;
            }
        }
        throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo generar código único.");
    }

    private void validarPrioridad(String prioridad) {
        if (prioridad == null || !PRIORIDADES.contains(prioridad.trim())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "prioridad inválida. Valores: " + PRIORIDADES);
        }
    }

    private TramiteResponse toResponse(TramiteDocument t) {
        return new TramiteResponse(
                t.getId().toHexString(),
                t.getCodigo(),
                t.getAsunto(),
                t.getDescripcion(),
                t.getFechaRegistro(),
                t.getPrioridad(),
                t.getEstado(),
                t.getNumeroTurno(),
                t.getPoliticaId().toHexString(),
                t.getClienteId().toHexString(),
                t.getNodoActualId(),
                t.getAreaActualId() != null ? t.getAreaActualId().toHexString() : null);
    }

    private RecorridoTramiteResponse toRecorridoResponse(RecorridoTramiteDocument r) {
        return new RecorridoTramiteResponse(
                r.getId().toHexString(),
                r.getTramiteId().toHexString(),
                r.getNodoId(),
                r.getAreaId() != null ? r.getAreaId().toHexString() : null,
                r.getUsuarioId() != null ? r.getUsuarioId().toHexString() : null,
                r.getFechaEntrada(),
                r.getFechaSalida(),
                r.getEstado(),
                r.getObservacion());
    }

    private static ObjectId parseObjectId(String hex, String ctx) {
        try {
            return new ObjectId(hex);
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "ObjectId inválido: " + ctx);
        }
    }
}
