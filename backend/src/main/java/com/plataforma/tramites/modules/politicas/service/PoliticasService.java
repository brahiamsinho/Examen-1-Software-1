package com.plataforma.tramites.modules.politicas.service;

import com.plataforma.tramites.modules.politicas.repository.PoliticaNegocioRepository;
import com.plataforma.tramites.shared.dto.ModuleStatusResponse;
import org.springframework.stereotype.Service;

@Service
public class PoliticasService {

    private final PoliticaNegocioRepository politicaNegocioRepository;

    public PoliticasService(PoliticaNegocioRepository politicaNegocioRepository) {
        this.politicaNegocioRepository = politicaNegocioRepository;
    }

    public ModuleStatusResponse moduleStatus() {
        return new ModuleStatusResponse("politicas", "bootstrap");
    }
}
