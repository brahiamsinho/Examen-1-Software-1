package com.plataforma.tramites.modules.documentos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class FormularioTramiteCreateRequest {

    @NotBlank
    private String tramiteId;

    private String nodoId;

    @NotBlank
    @Size(max = 500)
    private String titulo;

    @NotBlank
    @Size(max = 120)
    private String tipo;

    @NotBlank
    @Size(max = 32000)
    private String contenido;

    public String getTramiteId() {
        return tramiteId;
    }

    public void setTramiteId(String tramiteId) {
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
}
