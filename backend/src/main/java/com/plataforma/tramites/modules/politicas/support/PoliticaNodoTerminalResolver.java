package com.plataforma.tramites.modules.politicas.support;

import com.plataforma.tramites.modules.politicas.document.PoliticaNegocioDocument;
import com.plataforma.tramites.modules.politicas.model.NodoPoliticaEmbeddable;

/**
 * Determina si un nodo de política cierra el trámite de forma exitosa o por rechazo.
 */
public final class PoliticaNodoTerminalResolver {

    public enum ResultadoTerminal {
        NINGUNO,
        APROBADO,
        RECHAZADO
    }

    private PoliticaNodoTerminalResolver() {}

    public static ResultadoTerminal clasificar(PoliticaNegocioDocument politica, String idNodo) {
        if (politica == null || politica.getNodos() == null || idNodo == null || idNodo.isBlank()) {
            return ResultadoTerminal.NINGUNO;
        }
        String id = idNodo.trim();
        return politica.getNodos().stream()
                .filter(n -> id.equals(n.getIdNodo()))
                .findFirst()
                .map(PoliticaNodoTerminalResolver::clasificarNodo)
                .orElse(ResultadoTerminal.NINGUNO);
    }

    private static ResultadoTerminal clasificarNodo(NodoPoliticaEmbeddable n) {
        String tipo = n.getTipoNodo() == null ? "" : n.getTipoNodo().trim().toUpperCase();
        if ("RECHAZO".equals(tipo)) {
            return ResultadoTerminal.RECHAZADO;
        }
        if ("FIN".equals(tipo)) {
            return ResultadoTerminal.APROBADO;
        }
        return ResultadoTerminal.NINGUNO;
    }
}
