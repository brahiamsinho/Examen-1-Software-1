package com.plataforma.tramites.modules.tramites.controller;

import com.plataforma.tramites.modules.tramites.dto.RecorridoTramiteRequest;
import com.plataforma.tramites.modules.tramites.dto.RecorridoTramiteResponse;
import com.plataforma.tramites.modules.tramites.dto.TramiteActualizarRequest;
import com.plataforma.tramites.modules.tramites.dto.TramiteCreateRequest;
import com.plataforma.tramites.modules.tramites.dto.TramiteResponse;
import com.plataforma.tramites.modules.tramites.service.TramitesService;
import com.plataforma.tramites.shared.dto.ModuleStatusResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

import java.util.List;

@RestController
@RequestMapping("/api/tramites")
public class TramitesController {

    private final TramitesService tramitesService;

    public TramitesController(TramitesService tramitesService) {
        this.tramitesService = tramitesService;
    }

    @GetMapping("/status")
    public ModuleStatusResponse status() {
        return tramitesService.moduleStatus();
    }

    @GetMapping
    public Page<TramiteResponse> listar(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return tramitesService.listar(PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size))));
    }

    @GetMapping("/{id}")
    public TramiteResponse obtener(@PathVariable String id) {
        return tramitesService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TramiteResponse crear(@Valid @RequestBody TramiteCreateRequest body) {
        return tramitesService.crear(body);
    }

    @PatchMapping("/{id}")
    public TramiteResponse actualizar(@PathVariable String id, @Valid @RequestBody TramiteActualizarRequest body) {
        return tramitesService.actualizar(id, body);
    }

    /**
     * Cola FIFO (mismo estado; si se informa prioridad, filtra por ella — coherente con índice script.db).
     */
    @GetMapping("/cola/fifo")
    public List<TramiteResponse> colaFifo(
            @RequestParam String estado, @RequestParam(required = false) String prioridad) {
        return tramitesService.colaFifo(estado, prioridad);
    }

    @GetMapping("/{id}/recorridos")
    public List<RecorridoTramiteResponse> listarRecorridos(@PathVariable String id) {
        return tramitesService.listarRecorridos(id);
    }

    @PostMapping("/{id}/recorridos")
    @ResponseStatus(HttpStatus.CREATED)
    public RecorridoTramiteResponse registrarRecorrido(
            @PathVariable String id, @Valid @RequestBody RecorridoTramiteRequest body) {
        return tramitesService.registrarRecorrido(id, body);
    }
}
