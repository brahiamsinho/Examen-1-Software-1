package com.plataforma.tramites.modules.tramites.service;

import com.plataforma.tramites.modules.politicas.document.PoliticaNegocioDocument;
import com.plataforma.tramites.modules.politicas.model.ConexionFlujoEmbeddable;
import com.plataforma.tramites.modules.politicas.repository.PoliticaNegocioRepository;
import com.plataforma.tramites.modules.tramites.document.TramiteDocument;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TramiteFlujoService {

    private final TramiteRepository tramiteRepository;
    private final PoliticaNegocioRepository politicaNegocioRepository;
    private final TramiteFlujoAutorizacionService autorizacion;
    private final TramitesService tramitesService;

    public TramiteFlujoService(
            TramiteRepository tramiteRepository,
            PoliticaNegocioRepository politicaNegocioRepository,
            TramiteFlujoAutorizacionService autorizacion,
            TramitesService tramitesService) {
        this.tramiteRepository = tramiteRepository;
        this.politicaNegocioRepository = politicaNegocioRepository;
        this.autorizacion = autorizacion;
        this.tramitesService = tramitesService;
    }

    public List<SalidaFlujoDto> listarSalidas(String tramiteId) {
        autorizacion.assertFlujoActor(SecurityContextHolder.getContext().getAuthentication());
        TramiteDocument t = buscar(tramiteId);
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
            return List.of();
        }
        autorizacion.assertResponsablePuedeActuarEnNodo(SecurityContextHolder.getContext().getAuthentication(), p, cur);
        return p.getConexiones().stream()
                .filter(c -> cur.equals(c.getOrigenNodoId()))
                .map(c -> new SalidaFlujoDto(
                        c.getIdConexion(),
                        c.getDestinoNodoId(),
                        c.getTipoFlujo(),
                        c.getCondicion()))
                .toList();
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
        List<ConexionFlujoEmbeddable> paralelas = conexionesParalelasDesde(p, cur);
        if (paralelas.size() >= 2) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Hay bifurcación PARALELO con varias ramas; usá el endpoint de aprobación por rama.");
        }
        autorizacion.assertResponsablePuedeActuarEnNodo(auth, p, cur);
        String idCon = idConexion.trim();
        ConexionFlujoEmbeddable c = p.getConexiones().stream()
                .filter(x -> idCon.equals(x.getIdConexion()))
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "La conexión no existe en la política."));
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
        PoliticaNegocioDocument p = politicaNegocioRepository
                .findById(t.getPoliticaId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Política no encontrada."));
        if (nodoRamaId == null || nodoRamaId.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "nodoRamaId inválido.");
        }
        String rama = nodoRamaId.trim();
        autorizacion.assertResponsablePuedeActuarEnNodo(auth, p, rama);

        if (t.getParaleloSplitNodoId() == null) {
            String cur = t.getNodoActualId();
            List<ConexionFlujoEmbeddable> paralelas = conexionesParalelasDesde(p, cur);
            if (paralelas.size() < 2) {
                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "El nodo actual no tiene bifurcación PARALELO con al menos dos salidas.");
            }
            List<String> pendientes =
                    paralelas.stream().map(ConexionFlujoEmbeddable::getDestinoNodoId).distinct().toList();
            String join = calcularJoinDirecto(p, pendientes);
            if (join == null || join.isBlank()) {
                throw new ApiException(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "No hay nodo join común directo desde las ramas paralelas; revisá el grafo (cada rama debe apuntar al mismo destino en un paso).");
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

    private static List<ConexionFlujoEmbeddable> conexionesParalelasDesde(PoliticaNegocioDocument p, String origen) {
        if (origen == null || origen.isBlank()) {
            return List.of();
        }
        return p.getConexiones().stream()
                .filter(c -> origen.equals(c.getOrigenNodoId()))
                .filter(c -> c.getTipoFlujo() != null && "PARALELO".equalsIgnoreCase(c.getTipoFlujo().trim()))
                .filter(c -> c.getDestinoNodoId() != null && !c.getDestinoNodoId().isBlank())
                .toList();
    }

    /**
     * Intersección de los destinos directos de cada rama (un solo nodo común).
     */
    private static String calcularJoinDirecto(PoliticaNegocioDocument p, List<String> ramas) {
        if (ramas.isEmpty()) {
            return null;
        }
        Set<String> inter = null;
        for (String rama : ramas) {
            Set<String> destinos = p.getConexiones().stream()
                    .filter(c -> rama.equals(c.getOrigenNodoId()))
                    .map(ConexionFlujoEmbeddable::getDestinoNodoId)
                    .filter(Objects::nonNull)
                    .filter(d -> !d.isBlank())
                    .collect(Collectors.toSet());
            inter = inter == null ? destinos : intersection(inter, destinos);
            if (inter == null || inter.isEmpty()) {
                return null;
            }
        }
        if (inter.size() == 1) {
            return inter.iterator().next();
        }
        return null;
    }

    private static Set<String> intersection(Set<String> a, Set<String> b) {
        Set<String> r = new HashSet<>(a);
        r.retainAll(b);
        return r;
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
