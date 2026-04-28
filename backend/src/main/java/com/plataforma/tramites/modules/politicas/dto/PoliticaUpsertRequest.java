package com.plataforma.tramites.modules.politicas.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class PoliticaUpsertRequest {

    @NotBlank
    @Size(max = 200)
    private String nombre;

    @NotBlank
    @Size(max = 4000)
    private String descripcion;

    @Min(1)
    private int version = 1;

    @NotBlank
    private String estado;

    /** XML BPMN opcional (canon visual del modelador). */
    @Size(max = 2_000_000)
    private String bpmnXml;

    @NotNull
    @Valid
    private List<NodoPoliticaRequest> nodos;

    @NotNull
    @Valid
    private List<ConexionFlujoRequest> conexiones;

    /**
     * Concurrencia optimista (obligatorio en PUT; ignorado en POST de alta).
     */
    private Long lockVersion;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getBpmnXml() {
        return bpmnXml;
    }

    public void setBpmnXml(String bpmnXml) {
        this.bpmnXml = bpmnXml;
    }

    public List<NodoPoliticaRequest> getNodos() {
        return nodos;
    }

    public void setNodos(List<NodoPoliticaRequest> nodos) {
        this.nodos = nodos;
    }

    public List<ConexionFlujoRequest> getConexiones() {
        return conexiones;
    }

    public void setConexiones(List<ConexionFlujoRequest> conexiones) {
        this.conexiones = conexiones;
    }

    public Long getLockVersion() {
        return lockVersion;
    }

    public void setLockVersion(Long lockVersion) {
        this.lockVersion = lockVersion;
    }

    public static class NodoPoliticaRequest {

        @NotBlank
        private String idNodo;

        @NotBlank
        private String nombre;

        @NotBlank
        private String tipoNodo;

        private int orden;

        private String condicion;
        private boolean esInicial;
        private boolean esFinal;
        private String areaId;

        /** URL HTTPS opcional (p. ej. Google Forms). */
        @Size(max = 2048)
        private String formularioExternoUrl;

        /** Carril / swimlane BPMN (texto libre corto). */
        @Size(max = 160)
        private String carrilBpmn;

        @Valid
        private List<AsignacionResponsableRequest> asignacionesResponsable;

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

        public String getAreaId() {
            return areaId;
        }

        public void setAreaId(String areaId) {
            this.areaId = areaId;
        }

        public String getFormularioExternoUrl() {
            return formularioExternoUrl;
        }

        public void setFormularioExternoUrl(String formularioExternoUrl) {
            this.formularioExternoUrl = formularioExternoUrl;
        }

        public String getCarrilBpmn() {
            return carrilBpmn;
        }

        public void setCarrilBpmn(String carrilBpmn) {
            this.carrilBpmn = carrilBpmn;
        }

        public List<AsignacionResponsableRequest> getAsignacionesResponsable() {
            return asignacionesResponsable;
        }

        public void setAsignacionesResponsable(List<AsignacionResponsableRequest> asignacionesResponsable) {
            this.asignacionesResponsable = asignacionesResponsable;
        }
    }

    public static class AsignacionResponsableRequest {

        @NotBlank
        private String usuarioId;

        @NotBlank
        private String areaId;

        /** Si es null se usa la fecha/hora actual al persistir. */
        private java.time.Instant fechaAsignacion;

        private boolean estado = true;

        public String getUsuarioId() {
            return usuarioId;
        }

        public void setUsuarioId(String usuarioId) {
            this.usuarioId = usuarioId;
        }

        public String getAreaId() {
            return areaId;
        }

        public void setAreaId(String areaId) {
            this.areaId = areaId;
        }

        public java.time.Instant getFechaAsignacion() {
            return fechaAsignacion;
        }

        public void setFechaAsignacion(java.time.Instant fechaAsignacion) {
            this.fechaAsignacion = fechaAsignacion;
        }

        public boolean isEstado() {
            return estado;
        }

        public void setEstado(boolean estado) {
            this.estado = estado;
        }
    }

    public static class ConexionFlujoRequest {

        @NotBlank
        private String idConexion;

        @NotBlank
        private String tipoFlujo;

        private String condicion;

        @NotBlank
        private String origenNodoId;

        @NotBlank
        private String destinoNodoId;

        public String getIdConexion() {
            return idConexion;
        }

        public void setIdConexion(String idConexion) {
            this.idConexion = idConexion;
        }

        public String getTipoFlujo() {
            return tipoFlujo;
        }

        public void setTipoFlujo(String tipoFlujo) {
            this.tipoFlujo = tipoFlujo;
        }

        public String getCondicion() {
            return condicion;
        }

        public void setCondicion(String condicion) {
            this.condicion = condicion;
        }

        public String getOrigenNodoId() {
            return origenNodoId;
        }

        public void setOrigenNodoId(String origenNodoId) {
            this.origenNodoId = origenNodoId;
        }

        public String getDestinoNodoId() {
            return destinoNodoId;
        }

        public void setDestinoNodoId(String destinoNodoId) {
            this.destinoNodoId = destinoNodoId;
        }
    }
}
