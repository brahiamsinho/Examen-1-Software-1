package com.plataforma.tramites.modules.politicas.model;

/**
 * Conexión entre nodos en {@code politicas_negocio.conexiones} (script.db).
 */
public class ConexionFlujoEmbeddable {

    private String idConexion;
    private String tipoFlujo;
    private String condicion;
    private String origenNodoId;
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
