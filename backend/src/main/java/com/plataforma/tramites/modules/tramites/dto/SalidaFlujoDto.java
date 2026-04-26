package com.plataforma.tramites.modules.tramites.dto;

/** Salida posible desde el nodo actual según el grafo de la política. */
public record SalidaFlujoDto(String idConexion, String destinoNodoId, String tipoFlujo, String condicion) {}
