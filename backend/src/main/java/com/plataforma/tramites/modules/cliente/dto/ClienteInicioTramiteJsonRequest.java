package com.plataforma.tramites.modules.cliente.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Alta de trámite + documento sin archivo binario (p. ej. la ruta ya fue cargada en otro bucket).
 */
public class ClienteInicioTramiteJsonRequest {

    @NotBlank
    @Size(max = 500)
    private String nombreArchivo;

    @NotBlank
    @Size(max = 120)
    private String tipoArchivo;

    @NotBlank
    @Size(max = 2000)
    private String rutaArchivo;

    @Size(max = 500)
    private String asunto;

    @Size(max = 8000)
    private String descripcion;

    @Size(max = 32)
    private String prioridad;

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

    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }
}
