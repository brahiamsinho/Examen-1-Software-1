package com.plataforma.tramites.modules.seguimiento.service;

import com.plataforma.tramites.modules.seguimiento.repository.RecorridoTramiteRepository;
import com.plataforma.tramites.shared.dto.ModuleStatusResponse;
import org.springframework.stereotype.Service;

@Service
public class SeguimientoService {

    private final RecorridoTramiteRepository recorridoTramiteRepository;

    public SeguimientoService(RecorridoTramiteRepository recorridoTramiteRepository) {
        this.recorridoTramiteRepository = recorridoTramiteRepository;
    }

    public ModuleStatusResponse moduleStatus() {
        return new ModuleStatusResponse("seguimiento", "bootstrap");
    }
}
