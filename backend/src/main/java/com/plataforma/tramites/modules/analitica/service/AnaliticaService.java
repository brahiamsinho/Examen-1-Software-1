package com.plataforma.tramites.modules.analitica.service;

import com.plataforma.tramites.modules.analitica.repository.AnalisisRendimientoRepository;
import com.plataforma.tramites.shared.dto.ModuleStatusResponse;
import org.springframework.stereotype.Service;

@Service
public class AnaliticaService {

    private final AnalisisRendimientoRepository analisisRendimientoRepository;

    public AnaliticaService(AnalisisRendimientoRepository analisisRendimientoRepository) {
        this.analisisRendimientoRepository = analisisRendimientoRepository;
    }

    public ModuleStatusResponse moduleStatus() {
        return new ModuleStatusResponse("analitica", "bootstrap");
    }
}
