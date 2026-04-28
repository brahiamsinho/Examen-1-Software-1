package com.plataforma.tramites.modules.seguimiento.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "fcm_tokens")
public class FcmTokenDocument {

    @Id
    private String id;

    private String usuarioId;

    private String fcmToken;

    private Instant registradoEn;

    private Instant ultimoUso;

    public FcmTokenDocument() {
    }

    public FcmTokenDocument(String usuarioId, String fcmToken) {
        this.usuarioId = usuarioId;
        this.fcmToken = fcmToken;
        this.registradoEn = Instant.now();
        this.ultimoUso = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public Instant getRegistradoEn() {
        return registradoEn;
    }

    public void setRegistradoEn(Instant registradoEn) {
        this.registradoEn = registradoEn;
    }

    public Instant getUltimoUso() {
        return ultimoUso;
    }

    public void setUltimoUso(Instant ultimoUso) {
        this.ultimoUso = ultimoUso;
    }
}
