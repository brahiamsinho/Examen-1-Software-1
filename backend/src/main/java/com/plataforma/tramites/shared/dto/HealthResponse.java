package com.plataforma.tramites.shared.dto;

import java.time.Instant;

public record HealthResponse(String status, String service, Instant timestamp) {
}
