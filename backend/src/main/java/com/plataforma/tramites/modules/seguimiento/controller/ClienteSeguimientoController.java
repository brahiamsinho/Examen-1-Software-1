package com.plataforma.tramites.modules.seguimiento.controller;

import com.plataforma.tramites.modules.seguimiento.dto.NotificacionResponse;
import com.plataforma.tramites.modules.seguimiento.service.SeguimientoService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Seguimiento para el portal cliente: notificaciones del usuario autenticado (JWT {@code sub} = usuarioId).
 */
@RestController
@RequestMapping("/api/cliente/seguimiento")
public class ClienteSeguimientoController {

    private final SeguimientoService seguimientoService;

    public ClienteSeguimientoController(SeguimientoService seguimientoService) {
        this.seguimientoService = seguimientoService;
    }

    @GetMapping("/notificaciones")
    public Page<NotificacionResponse> misNotificaciones(
            Authentication auth, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        String usuarioId = auth.getName();
        return seguimientoService.listarNotificacionesUsuario(usuarioId, page, size);
    }

    @PatchMapping("/notificaciones/{id}/leida")
    public NotificacionResponse marcarLeida(Authentication auth, @PathVariable String id) {
        return seguimientoService.marcarNotificacionLeidaParaUsuario(id, auth.getName());
    }
}
