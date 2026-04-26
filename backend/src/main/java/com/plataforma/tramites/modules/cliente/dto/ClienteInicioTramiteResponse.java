package com.plataforma.tramites.modules.cliente.dto;

public class ClienteInicioTramiteResponse {

    private String tramiteId;
    private String codigoTramite;
    private String documentoId;
    private String politicaId;
    private String nodoIngresoId;
    private String nodoIngresoNombre;
    private String rutaArchivoGuardada;

    public String getTramiteId() {
        return tramiteId;
    }

    public void setTramiteId(String tramiteId) {
        this.tramiteId = tramiteId;
    }

    public String getCodigoTramite() {
        return codigoTramite;
    }

    public void setCodigoTramite(String codigoTramite) {
        this.codigoTramite = codigoTramite;
    }

    public String getDocumentoId() {
        return documentoId;
    }

    public void setDocumentoId(String documentoId) {
        this.documentoId = documentoId;
    }

    public String getPoliticaId() {
        return politicaId;
    }

    public void setPoliticaId(String politicaId) {
        this.politicaId = politicaId;
    }

    public String getNodoIngresoId() {
        return nodoIngresoId;
    }

    public void setNodoIngresoId(String nodoIngresoId) {
        this.nodoIngresoId = nodoIngresoId;
    }

    public String getNodoIngresoNombre() {
        return nodoIngresoNombre;
    }

    public void setNodoIngresoNombre(String nodoIngresoNombre) {
        this.nodoIngresoNombre = nodoIngresoNombre;
    }

    public String getRutaArchivoGuardada() {
        return rutaArchivoGuardada;
    }

    public void setRutaArchivoGuardada(String rutaArchivoGuardada) {
        this.rutaArchivoGuardada = rutaArchivoGuardada;
    }
}
