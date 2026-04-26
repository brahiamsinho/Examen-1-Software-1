package com.plataforma.tramites.modules.politicas.support;

import com.plataforma.tramites.modules.politicas.document.PoliticaNegocioDocument;
import com.plataforma.tramites.modules.politicas.model.NodoPoliticaEmbeddable;
import com.plataforma.tramites.shared.exception.ApiException;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PoliticaNodoInicialResolver {

    /**
     * Devuelve el nodo marcado {@code esInicial=true} (único). El nombre mostrado puede ser cualquier etiqueta de negocio
     * (p. ej. atención al cliente); no se asume un nombre fijo.
     */
    public NodoInicioPolitica resolver(PoliticaNegocioDocument politica) {
        List<NodoPoliticaEmbeddable> iniciales =
                politica.getNodos().stream().filter(NodoPoliticaEmbeddable::isEsInicial).toList();
        if (iniciales.isEmpty()) {
            throw new ApiException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "La política no tiene ningún nodo inicial (esInicial=true).");
        }
        if (iniciales.size() > 1) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "La política tiene más de un nodo inicial; debe quedar exactamente uno con esInicial=true.");
        }
        NodoPoliticaEmbeddable n = iniciales.get(0);
        if (n.getIdNodo() == null || n.getIdNodo().isBlank()) {
            throw new ApiException(
                    HttpStatus.UNPROCESSABLE_ENTITY, "El nodo inicial de la política no tiene idNodo definido.");
        }
        return new NodoInicioPolitica(n.getIdNodo(), n.getNombre(), n.getAreaId());
    }

    public record NodoInicioPolitica(String idNodo, String nombre, ObjectId areaId) {}
}
