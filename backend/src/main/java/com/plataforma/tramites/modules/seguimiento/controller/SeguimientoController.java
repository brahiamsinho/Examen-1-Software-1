package com.plataforma.tramites.modules.seguimiento.controller;

import com.plataforma.tramites.modules.seguimiento.dto.BitacoraEventoCreateRequest;
import com.plataforma.tramites.modules.seguimiento.dto.BitacoraEventoResponse;
import com.plataforma.tramites.modules.seguimiento.dto.NotificacionCreateRequest;
import com.plataforma.tramites.modules.seguimiento.dto.NotificacionResponse;
import com.plataforma.tramites.modules.seguimiento.service.SeguimientoService;
import com.plataforma.tramites.shared.dto.ModuleStatusResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seguimiento")
public class SeguimientoController {

    private final SeguimientoService seguimientoService;

    public SeguimientoController(SeguimientoService seguimientoService) {
        this.seguimientoService = seguimientoService;
    }

    @GetMapping("/status")
    public ModuleStatusResponse status() {
        return seguimientoService.moduleStatus();
    }

    @GetMapping("/notificaciones")
    public Page<NotificacionResponse> notificacionesPorUsuario(
            @RequestParam String usuarioId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return seguimientoService.listarNotificacionesUsuario(usuarioId, page, size);
    }

    @PostMapping("/notificaciones")
    @ResponseStatus(HttpStatus.CREATED)
    public NotificacionResponse crearNotificacion(@Valid @RequestBody NotificacionCreateRequest body) {
        return seguimientoService.crearNotificacion(body);
    }

    @PatchMapping("/notificaciones/{id}/leida")
    public NotificacionResponse marcarLeida(@PathVariable String id) {
        return seguimientoService.marcarNotificacionLeida(id);
    }

    @GetMapping("/bitacora/tramites/{tramiteId}")
    public Page<BitacoraEventoResponse> bitacoraPorTramite(
            @PathVariable String tramiteId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return seguimientoService.listarBitacoraPorTramite(tramiteId, page, size);
    }

    @PostMapping("/bitacora")
    @ResponseStatus(HttpStatus.CREATED)
    public BitacoraEventoResponse registrarBitacora(@Valid @RequestBody BitacoraEventoCreateRequest body) {
        return seguimientoService.registrarBitacora(body);
    }
}
