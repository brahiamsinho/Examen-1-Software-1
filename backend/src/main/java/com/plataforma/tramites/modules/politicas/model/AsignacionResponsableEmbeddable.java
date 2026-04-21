package com.plataforma.tramites.modules.politicas.model;

import org.bson.types.ObjectId;

import java.time.Instant;

/**
 * Subdocumento {@code asignacionesResponsable} dentro de un nodo (script.db).
 */
public class AsignacionResponsableEmbeddable {

    private ObjectId usuarioId;
    private ObjectId areaId;
    private Instant fechaAsignacion;
    private boolean estado;

    public ObjectId getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(ObjectId usuarioId) {
        this.usuarioId = usuarioId;
    }

    public ObjectId getAreaId() {
        return areaId;
    }

    public void setAreaId(ObjectId areaId) {
        this.areaId = areaId;
    }

    public Instant getFechaAsignacion() {
        return fechaAsignacion;
    }

    public void setFechaAsignacion(Instant fechaAsignacion) {
        this.fechaAsignacion = fechaAsignacion;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }
}
