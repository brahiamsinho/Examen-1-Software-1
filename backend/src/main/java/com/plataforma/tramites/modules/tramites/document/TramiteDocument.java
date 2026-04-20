package com.plataforma.tramites.modules.tramites.document;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Colección {@code tramites} (script.db).
 */
@Document(collection = "tramites")
public class TramiteDocument {

    @Id
    private ObjectId id;
    private String codigo;
    private String asunto;
    private String descripcion;
    private Instant fechaRegistro;
    private String prioridad;
    private String estado;
    private int numeroTurno;
    private ObjectId politicaId;
    private ObjectId clienteId;
    private String nodoActualId;
    private ObjectId areaActualId;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Instant getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Instant fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getNumeroTurno() {
        return numeroTurno;
    }

    public void setNumeroTurno(int numeroTurno) {
        this.numeroTurno = numeroTurno;
    }

    public ObjectId getPoliticaId() {
        return politicaId;
    }

    public void setPoliticaId(ObjectId politicaId) {
        this.politicaId = politicaId;
    }

    public ObjectId getClienteId() {
        return clienteId;
    }

    public void setClienteId(ObjectId clienteId) {
        this.clienteId = clienteId;
    }

    public String getNodoActualId() {
        return nodoActualId;
    }

    public void setNodoActualId(String nodoActualId) {
        this.nodoActualId = nodoActualId;
    }

    public ObjectId getAreaActualId() {
        return areaActualId;
    }

    public void setAreaActualId(ObjectId areaActualId) {
        this.areaActualId = areaActualId;
    }
}
