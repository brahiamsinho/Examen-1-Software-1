package com.plataforma.tramites.modules.tramites.dto;

import jakarta.validation.constraints.NotBlank;

/** Avance por una conexión válida desde el nodo actual (no bifurcación PARALELO con dos o más salidas). */
public class TramiteFlujoAvanzarRequest {

    @NotBlank
    private String idConexion;

    private String observacion;

    public String getIdConexion() {
        return idConexion;
    }

    public void setIdConexion(String idConexion) {
        this.idConexion = idConexion;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}
