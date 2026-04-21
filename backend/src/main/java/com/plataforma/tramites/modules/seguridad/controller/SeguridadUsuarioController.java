package com.plataforma.tramites.modules.seguridad.controller;

import com.plataforma.tramites.modules.seguridad.dto.UsuarioAreaResponse;
import com.plataforma.tramites.modules.seguridad.service.SeguridadUsuarioConsultaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/seguridad/usuarios")
public class SeguridadUsuarioController {

    private final SeguridadUsuarioConsultaService seguridadUsuarioConsultaService;

    public SeguridadUsuarioController(SeguridadUsuarioConsultaService seguridadUsuarioConsultaService) {
        this.seguridadUsuarioConsultaService = seguridadUsuarioConsultaService;
    }

    /** Usuarios activos del área (colección {@code usuarios}, campo {@code areaId} en script.db). */
    @GetMapping
    public List<UsuarioAreaResponse> listarPorArea(@RequestParam String areaId) {
        return seguridadUsuarioConsultaService.listarActivosPorArea(areaId);
    }
}
