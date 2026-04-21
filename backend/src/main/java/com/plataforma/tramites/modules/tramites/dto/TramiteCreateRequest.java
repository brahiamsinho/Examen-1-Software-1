package com.plataforma.tramites.modules.tramites.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TramiteCreateRequest {

    /** Si viene vacío se genera un código único. */
    @Size(max = 64)
    private String codigo;

    @NotBlank
    @Size(max = 500)
    private String asunto;

    @NotBlank
    @Size(max = 8000)
    private String descripcion;

    @NotBlank
    private String prioridad;

    @NotBlank
    private String politicaId;

    @NotBlank
    private String clienteId;

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

    public String getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }

    public String getPoliticaId() {
        return politicaId;
    }

    public void setPoliticaId(String politicaId) {
        this.politicaId = politicaId;
    }

    public String getClienteId() {
        return clienteId;
    }

    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }
}
