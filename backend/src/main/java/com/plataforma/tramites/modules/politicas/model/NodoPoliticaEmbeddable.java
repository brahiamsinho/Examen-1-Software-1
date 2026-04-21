package com.plataforma.tramites.modules.politicas.model;

import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

/**
 * Nodo de política embebido en {@code politicas_negocio.nodos} (script.db).
 */
public class NodoPoliticaEmbeddable {

    private String idNodo;
    private String nombre;
    private String tipoNodo;
    private int orden;
    private String condicion;
    private boolean esInicial;
    private boolean esFinal;
    private ObjectId areaId;
    private List<AsignacionResponsableEmbeddable> asignacionesResponsable = new ArrayList<>();

    public String getIdNodo() {
        return idNodo;
    }

    public void setIdNodo(String idNodo) {
        this.idNodo = idNodo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipoNodo() {
        return tipoNodo;
    }

    public void setTipoNodo(String tipoNodo) {
        this.tipoNodo = tipoNodo;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    public String getCondicion() {
        return condicion;
    }

    public void setCondicion(String condicion) {
        this.condicion = condicion;
    }

    public boolean isEsInicial() {
        return esInicial;
    }

    public void setEsInicial(boolean esInicial) {
        this.esInicial = esInicial;
    }

    public boolean isEsFinal() {
        return esFinal;
    }

    public void setEsFinal(boolean esFinal) {
        this.esFinal = esFinal;
    }

    public ObjectId getAreaId() {
        return areaId;
    }

    public void setAreaId(ObjectId areaId) {
        this.areaId = areaId;
    }

    public List<AsignacionResponsableEmbeddable> getAsignacionesResponsable() {
        return asignacionesResponsable;
    }

    public void setAsignacionesResponsable(List<AsignacionResponsableEmbeddable> asignacionesResponsable) {
        this.asignacionesResponsable =
                asignacionesResponsable != null ? asignacionesResponsable : new ArrayList<>();
    }
}
