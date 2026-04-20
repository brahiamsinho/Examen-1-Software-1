package com.plataforma.tramites.modules.admin.document;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Registro de auditoría para acciones de administración.
 */
@Document(collection = "bitacora_auditoria")
public class BitacoraDocument {

    @Id
    private ObjectId id;

    @Indexed
    private Instant fecha;

    private String actorUsuarioId;
    private String actorCorreo;
    private String accion;
    private String entidad;
    private String detalle;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public Instant getFecha() {
        return fecha;
    }

    public void setFecha(Instant fecha) {
        this.fecha = fecha;
    }

    public String getActorUsuarioId() {
        return actorUsuarioId;
    }

    public void setActorUsuarioId(String actorUsuarioId) {
        this.actorUsuarioId = actorUsuarioId;
    }

    public String getActorCorreo() {
        return actorCorreo;
    }

    public void setActorCorreo(String actorCorreo) {
        this.actorCorreo = actorCorreo;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getEntidad() {
        return entidad;
    }

    public void setEntidad(String entidad) {
        this.entidad = entidad;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }
}
