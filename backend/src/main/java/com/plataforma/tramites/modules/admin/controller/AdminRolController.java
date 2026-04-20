package com.plataforma.tramites.modules.admin.controller;

import com.plataforma.tramites.modules.admin.dto.RolAdminCreateRequest;
import com.plataforma.tramites.modules.admin.dto.RolAdminResponse;
import com.plataforma.tramites.modules.admin.dto.RolAdminUpdateRequest;
import com.plataforma.tramites.modules.admin.service.AdminRolService;
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
@RequestMapping("/api/admin/roles")
public class AdminRolController {

    private final AdminRolService adminRolService;

    public AdminRolController(AdminRolService adminRolService) {
        this.adminRolService = adminRolService;
    }

    @GetMapping
    public List<RolAdminResponse> listar() {
        return adminRolService.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RolAdminResponse crear(@Valid @RequestBody RolAdminCreateRequest body) {
        return adminRolService.crear(body);
    }

    @PatchMapping("/{id}")
    public RolAdminResponse actualizar(@PathVariable String id, @Valid @RequestBody RolAdminUpdateRequest body) {
        return adminRolService.actualizar(id, body);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String id) {
        adminRolService.eliminar(id);
    }
}
