package com.plataforma.tramites.modules.seguimiento.service;

import com.plataforma.tramites.modules.seguimiento.dto.NotificacionCreateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Notifica al cliente por push (FCM) y persiste el mismo mensaje en {@code notificaciones} (in-app).
 */
@Service
public class ClienteTramiteEventoNotificacionService {

    private static final Logger log = LoggerFactory.getLogger(ClienteTramiteEventoNotificacionService.class);

    private final FcmNotificationService fcmNotificationService;
    private final SeguimientoService seguimientoService;

    public ClienteTramiteEventoNotificacionService(
            FcmNotificationService fcmNotificationService, SeguimientoService seguimientoService) {
        this.fcmNotificationService = fcmNotificationService;
        this.seguimientoService = seguimientoService;
    }

    public void notificarPoliticaAsignada(String clienteIdHex, String tramiteIdHex, String codigoTramite, String nombrePolitica) {
        if (clienteIdHex == null || clienteIdHex.isBlank()) {
            return;
        }
        String titulo = "Política asignada a tu trámite";
        String cuerpo = "Tu trámite "
                + codigoTramite
                + " ya tiene política de negocio: "
                + (nombrePolitica != null && !nombrePolitica.isBlank() ? nombrePolitica : "(sin nombre)")
                + ".";
        enviar(clienteIdHex, tramiteIdHex, codigoTramite, titulo, cuerpo, "POLITICA_ASIGNADA");
    }

    public void notificarAvance(String clienteIdHex, String tramiteIdHex, String codigoTramite, String destinoNodoId) {
        if (clienteIdHex == null || clienteIdHex.isBlank()) {
            return;
        }
        String titulo = "Trámite actualizado";
        String cuerpo = "Tu trámite " + codigoTramite + " avanzó al nodo: " + destinoNodoId + ".";
        enviar(clienteIdHex, tramiteIdHex, codigoTramite, titulo, cuerpo, "AVANCE");
    }

    public void notificarFinalizacionAprobada(String clienteIdHex, String tramiteIdHex, String codigoTramite) {
        if (clienteIdHex == null || clienteIdHex.isBlank()) {
            return;
        }
        String titulo = "Trámite finalizado";
        String cuerpo = "Tu trámite " + codigoTramite + " finalizó correctamente (nodo fin de proceso).";
        enviar(clienteIdHex, tramiteIdHex, codigoTramite, titulo, cuerpo, "CIERRE");
    }

    public void notificarFinalizacionRechazo(String clienteIdHex, String tramiteIdHex, String codigoTramite) {
        if (clienteIdHex == null || clienteIdHex.isBlank()) {
            return;
        }
        String titulo = "Trámite rechazado";
        String cuerpo = "Tu trámite " + codigoTramite + " fue rechazado en el proceso.";
        enviar(clienteIdHex, tramiteIdHex, codigoTramite, titulo, cuerpo, "RECHAZO");
    }

    private void enviar(
            String clienteIdHex,
            String tramiteIdHex,
            String codigoTramite,
            String titulo,
            String cuerpo,
            String tipoNotificacion) {
        fcmNotificationService.enviarNotificacion(clienteIdHex, titulo, cuerpo, codigoTramite, tramiteIdHex);
        try {
            NotificacionCreateRequest req = new NotificacionCreateRequest();
            req.setUsuarioId(clienteIdHex);
            req.setTramiteId(tramiteIdHex);
            req.setMensaje(cuerpo);
            req.setTipo(tipoNotificacion);
            seguimientoService.crearNotificacion(req);
        } catch (RuntimeException ex) {
            log.warn("No se pudo persistir notificación in-app (tipo={}): {}", tipoNotificacion, ex.getMessage());
        }
    }
}
