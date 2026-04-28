package com.plataforma.tramites.modules.politicas.dto;

import java.time.Instant;
import java.util.List;

public record PoliticaNegocioResponse(
        String id,
        String nombre,
        String descripcion,
        int version,
        long lockVersion,
        String estado,
        Instant fechaCreacion,
        String bpmnXml,
        List<NodoPoliticaResponse> nodos,
        List<ConexionFlujoResponse> conexiones) {

    public record NodoPoliticaResponse(
            String idNodo,
            String nombre,
            String tipoNodo,
            int orden,
            String condicion,
            boolean esInicial,
            boolean esFinal,
            String areaId,
            List<AsignacionResponsableResponse> asignacionesResponsable,
            String formularioExternoUrl,
            String carrilBpmn) {}

    public record AsignacionResponsableResponse(
            String usuarioId,
            String areaId,
            Instant fechaAsignacion,
            boolean estado) {}

    public record ConexionFlujoResponse(
            String idConexion,
            String tipoFlujo,
            String condicion,
            String origenNodoId,
            String destinoNodoId) {}
}
