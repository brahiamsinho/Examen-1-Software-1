package com.plataforma.tramites.modules.seguimiento.dto;

import jakarta.validation.constraints.NotBlank;

public record FcmTokenRequest(
        @NotBlank String fcmToken
) {
}
