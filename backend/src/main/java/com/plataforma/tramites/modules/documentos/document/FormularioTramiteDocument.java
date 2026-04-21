package com.plataforma.tramites.modules.documentos.document;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Colección {@code formularios_tramite} (script.db).
 */
@Document(collection = "formularios_tramite")
public class FormularioTramiteDocument {

    @Id
    private ObjectId id;
    private ObjectId tramiteId;
    private String nodoId;
    private String titulo;
    private String tipo;
    private String contenido;
    private Instant fechaRegistro;

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

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public Instant getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Instant fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
