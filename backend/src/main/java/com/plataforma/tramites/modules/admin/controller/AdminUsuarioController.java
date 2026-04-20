package com.plataforma.tramites.modules.admin.controller;

import com.plataforma.tramites.modules.admin.dto.PagedResponse;
import com.plataforma.tramites.modules.admin.dto.UsuarioAdminCreateRequest;
import com.plataforma.tramites.modules.admin.dto.UsuarioAdminResponse;
import com.plataforma.tramites.modules.admin.dto.UsuarioAdminUpdateRequest;
import com.plataforma.tramites.modules.admin.service.AdminUsuarioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/admin/usuarios")
public class AdminUsuarioController {

    private final AdminUsuarioService adminUsuarioService;

    public AdminUsuarioController(AdminUsuarioService adminUsuarioService) {
        this.adminUsuarioService = adminUsuarioService;
    }

    @GetMapping
    public PagedResponse<UsuarioAdminResponse> listar(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return adminUsuarioService.listar(PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size))));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioAdminResponse crear(@Valid @RequestBody UsuarioAdminCreateRequest body) {
        return adminUsuarioService.crear(body);
    }

    @PatchMapping("/{id}")
    public UsuarioAdminResponse actualizar(@PathVariable String id, @Valid @RequestBody UsuarioAdminUpdateRequest body) {
        return adminUsuarioService.actualizar(id, body);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String id) {
        adminUsuarioService.eliminar(id);
    }
}
