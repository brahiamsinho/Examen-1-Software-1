package com.plataforma.tramites.modules.seguridad.service;

import com.plataforma.tramites.modules.seguridad.document.UsuarioDocument;
import com.plataforma.tramites.modules.seguridad.dto.UsuarioAreaResponse;
import com.plataforma.tramites.modules.seguridad.repository.UsuarioRepository;
import com.plataforma.tramites.shared.exception.ApiException;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeguridadUsuarioConsultaService {

    private final UsuarioRepository usuarioRepository;

    public SeguridadUsuarioConsultaService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<UsuarioAreaResponse> listarActivosPorArea(String areaIdHex) {
        ObjectId areaId = parseObjectId(areaIdHex, "areaId");
        return usuarioRepository.findByAreaIdAndEstadoTrueOrderByCorreoAsc(areaId).stream()
                .map(SeguridadUsuarioConsultaService::toDto)
                .toList();
    }

    private static UsuarioAreaResponse toDto(UsuarioDocument u) {
        return new UsuarioAreaResponse(
                u.getId().toHexString(),
                u.getCorreo(),
                u.getNombres(),
                u.getApellidos());
    }

    private static ObjectId parseObjectId(String hex, String campo) {
        if (hex == null || hex.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, campo + " es obligatorio.");
        }
        try {
            return new ObjectId(hex.trim());
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, campo + " inválido.");
        }
    }
}
