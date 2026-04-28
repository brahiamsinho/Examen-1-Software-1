package com.plataforma.tramites.modules.politicas.support;

import com.plataforma.tramites.modules.politicas.document.PoliticaNegocioDocument;
import com.plataforma.tramites.modules.politicas.model.NodoPoliticaEmbeddable;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PoliticaNodoTerminalResolverTest {

    @Test
    void finEsAprobado() {
        PoliticaNegocioDocument p = new PoliticaNegocioDocument();
        p.setId(new ObjectId());
        NodoPoliticaEmbeddable n = new NodoPoliticaEmbeddable();
        n.setIdNodo("n_fin");
        n.setTipoNodo("FIN");
        n.setEsFinal(true);
        p.setNodos(List.of(n));
        assertEquals(
                PoliticaNodoTerminalResolver.ResultadoTerminal.APROBADO,
                PoliticaNodoTerminalResolver.clasificar(p, "n_fin"));
    }

    @Test
    void rechazoEsRechazado() {
        PoliticaNegocioDocument p = new PoliticaNegocioDocument();
        p.setId(new ObjectId());
        NodoPoliticaEmbeddable n = new NodoPoliticaEmbeddable();
        n.setIdNodo("n_r");
        n.setTipoNodo("RECHAZO");
        n.setEsFinal(true);
        p.setNodos(List.of(n));
        assertEquals(
                PoliticaNodoTerminalResolver.ResultadoTerminal.RECHAZADO,
                PoliticaNodoTerminalResolver.clasificar(p, "n_r"));
    }

    @Test
    void actividadNoEsTerminal() {
        PoliticaNegocioDocument p = new PoliticaNegocioDocument();
        p.setId(new ObjectId());
        NodoPoliticaEmbeddable n = new NodoPoliticaEmbeddable();
        n.setIdNodo("n1");
        n.setTipoNodo("ACTIVIDAD");
        n.setEsFinal(false);
        p.setNodos(List.of(n));
        assertEquals(
                PoliticaNodoTerminalResolver.ResultadoTerminal.NINGUNO,
                PoliticaNodoTerminalResolver.clasificar(p, "n1"));
    }
}
