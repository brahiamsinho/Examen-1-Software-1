package com.plataforma.tramites.modules.admin.controller;

import com.plataforma.tramites.modules.admin.dto.PermisoCreateRequest;
import com.plataforma.tramites.modules.admin.dto.PermisoResponse;
import com.plataforma.tramites.modules.admin.dto.PermisoUpdateRequest;
import com.plataforma.tramites.modules.admin.service.AdminPermisoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/permisos")
public class AdminPermisoController {

    private final AdminPermisoService adminPermisoService;

    public AdminPermisoController(AdminPermisoService adminPermisoService) {
        this.adminPermisoService = adminPermisoService;
    }

    @GetMapping
    public List<PermisoResponse> listar() {
        return adminPermisoService.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PermisoResponse crear(@Valid @RequestBody PermisoCreateRequest body) {
        return adminPermisoService.crear(body);
    }

    @PatchMapping("/{id}")
    public PermisoResponse actualizar(@PathVariable String id, @Valid @RequestBody PermisoUpdateRequest body) {
        return adminPermisoService.actualizar(id, body);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String id) {
        adminPermisoService.eliminar(id);
    }
}
