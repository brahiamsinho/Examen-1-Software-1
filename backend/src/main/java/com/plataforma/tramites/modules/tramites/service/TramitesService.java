package com.plataforma.tramites.modules.tramites.service;

import com.plataforma.tramites.modules.politicas.document.PoliticaNegocioDocument;
import com.plataforma.tramites.modules.politicas.repository.PoliticaNegocioRepository;
import com.plataforma.tramites.modules.politicas.support.PoliticaNodoInicialResolver;
import com.plataforma.tramites.modules.politicas.support.PoliticaNodoInicialResolver.NodoInicioPolitica;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
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
    private final PoliticaNegocioRepository politicaNegocioRepository;
    private final PoliticaNodoInicialResolver politicaNodoInicialResolver;
    private final TramiteFlujoAutorizacionService tramiteFlujoAutorizacionService;
    private final String intakeNodeIdConfig;

    public TramitesService(
            TramiteRepository tramiteRepository,
            RecorridoTramiteRepository recorridoTramiteRepository,
            PoliticaNegocioRepository politicaNegocioRepository,
            PoliticaNodoInicialResolver politicaNodoInicialResolver,
            TramiteFlujoAutorizacionService tramiteFlujoAutorizacionService,
            @Value("${app.workflow.intake-node-id:ATENCION_CLIENTE}") String intakeNodeIdConfig) {
        this.tramiteRepository = tramiteRepository;
        this.recorridoTramiteRepository = recorridoTramiteRepository;
        this.politicaNegocioRepository = politicaNegocioRepository;
        this.politicaNodoInicialResolver = politicaNodoInicialResolver;
        this.tramiteFlujoAutorizacionService = tramiteFlujoAutorizacionService;
        this.intakeNodeIdConfig = intakeNodeIdConfig != null ? intakeNodeIdConfig.trim() : "ATENCION_CLIENTE";
    }

    public ModuleStatusResponse moduleStatus() {
        return new ModuleStatusResponse("tramites", "bootstrap");
    }

    public Page<TramiteResponse> listar(Pageable pageable) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        tramiteFlujoAutorizacionService.assertPuedeListarTramitesEnApi(auth);
        if (tramiteFlujoAutorizacionService.hasRole(auth, TramiteFlujoAutorizacionService.ROL_ADMIN)) {
            return tramiteRepository.findAllByOrderByFechaRegistroDesc(pageable).map(this::toResponse);
        }
        if (tramiteFlujoAutorizacionService.hasRole(auth, TramiteFlujoAutorizacionService.ROL_PLANIFICADOR)) {
            return tramiteRepository.findByPoliticaIdIsNullOrderByFechaRegistroDesc(pageable).map(this::toResponse);
        }
        ObjectId areaId = tramiteFlujoAutorizacionService.requireUsuarioAreaId(auth);
        return tramiteRepository.findByAreaActualIdOrderByFechaRegistroDesc(areaId, pageable).map(this::toResponse);
    }

    public Page<TramiteResponse> listarSinPolitica(Pageable pageable) {
        return tramiteRepository.findByPoliticaIdIsNullOrderByFechaRegistroDesc(pageable).map(this::toResponse);
    }

    public TramiteResponse obtener(String id) {
        TramiteDocument t = buscar(id);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        tramiteFlujoAutorizacionService.assertPuedeConsultarTramiteStaff(auth, t);
        return toResponse(t);
    }

    /** Respuesta del trámite sin validar portal staff (uso interno tras flujo u operaciones ya autorizadas). */
    public TramiteResponse obtenerInterno(String id) {
        return toResponse(buscar(id));
    }

    /** Listado paginado solo de trámites cuyo {@code clienteId} coincide (portal cliente). */
    public Page<TramiteResponse> listarPorClienteId(String clienteIdHex, Pageable pageable) {
        ObjectId clienteId = parseObjectId(clienteIdHex, "clienteId");
        return tramiteRepository.findByClienteIdOrderByFechaRegistroDesc(clienteId, pageable).map(this::toResponse);
    }

    /** Obtiene un trámite solo si pertenece al cliente indicado. */
    public TramiteResponse obtenerDeCliente(String tramiteIdHex, String clienteIdHex) {
        ObjectId tid = parseObjectId(tramiteIdHex, "tramiteId");
        ObjectId cid = parseObjectId(clienteIdHex, "clienteId");
        TramiteDocument t = tramiteRepository
                .findByIdAndClienteId(tid, cid)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Trámite no encontrado."));
        return toResponse(t);
    }

    /**
     * Asigna la política de negocio a un trámite que ingresó sin política y está en el nodo de atención al cliente;
     * ubica el expediente en el nodo inicial de esa política ({@code esInicial=true}).
     */
    public TramiteResponse asignarPoliticaDesdeIngreso(String tramiteId, String politicaIdHex, String usuarioPlanificadorHex) {
        TramiteDocument t = buscar(tramiteId);
        if (t.getPoliticaId() != null) {
            throw new ApiException(HttpStatus.CONFLICT, "El trámite ya tiene política asignada.");
        }
        if (t.getNodoActualId() == null || !intakeNodeIdConfig.equals(t.getNodoActualId())) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "El trámite no está en el nodo de ingreso esperado (" + intakeNodeIdConfig + ").");
        }
        ObjectId pid = parseObjectId(politicaIdHex, "politicaId");
        PoliticaNegocioDocument politica = politicaNegocioRepository
                .findById(pid)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Política no encontrada."));
        NodoInicioPolitica inicio = politicaNodoInicialResolver.resolver(politica);
        t.setPoliticaId(politica.getId());
        t.setNodoActualId(inicio.idNodo());
        t.setAreaActualId(inicio.areaId());
        t.setParaleloSplitNodoId(null);
        t.setParaleloJoinNodoId(null);
        t.setParaleloRamasPendientes(new ArrayList<>());
        t.setParaleloRamasAprobadas(new ArrayList<>());
        tramiteRepository.save(t);

        RecorridoTramiteRequest rec = new RecorridoTramiteRequest();
        rec.setNodoId(inicio.idNodo());
        if (inicio.areaId() != null) {
            rec.setAreaId(inicio.areaId().toHexString());
        }
        if (usuarioPlanificadorHex != null && !usuarioPlanificadorHex.isBlank()) {
            rec.setUsuarioId(usuarioPlanificadorHex);
        }
        rec.setEstado("ACTIVO");
        String nombrePolitica = politica.getNombre() != null ? politica.getNombre() : "(sin nombre)";
        rec.setObservacion("Planificador asignó política «" + nombrePolitica + "» y ubicación en nodo inicial del flujo.");
        registrarRecorrido(tramiteId, rec);
        return toResponse(buscar(tramiteId));
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

    /**
     * Alta inicial desde portal cliente sin política asignada todavía.
     * El planificador define la política después del triaje en atención al cliente.
     */
    public TramiteResponse crearIngresoCliente(
            String clienteId,
            String asunto,
            String descripcion,
            String prioridad,
            String nodoIngresoId,
            String areaIngresoId) {
        validarPrioridad(prioridad);
        String codigo = generarCodigoUnico();
        int turno = siguienteNumeroTurno();
        TramiteDocument t = new TramiteDocument();
        t.setCodigo(codigo);
        t.setAsunto(asunto.trim());
        t.setDescripcion(descripcion.trim());
        t.setFechaRegistro(Instant.now());
        t.setPrioridad(prioridad.trim());
        t.setEstado("EN_PROCESO");
        t.setNumeroTurno(turno);
        t.setClienteId(parseObjectId(clienteId, "clienteId"));
        t.setNodoActualId(nodoIngresoId);
        if (areaIngresoId != null && !areaIngresoId.isBlank()) {
            t.setAreaActualId(parseObjectId(areaIngresoId, "areaActualId"));
        }
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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        tramiteFlujoAutorizacionService.assertPuedeUsarColaFifo(auth);
        if (tramiteFlujoAutorizacionService.hasRole(auth, TramiteFlujoAutorizacionService.ROL_RESPONSABLE)) {
            ObjectId areaId = tramiteFlujoAutorizacionService.requireUsuarioAreaId(auth);
            if (prioridad == null || prioridad.isBlank()) {
                return tramiteRepository
                        .findByEstadoAndAreaActualIdOrderByFechaRegistroAscNumeroTurnoAsc(estado, areaId)
                        .stream()
                        .map(this::toResponse)
                        .toList();
            }
            validarPrioridad(prioridad);
            return tramiteRepository
                    .findByEstadoAndPrioridadAndAreaActualIdOrderByFechaRegistroAscNumeroTurnoAsc(
                            estado, prioridad.trim(), areaId)
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }
        if (tramiteFlujoAutorizacionService.hasRole(auth, TramiteFlujoAutorizacionService.ROL_PLANIFICADOR)) {
            if (prioridad == null || prioridad.isBlank()) {
                return tramiteRepository.findByEstadoOrderByFechaRegistroAscNumeroTurnoAsc(estado).stream()
                        .filter(t -> t.getPoliticaId() == null)
                        .map(this::toResponse)
                        .toList();
            }
            validarPrioridad(prioridad);
            return tramiteRepository
                    .findByEstadoAndPrioridadOrderByFechaRegistroAscNumeroTurnoAsc(estado, prioridad.trim())
                    .stream()
                    .filter(t -> t.getPoliticaId() == null)
                    .map(this::toResponse)
                    .toList();
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
        TramiteDocument t = buscar(tramiteId);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        tramiteFlujoAutorizacionService.assertPuedeConsultarTramiteStaff(auth, t);
        return recorridosDeTramite(t.getId());
    }

    /**
     * Historial de recorridos sin chequeo de rol de staff — invocar solo tras verificar acceso (p. ej. portal
     * cliente).
     */
    public List<RecorridoTramiteResponse> listarRecorridosInterno(String tramiteId) {
        ObjectId tid = parseObjectId(tramiteId, "tramiteId");
        if (!tramiteRepository.existsById(tid)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Trámite no encontrado.");
        }
        return recorridosDeTramite(tid);
    }

    private List<RecorridoTramiteResponse> recorridosDeTramite(ObjectId tramiteId) {
        return recorridoTramiteRepository.findByTramiteIdOrderByFechaEntradaAsc(tramiteId).stream()
                .map(this::toRecorridoResponse)
                .toList();
    }

    /** Registro vía {@code POST /api/tramites/{id}/recorridos} (política de área / admin). */
    public RecorridoTramiteResponse registrarRecorridoDesdeApi(String tramiteId, RecorridoTramiteRequest body) {
        TramiteDocument t = buscar(tramiteId);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        tramiteFlujoAutorizacionService.assertPuedeRegistrarRecorridoViaApiTramites(auth, t, body);
        return registrarRecorrido(tramiteId, body);
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
                t.getPoliticaId() != null ? t.getPoliticaId().toHexString() : null,
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
