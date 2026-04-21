package com.plataforma.tramites.modules.seguimiento.service;

import com.plataforma.tramites.modules.seguimiento.document.BitacoraEventoDocument;
import com.plataforma.tramites.modules.seguimiento.document.NotificacionDocument;
import com.plataforma.tramites.modules.seguimiento.dto.BitacoraEventoCreateRequest;
import com.plataforma.tramites.modules.seguimiento.dto.BitacoraEventoResponse;
import com.plataforma.tramites.modules.seguimiento.dto.NotificacionCreateRequest;
import com.plataforma.tramites.modules.seguimiento.dto.NotificacionResponse;
import com.plataforma.tramites.modules.seguimiento.repository.BitacoraEventoRepository;
import com.plataforma.tramites.modules.seguimiento.repository.NotificacionRepository;
import com.plataforma.tramites.shared.dto.ModuleStatusResponse;
import com.plataforma.tramites.shared.exception.ApiException;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;

@Service
public class SeguimientoService {

    private static final Set<String> TIPOS_NOTIFICACION = Set.of(
            "AVANCE",
            "OBSERVACION",
            "REQUERIMIENTO",
            "APROBACION",
            "RECHAZO",
            "CIERRE");

    private final NotificacionRepository notificacionRepository;
    private final BitacoraEventoRepository bitacoraEventoRepository;

    public SeguimientoService(
            NotificacionRepository notificacionRepository, BitacoraEventoRepository bitacoraEventoRepository) {
        this.notificacionRepository = notificacionRepository;
        this.bitacoraEventoRepository = bitacoraEventoRepository;
    }

    public ModuleStatusResponse moduleStatus() {
        return new ModuleStatusResponse("seguimiento", "bootstrap");
    }

    public Page<NotificacionResponse> listarNotificacionesUsuario(String usuarioId, int page, int size) {
        ObjectId uid = parseOid(usuarioId, "usuarioId");
        return notificacionRepository
                .findByUsuarioIdOrderByFechaEnvioDesc(uid, PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size))))
                .map(this::toNotifResponse);
    }

    public NotificacionResponse crearNotificacion(NotificacionCreateRequest body) {
        if (!TIPOS_NOTIFICACION.contains(body.getTipo().trim())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "tipo de notificación inválido.");
        }
        NotificacionDocument n = new NotificacionDocument();
        n.setTramiteId(parseOid(body.getTramiteId(), "tramiteId"));
        n.setUsuarioId(parseOid(body.getUsuarioId(), "usuarioId"));
        n.setMensaje(body.getMensaje().trim());
        n.setTipo(body.getTipo().trim());
        n.setFechaEnvio(Instant.now());
        n.setLeida(false);
        return toNotifResponse(notificacionRepository.save(n));
    }

    public NotificacionResponse marcarNotificacionLeida(String id) {
        NotificacionDocument n = notificacionRepository
                .findById(parseOid(id, "id"))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Notificación no encontrada."));
        n.setLeida(true);
        return toNotifResponse(notificacionRepository.save(n));
    }

    public Page<BitacoraEventoResponse> listarBitacoraPorTramite(String tramiteId, int page, int size) {
        ObjectId tid = parseOid(tramiteId, "tramiteId");
        return bitacoraEventoRepository
                .findByTramiteIdOrderByFechaHoraDesc(tid, PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size))))
                .map(this::toBitacoraResponse);
    }

    public BitacoraEventoResponse registrarBitacora(BitacoraEventoCreateRequest body) {
        BitacoraEventoDocument b = new BitacoraEventoDocument();
        b.setUsuarioId(parseOid(body.getUsuarioId(), "usuarioId"));
        b.setAccion(body.getAccion().trim());
        b.setDescripcion(body.getDescripcion().trim());
        b.setFechaHora(Instant.now());
        b.setEntidadTipo(body.getEntidadTipo());
        b.setEntidadId(body.getEntidadId());
        if (body.getTramiteId() != null && !body.getTramiteId().isBlank()) {
            b.setTramiteId(parseOid(body.getTramiteId(), "tramiteId"));
        }
        if (body.getPoliticaId() != null && !body.getPoliticaId().isBlank()) {
            b.setPoliticaId(parseOid(body.getPoliticaId(), "politicaId"));
        }
        return toBitacoraResponse(bitacoraEventoRepository.save(b));
    }

    private NotificacionResponse toNotifResponse(NotificacionDocument n) {
        return new NotificacionResponse(
                n.getId().toHexString(),
                n.getTramiteId().toHexString(),
                n.getUsuarioId().toHexString(),
                n.getMensaje(),
                n.getTipo(),
                n.getFechaEnvio(),
                n.isLeida());
    }

    private BitacoraEventoResponse toBitacoraResponse(BitacoraEventoDocument b) {
        return new BitacoraEventoResponse(
                b.getId().toHexString(),
                b.getUsuarioId().toHexString(),
                b.getAccion(),
                b.getDescripcion(),
                b.getFechaHora(),
                b.getEntidadTipo(),
                b.getEntidadId(),
                b.getTramiteId() != null ? b.getTramiteId().toHexString() : null,
                b.getPoliticaId() != null ? b.getPoliticaId().toHexString() : null);
    }

    private static ObjectId parseOid(String hex, String ctx) {
        try {
            return new ObjectId(hex);
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "ObjectId inválido: " + ctx);
        }
    }
}
