package com.plataforma.tramites.modules.seguridad.service;

import com.plataforma.tramites.modules.seguridad.repository.UsuarioRepository;
import com.plataforma.tramites.shared.dto.ModuleStatusResponse;
import org.springframework.stereotype.Service;

@Service
public class SeguridadService {

    private final UsuarioRepository usuarioRepository;

    public SeguridadService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public ModuleStatusResponse moduleStatus() {
        return new ModuleStatusResponse("seguridad", "bootstrap");
    }
}
