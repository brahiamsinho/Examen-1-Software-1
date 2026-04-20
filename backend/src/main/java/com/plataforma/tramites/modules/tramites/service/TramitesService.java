package com.plataforma.tramites.modules.tramites.service;

import com.plataforma.tramites.modules.tramites.repository.TramiteRepository;
import com.plataforma.tramites.shared.dto.ModuleStatusResponse;
import org.springframework.stereotype.Service;

@Service
public class TramitesService {

    private final TramiteRepository tramiteRepository;

    public TramitesService(TramiteRepository tramiteRepository) {
        this.tramiteRepository = tramiteRepository;
    }

    public ModuleStatusResponse moduleStatus() {
        return new ModuleStatusResponse("tramites", "bootstrap");
    }
}
