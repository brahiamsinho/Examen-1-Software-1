package com.plataforma.tramites.modules.tramites.document;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Colección {@code recorridos_tramite} (script.db).
 */
@Document(collection = "recorridos_tramite")
public class RecorridoTramiteDocument {

    @Id
    private ObjectId id;
    private ObjectId tramiteId;
    private String nodoId;
    private ObjectId areaId;
    private ObjectId usuarioId;
    private Instant fechaEntrada;
    private Instant fechaSalida;
    private String estado;
    private String observacion;

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

    public String getNodoId() {
        return nodoId;
    }

    public void setNodoId(String nodoId) {
        this.nodoId = nodoId;
    }

    public ObjectId getAreaId() {
        return areaId;
    }

    public void setAreaId(ObjectId areaId) {
        this.areaId = areaId;
    }

    public ObjectId getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(ObjectId usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Instant getFechaEntrada() {
        return fechaEntrada;
    }

    public void setFechaEntrada(Instant fechaEntrada) {
        this.fechaEntrada = fechaEntrada;
    }

    public Instant getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(Instant fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}
