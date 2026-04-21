package com.plataforma.tramites.modules.seguimiento.document;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Colección {@code notificaciones} (script.db).
 */
@Document(collection = "notificaciones")
public class NotificacionDocument {

    @Id
    private ObjectId id;
    private ObjectId tramiteId;
    private ObjectId usuarioId;
    private String mensaje;
    private String tipo;
    private Instant fechaEnvio;
    private boolean leida;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public ObjectId getTramiteId() {
        return tramiteId;
    }

    public void setTramiteId(ObjectId tramiteId) {
        this.tramiteId = tramiteId;
    }

    public ObjectId getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(ObjectId usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Instant getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(Instant fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public boolean isLeida() {
        return leida;
    }

    public void setLeida(boolean leida) {
        this.leida = leida;
    }
}
