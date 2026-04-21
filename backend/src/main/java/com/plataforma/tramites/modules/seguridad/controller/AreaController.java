package com.plataforma.tramites.modules.seguridad.controller;

import com.plataforma.tramites.modules.seguridad.dto.AreaResponse;
import com.plataforma.tramites.modules.seguridad.dto.AreaUpsertRequest;
import com.plataforma.tramites.modules.seguridad.service.AreaCatalogoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/seguridad/areas")
public class AreaController {

    private final AreaCatalogoService areaCatalogoService;

    public AreaController(AreaCatalogoService areaCatalogoService) {
        this.areaCatalogoService = areaCatalogoService;
    }

    @GetMapping
    public List<AreaResponse> listar() {
        return areaCatalogoService.listar();
    }

    @GetMapping("/{id}")
    public AreaResponse obtener(@PathVariable String id) {
        return areaCatalogoService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AreaResponse crear(@Valid @RequestBody AreaUpsertRequest body) {
        return areaCatalogoService.crear(body);
    }

    @PutMapping("/{id}")
    public AreaResponse actualizar(@PathVariable String id, @Valid @RequestBody AreaUpsertRequest body) {
        return areaCatalogoService.actualizar(id, body);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String id) {
        areaCatalogoService.eliminar(id);
    }
}
