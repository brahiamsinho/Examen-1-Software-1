package com.plataforma.tramites.shared.dto;

/**
 * Estado de dependencias externas (MongoDB, Redis) para diagnóstico operativo.
 */
public record InfraHealthResponse(boolean mongodbOk, boolean redisOk, String mongodbDetail, String redisDetail) {}
