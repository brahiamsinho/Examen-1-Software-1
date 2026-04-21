package com.plataforma.tramites.modules.seguridad.controller;

import com.plataforma.tramites.modules.seguridad.dto.ResponsableAreaContextResponse;
import com.plataforma.tramites.modules.seguridad.service.ResponsableAreaContextService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seguridad/responsable")
public class ResponsableAreaContextController {

    private final ResponsableAreaContextService responsableAreaContextService;

    public ResponsableAreaContextController(ResponsableAreaContextService responsableAreaContextService) {
        this.responsableAreaContextService = responsableAreaContextService;
    }

    @GetMapping("/contexto")
    public ResponsableAreaContextResponse contexto() {
        return responsableAreaContextService.obtenerContextoActual();
    }
}
