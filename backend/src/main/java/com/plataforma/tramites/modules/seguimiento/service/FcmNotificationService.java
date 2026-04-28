package com.plataforma.tramites.modules.seguimiento.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.plataforma.tramites.modules.seguimiento.document.FcmTokenDocument;
import com.plataforma.tramites.modules.seguimiento.repository.FcmTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class FcmNotificationService {

    private static final Logger log = LoggerFactory.getLogger(FcmNotificationService.class);

    private final FcmTokenRepository fcmTokenRepository;

    public FcmNotificationService(FcmTokenRepository fcmTokenRepository) {
        this.fcmTokenRepository = fcmTokenRepository;
    }

    public void registrarToken(String usuarioId, String fcmToken) {
        fcmTokenRepository.deleteByFcmToken(fcmToken);

        FcmTokenDocument doc = new FcmTokenDocument(usuarioId, fcmToken);
        fcmTokenRepository.save(doc);
        log.info("FCM token registrado para usuario {}", usuarioId);
    }

    /**
     * @param tramiteIdHex ObjectId hex del trámite (opcional); se envía en {@code data} para deep links en el cliente.
     */
    public void enviarNotificacion(
            String usuarioId, String titulo, String cuerpo, String tramiteCodigo, String tramiteIdHex) {
        List<FcmTokenDocument> tokens = fcmTokenRepository.findByUsuarioId(usuarioId);
        if (tokens.isEmpty()) {
            log.debug("Sin FCM tokens para usuario {}", usuarioId);
            return;
        }

        String tid = tramiteIdHex != null && !tramiteIdHex.isBlank() ? tramiteIdHex.trim() : "";

        for (FcmTokenDocument tokenDoc : tokens) {
            try {
                Message message = Message.builder()
                        .setToken(tokenDoc.getFcmToken())
                        .setNotification(Notification.builder()
                                .setTitle(titulo)
                                .setBody(cuerpo)
                                .build())
                        .putData("tramiteCodigo", tramiteCodigo != null ? tramiteCodigo : "")
                        .putData("tramiteId", tid)
                        .putData("type", "tramite_update")
                        .build();

                FirebaseMessaging.getInstance().send(message);
                tokenDoc.setUltimoUso(Instant.now());
                fcmTokenRepository.save(tokenDoc);
                log.info("Notificacion FCM enviada a usuario {}: {}", usuarioId, titulo);
            } catch (FirebaseMessagingException e) {
                log.warn("Error enviando FCM a usuario {}: {}", usuarioId, e.getMessage());
                if (isInvalidTokenError(e)) {
                    fcmTokenRepository.delete(tokenDoc);
                    log.info("FCM token eliminado para usuario {} (token invalido)", usuarioId);
                }
            }
        }
    }

    private boolean isInvalidTokenError(FirebaseMessagingException e) {
        String code = e.getMessagingErrorCode().name();
        return "UNREGISTERED".equals(code) || "INVALID_ARGUMENT".equals(code);
    }
}
