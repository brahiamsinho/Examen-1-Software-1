package com.plataforma.tramites.modules.tramites.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.plataforma.tramites.modules.documentos.document.FormularioTramiteDocument;
import com.plataforma.tramites.modules.documentos.repository.FormularioTramiteRepository;
import com.plataforma.tramites.modules.politicas.document.PoliticaNegocioDocument;
import com.plataforma.tramites.modules.politicas.model.ConexionFlujoEmbeddable;
import com.plataforma.tramites.modules.politicas.repository.PoliticaNegocioRepository;
import com.plataforma.tramites.modules.politicas.support.PoliticaNodoTerminalResolver;
import com.plataforma.tramites.modules.seguimiento.service.ClienteTramiteEventoNotificacionService;
import com.plataforma.tramites.modules.tramites.document.TramiteDocument;
import com.plataforma.tramites.modules.politicas.model.NodoPoliticaEmbeddable;
import com.plataforma.tramites.modules.tramites.dto.FlujoSalidasResponse;
import com.plataforma.tramites.modules.tramites.dto.RecorridoTramiteRequest;
import com.plataforma.tramites.modules.tramites.dto.SalidaFlujoDto;
import com.plataforma.tramites.modules.tramites.dto.TramiteResponse;
import com.plataforma.tramites.modules.tramites.repository.TramiteRepository;
import com.plataforma.tramites.shared.exception.ApiException;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TramiteFlujoService {

    private final TramiteRepository tramiteRepository;
    private final PoliticaNegocioRepository politicaNegocioRepository;
    private final TramiteFlujoAutorizacionService autorizacion;
    private final TramitesService tramitesService;
    private final FormularioTramiteRepository formularioTramiteRepository;
    private final TramiteFlujoCondicionEvaluator condicionEvaluator;
    private final ClienteTramiteEventoNotificacionService clienteTramiteEventoNotificacionService;
    private final ObjectMapper objectMapper;

    public TramiteFlujoService(
            TramiteRepository tramiteRepository,
            PoliticaNegocioRepository politicaNegocioRepository,
            TramiteFlujoAutorizacionService autorizacion,
            TramitesService tramitesService,
            FormularioTramiteRepository formularioTramiteRepository,
            TramiteFlujoCondicionEvaluator condicionEvaluator,
            ClienteTramiteEventoNotificacionService clienteTramiteEventoNotificacionService,
            ObjectMapper objectMapper) {
        this.tramiteRepository = tramiteRepository;
        this.politicaNegocioRepository = politicaNegocioRepository;
        this.autorizacion = autorizacion;
        this.tramitesService = tramitesService;
        this.formularioTramiteRepository = formularioTramiteRepository;
        this.condicionEvaluator = condicionEvaluator;
        this.clienteTramiteEventoNotificacionService = clienteTramiteEventoNotificacionService;
        this.objectMapper = objectMapper;
    }

    public FlujoSalidasResponse listarSalidas(String tramiteId) {
        autorizacion.assertFlujoActor(SecurityContextHolder.getContext().getAuthentication());
        TramiteDocument t = buscar(tramiteId);
        assertFlujoNoFinalizado(t);
        if (t.getPoliticaId() == null) {
            throw new ApiException(
                    HttpStatus.PRECONDITION_FAILED,
                    "El trámite aún no tiene política asignada. Debe asignarla un planificador.");
        }
        PoliticaNegocioDocument p = politicaNegocioRepository
                .findById(t.getPoliticaId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Política no encontrada."));
        String cur = t.getNodoActualId();
        if (cur == null || cur.isBlank()) {
            return new FlujoSalidasResponse(List.of(), null, null, null, null);
        }
        autorizacion.assertResponsablePuedeActuarEnNodo(SecurityContextHolder.getContext().getAuthentication(), p, cur);
        String nombre = null;
        String carril = null;
        String formularioUrl = null;
        for (NodoPoliticaEmbeddable n : p.getNodos()) {
            if (cur.equals(n.getIdNodo())) {
                nombre = n.getNombre();
                carril = n.getCarrilBpmn();
                formularioUrl = n.getFormularioExternoUrl();
                break;
            }
        }
        List<SalidaFlujoDto> salidas = p.getConexiones().stream()
                .filter(c -> cur.equals(c.getOrigenNodoId()))
                .map(c -> new SalidaFlujoDto(
                        c.getIdConexion(),
                        c.getDestinoNodoId(),
                        c.getTipoFlujo(),
                        c.getCondicion()))
                .toList();
        return new FlujoSalidasResponse(salidas, cur, nombre, carril, formularioUrl);
    }

    /**
     * Avanza el trámite siguiendo una conexión explícita desde el nodo actual. No usar cuando hay dos o más salidas
     * {@code PARALELO} desde el nodo actual (en ese caso {@link #aprobarRamaParalela}). Las condiciones de arista no
     * se evalúan automáticamente: el responsable elige la salida.
     */
    public TramiteResponse avanzar(String tramiteId, String idConexion, String observacion, String usuarioIdHex) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        autorizacion.assertFlujoActor(auth);
        if (idConexion == null || idConexion.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "idConexion es obligatorio.");
        }
        TramiteDocument t = buscar(tramiteId);
        if (t.getPoliticaId() == null) {
            throw new ApiException(
                    HttpStatus.PRECONDITION_FAILED,
                    "El trámite aún no tiene política asignada. Debe asignarla un planificador.");
        }
        assertFlujoNoFinalizado(t);
        if (t.getParaleloSplitNodoId() != null) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "El trámite está en bifurcación paralela pendiente; usá «aprobar rama paralela» por cada departamento.");
        }
        PoliticaNegocioDocument p = politicaNegocioRepository
                .findById(t.getPoliticaId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Política no encontrada."));
        String cur = t.getNodoActualId();
        if (cur == null || cur.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El trámite no tiene nodo actual.");
        }
        Map<String, String> contexto = construirContextoCondiciones(t);
        List<ConexionFlujoEmbeddable> salidasValidas = salidasDesdeNodoConCondicion(p, cur, contexto);
        if (salidasValidas.isEmpty()) {
            throw new ApiException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "SIN_SALIDA_VALIDA: ninguna conexión cumple condición para el estado actual del trámite.");
        }
        List<ConexionFlujoEmbeddable> paralelas = salidasValidas.stream()
                .filter(c -> c.getTipoFlujo() != null && "PARALELO".equalsIgnoreCase(c.getTipoFlujo().trim()))
                .toList();
        if (paralelas.size() >= 2) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Hay bifurcación PARALELO con varias ramas; usá el endpoint de aprobación por rama.");
        }
        autorizacion.assertResponsablePuedeActuarEnNodo(auth, p, cur);
        String idCon = idConexion.trim();
        ConexionFlujoEmbeddable c = salidasValidas.stream()
                .filter(x -> idCon.equals(x.getIdConexion()))
                .findFirst()
                .orElseThrow(() -> new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "La conexión no existe, no sale del nodo actual o no cumple condición."));
        if (!cur.equals(c.getOrigenNodoId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La conexión no sale del nodo actual del trámite.");
        }
        if (c.getDestinoNodoId() == null || c.getDestinoNodoId().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La conexión no tiene destino.");
        }
        if (c.getTipoFlujo() != null && "PARALELO".equalsIgnoreCase(c.getTipoFlujo().trim()) && paralelas.size() == 1) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Salida PARALELO: usá aprobar-rama-paralela cuando corresponda al modelo paralelo.");
        }
        String dest = c.getDestinoNodoId().trim();
        ObjectId areaDest = autorizacion.areaDelNodo(p, dest);
        t.setNodoActualId(dest);
        t.setAreaActualId(areaDest);
        tramiteRepository.save(t);

        RecorridoTramiteRequest rec = new RecorridoTramiteRequest();
        rec.setNodoId(dest);
        if (areaDest != null) {
            rec.setAreaId(areaDest.toHexString());
        }
        rec.setUsuarioId(usuarioIdHex);
        rec.setEstado("ACTIVO");
        String obs = observacion != null && !observacion.isBlank()
                ? observacion.trim()
                : "Avance de flujo por conexión " + idCon + ".";
        rec.setObservacion(obs);
        tramitesService.registrarRecorrido(tramiteId, rec);
        aplicarEstadoTerminalYNotificarCliente(t, p, dest);
        return tramitesService.obtenerInterno(tramiteId);
    }

    /**
     * Aprueba una rama de un split {@code PARALELO} desde el nodo actual. Cuando todas las ramas requeridas tienen
     * visto bueno y existe un <strong>sucesor común directo</strong> desde cada rama, el trámite avanza a ese nodo join.
     */
    public TramiteResponse aprobarRamaParalela(String tramiteId, String nodoRamaId, String usuarioId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        autorizacion.assertFlujoActor(auth);
        TramiteDocument t = buscar(tramiteId);
        if (t.getPoliticaId() == null) {
            throw new ApiException(
                    HttpStatus.PRECONDITION_FAILED,
                    "El trámite aún no tiene política asignada. Debe asignarla un planificador.");
        }
        assertFlujoNoFinalizado(t);
        PoliticaNegocioDocument p = politicaNegocioRepository
                .findById(t.getPoliticaId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Política no encontrada."));
        if (nodoRamaId == null || nodoRamaId.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "nodoRamaId inválido.");
        }
        String rama = nodoRamaId.trim();
        autorizacion.assertResponsablePuedeActuarEnNodo(auth, p, rama);

        Map<String, String> contexto = construirContextoCondiciones(t);
        if (t.getParaleloSplitNodoId() == null) {
            String cur = t.getNodoActualId();
            List<ConexionFlujoEmbeddable> paralelas = conexionesParalelasDesde(p, cur, contexto);
            if (paralelas.size() < 2) {
                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "El nodo actual no tiene bifurcación PARALELO con al menos dos salidas.");
            }
            List<String> pendientes =
                    paralelas.stream().map(ConexionFlujoEmbeddable::getDestinoNodoId).distinct().toList();
            String join = calcularJoinMultiHop(p, pendientes, 12);
            if (join == null || join.isBlank()) {
                throw new ApiException(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "CONVERGENCIA_NO_ENCONTRADA: no hay nodo de convergencia común entre ramas paralelas.");
            }
            t.setParaleloSplitNodoId(cur);
            t.setParaleloRamasPendientes(new ArrayList<>(pendientes));
            t.setParaleloRamasAprobadas(new ArrayList<>());
            t.setParaleloJoinNodoId(join);
            tramiteRepository.save(t);
            t = buscar(tramiteId);
        }

        List<String> pendientes = safeList(t.getParaleloRamasPendientes());
        List<String> aprobadas = safeList(t.getParaleloRamasAprobadas());
        if (!pendientes.contains(rama)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La rama indicada no pertenece al split paralelo actual.");
        }
        if (aprobadas.contains(rama)) {
            return tramitesService.obtenerInterno(tramiteId);
        }
        aprobadas.add(rama);
        t.setParaleloRamasAprobadas(aprobadas);
        tramiteRepository.save(t);

        registrarRecorridoRama(tramiteId, rama, usuarioId, t.getParaleloSplitNodoId(), p);

        Set<String> pendSet = new HashSet<>(pendientes);
        Set<String> aprSet = new HashSet<>(aprobadas);
        if (pendSet.equals(aprSet)) {
            String join = Objects.requireNonNull(t.getParaleloJoinNodoId());
            t.setNodoActualId(join);
            ObjectId areaJoin = autorizacion.areaDelNodo(p, join);
            t.setAreaActualId(areaJoin);
            limpiarEstadoParalelo(t);
            tramiteRepository.save(t);
            registrarRecorridoJoin(tramiteId, join, usuarioId, areaJoin);
            aplicarEstadoTerminalYNotificarCliente(t, p, join);
        }
        return tramitesService.obtenerInterno(tramiteId);
    }

    private static void limpiarEstadoParalelo(TramiteDocument t) {
        t.setParaleloSplitNodoId(null);
        t.setParaleloJoinNodoId(null);
        t.setParaleloRamasPendientes(new ArrayList<>());
        t.setParaleloRamasAprobadas(new ArrayList<>());
    }

    private void registrarRecorridoRama(
            String tramiteId, String nodoRama, String usuarioId, String split, PoliticaNegocioDocument politica) {
        RecorridoTramiteRequest rec = new RecorridoTramiteRequest();
        rec.setNodoId(nodoRama);
        ObjectId areaRama = autorizacion.areaDelNodo(politica, nodoRama);
        if (areaRama != null) {
            rec.setAreaId(areaRama.toHexString());
        }
        rec.setUsuarioId(usuarioId);
        rec.setEstado("APROBADO");
        rec.setObservacion("Visto bueno en rama paralela (split " + split + ").");
        tramitesService.registrarRecorrido(tramiteId, rec);
    }

    private void registrarRecorridoJoin(String tramiteId, String joinNodoId, String usuarioId, ObjectId areaJoin) {
        RecorridoTramiteRequest rec = new RecorridoTramiteRequest();
        rec.setNodoId(joinNodoId);
        if (areaJoin != null) {
            rec.setAreaId(areaJoin.toHexString());
        }
        rec.setUsuarioId(usuarioId);
        rec.setEstado("ACTIVO");
        rec.setObservacion("Unión de ramas paralelas completada; trámite en nodo join.");
        tramitesService.registrarRecorrido(tramiteId, rec);
    }

    private static List<String> safeList(List<String> l) {
        return l != null ? l : new ArrayList<>();
    }

    private List<ConexionFlujoEmbeddable> conexionesParalelasDesde(
            PoliticaNegocioDocument p, String origen, Map<String, String> contexto) {
        if (origen == null || origen.isBlank()) {
            return List.of();
        }
        return p.getConexiones().stream()
                .filter(c -> origen.equals(c.getOrigenNodoId()))
                .filter(c -> condicionEvaluator.evaluate(c.getCondicion(), contexto))
                .filter(c -> c.getTipoFlujo() != null && "PARALELO".equalsIgnoreCase(c.getTipoFlujo().trim()))
                .filter(c -> c.getDestinoNodoId() != null && !c.getDestinoNodoId().isBlank())
                .toList();
    }

    /**
     * Busca convergencia común multi-salto entre ramas paralelas.
     */
    private static String calcularJoinMultiHop(PoliticaNegocioDocument p, List<String> ramas, int maxDepth) {
        if (ramas.isEmpty()) {
            return null;
        }
        List<Map<String, Integer>> distances = new ArrayList<>();
        for (String rama : ramas) {
            Map<String, Integer> d = bfsDistances(p, rama, maxDepth);
            if (d.isEmpty()) {
                return null;
            }
            distances.add(d);
        }
        Set<String> comunes = new HashSet<>(distances.get(0).keySet());
        for (int i = 1; i < distances.size(); i++) {
            comunes.retainAll(distances.get(i).keySet());
            if (comunes.isEmpty()) {
                return null;
            }
        }
        return comunes.stream()
                .min(Comparator
                        .comparingInt((String n) -> distances.stream().mapToInt(m -> m.getOrDefault(n, 999_999)).sum())
                        .thenComparing(s -> s))
                .orElse(null);
    }

    private static Map<String, Integer> bfsDistances(PoliticaNegocioDocument p, String start, int maxDepth) {
        Map<String, Integer> distances = new HashMap<>();
        Deque<String> q = new ArrayDeque<>();
        distances.put(start, 0);
        q.add(start);
        while (!q.isEmpty()) {
            String cur = q.poll();
            int d = distances.get(cur);
            if (d >= maxDepth) {
                continue;
            }
            List<String> next = p.getConexiones().stream()
                    .filter(c -> cur.equals(c.getOrigenNodoId()))
                    .map(ConexionFlujoEmbeddable::getDestinoNodoId)
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .toList();
            for (String n : next) {
                if (!distances.containsKey(n)) {
                    distances.put(n, d + 1);
                    q.add(n);
                }
            }
        }
        distances.remove(start);
        return distances;
    }

    private List<ConexionFlujoEmbeddable> salidasDesdeNodoConCondicion(
            PoliticaNegocioDocument p, String nodoOrigenId, Map<String, String> contexto) {
        return p.getConexiones().stream()
                .filter(c -> nodoOrigenId.equals(c.getOrigenNodoId()))
                .filter(c -> condicionEvaluator.evaluate(c.getCondicion(), contexto))
                .toList();
    }

    private Map<String, String> construirContextoCondiciones(TramiteDocument t) {
        Map<String, String> ctx = new HashMap<>();
        put(ctx, "codigo", t.getCodigo());
        put(ctx, "asunto", t.getAsunto());
        put(ctx, "descripcion", t.getDescripcion());
        put(ctx, "prioridad", t.getPrioridad());
        put(ctx, "estado", t.getEstado());
        put(ctx, "numeroTurno", String.valueOf(t.getNumeroTurno()));
        put(ctx, "nodoActualId", t.getNodoActualId());
        if (t.getAreaActualId() != null) {
            put(ctx, "areaActualId", t.getAreaActualId().toHexString());
        }
        if (t.getPoliticaId() != null) {
            put(ctx, "politicaId", t.getPoliticaId().toHexString());
        }
        if (t.getClienteId() != null) {
            put(ctx, "clienteId", t.getClienteId().toHexString());
        }

        // alias con prefijo tramite.*
        new HashMap<>(ctx).forEach((k, v) -> put(ctx, "tramite." + k, v));

        if (t.getId() != null) {
            FormularioTramiteDocument form = formularioTramiteRepository
                    .findByTramiteIdOrderByFechaRegistroDesc(t.getId())
                    .stream()
                    .findFirst()
                    .orElse(null);
            if (form != null) {
                put(ctx, "form.titulo", form.getTitulo());
                put(ctx, "form.tipo", form.getTipo());
                put(ctx, "form.nodoId", form.getNodoId());
                put(ctx, "form.contenido", form.getContenido());
                if (form.getContenido() != null && !form.getContenido().isBlank()) {
                    try {
                        JsonNode root = objectMapper.readTree(form.getContenido());
                        if (root.isObject()) {
                            root.fields().forEachRemaining(e -> {
                                if (e.getValue() != null && !e.getValue().isContainerNode()) {
                                    String v = e.getValue().asText();
                                    put(ctx, "form." + e.getKey(), v);
                                    put(ctx, e.getKey(), v);
                                }
                            });
                        }
                    } catch (Exception ignored) {
                        // Contenido no JSON: se mantiene solo como string en form.contenido
                    }
                }
            }
        }
        return ctx;
    }

    private static void put(Map<String, String> ctx, String key, String value) {
        if (key == null || key.isBlank() || value == null) {
            return;
        }
        ctx.put(key, value);
    }

    private static void assertFlujoNoFinalizado(TramiteDocument t) {
        String e = t.getEstado();
        if (e == null) {
            return;
        }
        String u = e.trim().toUpperCase();
        if ("APROBADO".equals(u) || "RECHAZADO".equals(u) || "CERRADO".equals(u)) {
            throw new ApiException(
                    HttpStatus.CONFLICT, "El trámite ya finalizó (" + e + "); no se puede modificar el flujo.");
        }
    }

    /**
     * Si el nodo destino es terminal ({@code FIN} o {@code RECHAZO}), actualiza {@link TramiteDocument#getEstado()} y
     * notifica al cliente; si no, notifica solo el avance.
     */
    private void aplicarEstadoTerminalYNotificarCliente(
            TramiteDocument t, PoliticaNegocioDocument politica, String destinoNodoId) {
        if (t.getClienteId() == null) {
            return;
        }
        String cid = t.getClienteId().toHexString();
        String tid = t.getId().toHexString();
        String cod = t.getCodigo();
        PoliticaNodoTerminalResolver.ResultadoTerminal r =
                PoliticaNodoTerminalResolver.clasificar(politica, destinoNodoId);
        if (r == PoliticaNodoTerminalResolver.ResultadoTerminal.APROBADO) {
            t.setEstado("APROBADO");
            tramiteRepository.save(t);
            clienteTramiteEventoNotificacionService.notificarFinalizacionAprobada(cid, tid, cod);
        } else if (r == PoliticaNodoTerminalResolver.ResultadoTerminal.RECHAZADO) {
            t.setEstado("RECHAZADO");
            tramiteRepository.save(t);
            clienteTramiteEventoNotificacionService.notificarFinalizacionRechazo(cid, tid, cod);
        } else {
            clienteTramiteEventoNotificacionService.notificarAvance(cid, tid, cod, destinoNodoId);
        }
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
}
