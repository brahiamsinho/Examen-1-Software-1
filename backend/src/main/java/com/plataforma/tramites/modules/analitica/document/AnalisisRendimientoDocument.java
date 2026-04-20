package com.plataforma.tramites.modules.analitica.document;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

/**
 * Colección {@code analisis_rendimiento} (script.db).
 */
@Document(collection = "analisis_rendimiento")
public class AnalisisRendimientoDocument {

    @Id
    private ObjectId id;
    private ObjectId politicaId;
    private List<ObjectId> tramiteIds;
    private Instant fechaAnalisis;
    private Double tiempoPromedio;
    private String cuelloBotellaDetectado;
    private String observacion;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public ObjectId getPoliticaId() {
        return politicaId;
    }

    public void setPoliticaId(ObjectId politicaId) {
        this.politicaId = politicaId;
    }

    public List<ObjectId> getTramiteIds() {
        return tramiteIds;
    }

    public void setTramiteIds(List<ObjectId> tramiteIds) {
        this.tramiteIds = tramiteIds;
    }

    public Instant getFechaAnalisis() {
        return fechaAnalisis;
    }

    public void setFechaAnalisis(Instant fechaAnalisis) {
        this.fechaAnalisis = fechaAnalisis;
    }

    public Double getTiempoPromedio() {
        return tiempoPromedio;
    }

    public void setTiempoPromedio(Double tiempoPromedio) {
        this.tiempoPromedio = tiempoPromedio;
    }

    public String getCuelloBotellaDetectado() {
        return cuelloBotellaDetectado;
    }

    public void setCuelloBotellaDetectado(String cuelloBotellaDetectado) {
        this.cuelloBotellaDetectado = cuelloBotellaDetectado;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}
