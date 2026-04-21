package com.plataforma.tramites.modules.analitica.document;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Colección {@code recomendaciones_politica} (script.db). Sin motor de IA: solo persistencia estructural.
 */
@Document(collection = "recomendaciones_politica")
public class RecomendacionPoliticaDocument {

    @Id
    private ObjectId id;
    private ObjectId politicaId;
    private ObjectId usuarioId;
    private Instant fechaGeneracion;
    private String politicaSugerida;
    private double probabilidadExito;
    private double tiempoEstimado;
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

    public ObjectId getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(ObjectId usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Instant getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(Instant fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public String getPoliticaSugerida() {
        return politicaSugerida;
    }

    public void setPoliticaSugerida(String politicaSugerida) {
        this.politicaSugerida = politicaSugerida;
    }

    public double getProbabilidadExito() {
        return probabilidadExito;
    }

    public void setProbabilidadExito(double probabilidadExito) {
        this.probabilidadExito = probabilidadExito;
    }

    public double getTiempoEstimado() {
        return tiempoEstimado;
    }

    public void setTiempoEstimado(double tiempoEstimado) {
        this.tiempoEstimado = tiempoEstimado;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}
