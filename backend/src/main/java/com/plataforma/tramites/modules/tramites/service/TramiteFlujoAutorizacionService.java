package com.plataforma.tramites.modules.tramites.service;

import com.plataforma.tramites.modules.politicas.document.PoliticaNegocioDocument;
import com.plataforma.tramites.modules.politicas.model.NodoPoliticaEmbeddable;
import com.plataforma.tramites.modules.politicas.repository.PoliticaNegocioRepository;
import com.plataforma.tramites.modules.seguridad.document.UsuarioDocument;
import com.plataforma.tramites.modules.seguridad.repository.UsuarioRepository;
import com.plataforma.tramites.modules.tramites.document.TramiteDocument;
import com.plataforma.tramites.modules.tramites.dto.RecorridoTramiteRequest;
import com.plataforma.tramites.shared.exception.ApiException;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Autorización compartida entre {@link TramiteFlujoService} y las APIs de trámites/recorridos para staff
 * (administrador, responsable de área, planificador).
 */
@Service
public class TramiteFlujoAutorizacionService {

    public static final String ROL_ADMIN = "ROLE_ADMINISTRADOR";
    public static final String ROL_RESPONSABLE = "ROLE_RESPONSABLE_AREA";
    public static final String ROL_PLANIFICADOR = "ROLE_PLANIFICADOR";

    private final UsuarioRepository usuarioRepository;
    private final PoliticaNegocioRepository politicaNegocioRepository;

    public TramiteFlujoAutorizacionService(
            UsuarioRepository usuarioRepository, PoliticaNegocioRepository politicaNegocioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.politicaNegocioRepository = politicaNegocioRepository;
    }

    public void assertFlujoActor(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "No autenticado.");
        }
        boolean ok = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> ROL_ADMIN.equals(a) || ROL_RESPONSABLE.equals(a));
        if (!ok) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "Solo responsables de área o administradores pueden consultar o modificar el flujo del trámite.");
        }
    }

    public void assertResponsablePuedeActuarEnNodo(Authentication auth, PoliticaNegocioDocument p, String nodoId) {
        if (nodoId == null || nodoId.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Nodo inválido.");
        }
        if (hasRole(auth, ROL_ADMIN)) {
            return;
        }
        if (!ObjectId.isValid(auth.getName())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Sesión inválida.");
        }
        ObjectId uid = new ObjectId(auth.getName());
        UsuarioDocument u = usuarioRepository
                .findById(uid)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));
        ObjectId userArea = u.getAreaId();
        ObjectId nodeArea = areaDelNodo(p, nodoId);
        if (nodeArea == null) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "El nodo no tiene área asignada en la política; solo un administrador puede actuar aquí.");
        }
        if (userArea == null || !userArea.equals(nodeArea)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Tu área no corresponde a esta etapa del trámite.");
        }
    }

    /**
     * Lectura de expediente vía {@code /api/tramites/**} (no portal cliente). Planificador: solo trámites sin
     * política. Responsable: área actual o rama paralela pendiente de su área.
     */
    public void assertPuedeConsultarTramiteStaff(Authentication auth, TramiteDocument t) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "No autenticado.");
        }
        if (hasRole(auth, ROL_ADMIN)) {
            return;
        }
        if (hasRole(auth, ROL_PLANIFICADOR)) {
            if (t.getPoliticaId() != null) {
                throw new ApiException(
                        HttpStatus.FORBIDDEN,
                        "Los planificadores solo acceden a trámites pendientes de asignación de política.");
            }
            return;
        }
        if (hasRole(auth, ROL_RESPONSABLE)) {
            ObjectId ua = requireUsuarioAreaId(auth);
            if (t.getAreaActualId() != null && ua.equals(t.getAreaActualId())) {
                return;
            }
            if (t.getPoliticaId() != null && t.getParaleloSplitNodoId() != null && t.getParaleloRamasPendientes() != null) {
                PoliticaNegocioDocument p = politicaNegocioRepository.findById(t.getPoliticaId()).orElse(null);
                if (p != null) {
                    for (String rama : t.getParaleloRamasPendientes()) {
                        ObjectId ar = areaDelNodo(p, rama);
                        if (ar != null && ar.equals(ua)) {
                            return;
                        }
                    }
                }
            }
            throw new ApiException(HttpStatus.FORBIDDEN, "No tenés visibilidad de este trámite.");
        }
        throw new ApiException(HttpStatus.FORBIDDEN, "No tenés permiso para consultar este trámite.");
    }

    /**
     * {@code POST /api/tramites/{id}/recorridos}: mismo actor que el flujo; sin política solo administrador; con
     * política, el nodo del cuerpo debe ser del área del responsable (admin sin límite). Ajusta {@code areaId} del
     * cuerpo según la política para usuarios no admin.
     */
    public void assertPuedeRegistrarRecorridoViaApiTramites(Authentication auth, TramiteDocument t, RecorridoTramiteRequest body) {
        assertFlujoActor(auth);
        if (t.getPoliticaId() == null) {
            if (!hasRole(auth, ROL_ADMIN)) {
                throw new ApiException(
                        HttpStatus.FORBIDDEN,
                        "Sin política asignada solo un administrador puede registrar recorridos por esta API.");
            }
            return;
        }
        PoliticaNegocioDocument p = politicaNegocioRepository
                .findById(t.getPoliticaId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Política no encontrada."));
        assertResponsablePuedeActuarEnNodo(auth, p, body.getNodoId());
        if (!hasRole(auth, ROL_ADMIN)) {
            ObjectId areaNodo = areaDelNodo(p, body.getNodoId());
            if (areaNodo != null) {
                body.setAreaId(areaNodo.toHexString());
            }
        }
    }

    public void assertAutenticado(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "No autenticado.");
        }
    }

    public void assertPuedeListarTramitesEnApi(Authentication auth) {
        assertAutenticado(auth);
        if (hasRole(auth, ROL_ADMIN)
                || hasRole(auth, ROL_RESPONSABLE)
                || hasRole(auth, ROL_PLANIFICADOR)) {
            return;
        }
        throw new ApiException(HttpStatus.FORBIDDEN, "No tenés permiso para listar trámites en este endpoint.");
    }

    public void assertPuedeUsarColaFifo(Authentication auth) {
        assertAutenticado(auth);
        if (hasRole(auth, ROL_ADMIN)
                || hasRole(auth, ROL_RESPONSABLE)
                || hasRole(auth, ROL_PLANIFICADOR)) {
            return;
        }
        throw new ApiException(HttpStatus.FORBIDDEN, "No tenés permiso para usar la cola de trámites.");
    }

    public boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream().anyMatch(a -> role.equals(a.getAuthority()));
    }

    public ObjectId requireUsuarioAreaId(Authentication auth) {
        if (!ObjectId.isValid(auth.getName())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Sesión inválida.");
        }
        UsuarioDocument u = usuarioRepository
                .findById(new ObjectId(auth.getName()))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));
        ObjectId area = u.getAreaId();
        if (area == null) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Tu usuario no tiene área asignada.");
        }
        return area;
    }

    public ObjectId areaDelNodo(PoliticaNegocioDocument p, String idNodo) {
        return p.getNodos().stream()
                .filter(n -> idNodo.equals(n.getIdNodo()))
                .map(NodoPoliticaEmbeddable::getAreaId)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

}
