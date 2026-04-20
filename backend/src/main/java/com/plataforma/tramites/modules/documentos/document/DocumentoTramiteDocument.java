package com.plataforma.tramites.modules.documentos.document;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Colección {@code documentos_tramite} (script.db).
 */
@Document(collection = "documentos_tramite")
public class DocumentoTramiteDocument {

    @Id
    private ObjectId id;
    private ObjectId tramiteId;
    private String nodoId;
    private String nombreArchivo;
    private String tipoArchivo;
    private String rutaArchivo;
    private Instant fechaCarga;
    private String estado;

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

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public String getTipoArchivo() {
        return tipoArchivo;
    }

    public void setTipoArchivo(String tipoArchivo) {
        this.tipoArchivo = tipoArchivo;
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public void setRutaArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    public Instant getFechaCarga() {
        return fechaCarga;
    }

    public void setFechaCarga(Instant fechaCarga) {
        this.fechaCarga = fechaCarga;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
