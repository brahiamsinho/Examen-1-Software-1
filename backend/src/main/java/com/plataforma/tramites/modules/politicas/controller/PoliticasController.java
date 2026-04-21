package com.plataforma.tramites.modules.politicas.controller;

import com.plataforma.tramites.modules.politicas.dto.PoliticaNegocioResponse;
import com.plataforma.tramites.modules.politicas.dto.PoliticaUpsertRequest;
import com.plataforma.tramites.modules.politicas.service.PoliticasDominioService;
import com.plataforma.tramites.modules.politicas.service.PoliticasService;
import com.plataforma.tramites.shared.dto.ModuleStatusResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/politicas")
public class PoliticasController {

    private final PoliticasService politicasService;
    private final PoliticasDominioService politicasDominioService;

    public PoliticasController(PoliticasService politicasService, PoliticasDominioService politicasDominioService) {
        this.politicasService = politicasService;
        this.politicasDominioService = politicasDominioService;
    }

    @GetMapping("/status")
    public ModuleStatusResponse status() {
        return politicasService.moduleStatus();
    }

    @GetMapping
    public Page<PoliticaNegocioResponse> listar(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return politicasDominioService.listar(PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size))));
    }

    @GetMapping("/{id}")
    public PoliticaNegocioResponse obtener(@PathVariable String id) {
        return politicasDominioService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PoliticaNegocioResponse crear(@Valid @RequestBody PoliticaUpsertRequest body) {
        return politicasDominioService.crear(body);
    }

    @PutMapping("/{id}")
    public PoliticaNegocioResponse reemplazar(@PathVariable String id, @Valid @RequestBody PoliticaUpsertRequest body) {
        return politicasDominioService.reemplazar(id, body);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String id) {
        politicasDominioService.eliminar(id);
    }
}
