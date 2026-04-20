package com.plataforma.tramites.modules.admin.controller;

import com.plataforma.tramites.modules.admin.dto.BitacoraResponse;
import com.plataforma.tramites.modules.admin.dto.PagedResponse;
import com.plataforma.tramites.modules.admin.service.AdminBitacoraService;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/bitacora")
public class AdminBitacoraController {

    private final AdminBitacoraService adminBitacoraService;

    public AdminBitacoraController(AdminBitacoraService adminBitacoraService) {
        this.adminBitacoraService = adminBitacoraService;
    }

    @GetMapping
    public PagedResponse<BitacoraResponse> listar(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size) {
        return adminBitacoraService.listar(PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size))));
    }
}
