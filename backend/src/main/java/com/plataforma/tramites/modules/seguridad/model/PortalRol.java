package com.plataforma.tramites.modules.seguridad.model;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Actores/portales habilitados para autenticación.
 */
public enum PortalRol {
    ADMINISTRADOR("ADMINISTRADOR", "Administrador"),
    DISENADOR_POLITICAS("DISENADOR_POLITICAS", "Diseñador de políticas de negocio"),
    RESPONSABLE_AREA("RESPONSABLE_AREA", "Responsable de área"),
    CLIENTE("CLIENTE", "Cliente");

    private final String codigo;
    private final String nombre;

    PortalRol(String codigo, String nombre) {
        this.codigo = codigo;
        this.nombre = nombre;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public static Optional<PortalRol> fromCodigo(String codigo) {
        if (codigo == null) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(value -> value.codigo.equals(codigo))
                .findFirst();
    }

    public static Set<String> codigos() {
        return Arrays.stream(values())
                .map(PortalRol::getCodigo)
                .collect(Collectors.toUnmodifiableSet());
    }
}
