package com.plataforma.tramites.modules.admin.dto;

import java.util.List;

public class RolAdminResponse {

    private String id;
    private String codigo;
    private String nombre;
    private List<String> permisoCodigos;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<String> getPermisoCodigos() {
        return permisoCodigos;
    }

    public void setPermisoCodigos(List<String> permisoCodigos) {
        this.permisoCodigos = permisoCodigos;
    }
}
