package com.plataforma.tramites.modules.seguridad.document;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Colección {@code roles}. El campo {@code codigo} enlaza con el portal de login y los permisos.
 */
@Document(collection = "roles")
public class RolDocument {

    @Id
    private ObjectId id;
    /** Ej.: ADMINISTRADOR, DISENADOR_POLITICAS, RESPONSABLE_AREA, CLIENTE */
    private String codigo;
    private String nombre;
    /** Códigos de permisos del catálogo (colección {@code permisos}) asignados al rol. */
    private List<String> permisoCodigos = new ArrayList<>();

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
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
        return permisoCodigos != null ? permisoCodigos : Collections.emptyList();
    }

    public void setPermisoCodigos(List<String> permisoCodigos) {
        this.permisoCodigos = permisoCodigos != null ? permisoCodigos : new ArrayList<>();
    }
}
