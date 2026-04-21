package com.plataforma.tramites.modules.seguimiento.document;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Colección {@code bitacora} del dominio de trámites (script.db). Distinta de {@code bitacora_auditoria} del módulo admin.
 */
@Document(collection = "bitacora")
public class BitacoraEventoDocument {

    @Id
    private ObjectId id;
    private ObjectId usuarioId;
    private String accion;
    private String descripcion;
    private Instant fechaHora;
    private String entidadTipo;
    private String entidadId;
    private ObjectId tramiteId;
    private ObjectId politicaId;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public ObjectId getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(ObjectId usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Instant getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(Instant fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getEntidadTipo() {
        return entidadTipo;
    }

    public void setEntidadTipo(String entidadTipo) {
        this.entidadTipo = entidadTipo;
    }

    public String getEntidadId() {
        return entidadId;
    }

    public void setEntidadId(String entidadId) {
        this.entidadId = entidadId;
    }

    public ObjectId getTramiteId() {
        return tramiteId;
    }

    public void setTramiteId(ObjectId tramiteId) {
        this.tramiteId = tramiteId;
    }

    public ObjectId getPoliticaId() {
        return politicaId;
    }

    public void setPoliticaId(ObjectId politicaId) {
        this.politicaId = politicaId;
    }
}
