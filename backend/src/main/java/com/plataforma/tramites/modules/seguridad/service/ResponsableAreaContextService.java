package com.plataforma.tramites.modules.seguridad.service;

import com.plataforma.tramites.modules.politicas.document.PoliticaNegocioDocument;
import com.plataforma.tramites.modules.politicas.repository.PoliticaNegocioRepository;
import com.plataforma.tramites.modules.seguridad.document.AreaDocument;
import com.plataforma.tramites.modules.seguridad.document.UsuarioDocument;
import com.plataforma.tramites.modules.seguridad.dto.PoliticaAreaResumenDto;
import com.plataforma.tramites.modules.seguridad.dto.ResponsableAreaContextResponse;
import com.plataforma.tramites.modules.seguridad.repository.AreaRepository;
import com.plataforma.tramites.modules.seguridad.repository.UsuarioRepository;
import com.plataforma.tramites.shared.exception.ApiException;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class ResponsableAreaContextService {

    private static final SimpleGrantedAuthority ROL_RESPONSABLE =
            new SimpleGrantedAuthority("ROLE_RESPONSABLE_AREA");

    private final UsuarioRepository usuarioRepository;
    private final AreaRepository areaRepository;
    private final PoliticaNegocioRepository politicaNegocioRepository;

    public ResponsableAreaContextService(
            UsuarioRepository usuarioRepository,
            AreaRepository areaRepository,
            PoliticaNegocioRepository politicaNegocioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.areaRepository = areaRepository;
        this.politicaNegocioRepository = politicaNegocioRepository;
    }

    public ResponsableAreaContextResponse obtenerContextoActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null
                || auth.getAuthorities().stream().noneMatch(ROL_RESPONSABLE::equals)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Este recurso es solo para responsables de área.");
        }
        String subject = auth.getName();
        if (!ObjectId.isValid(subject)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Sesión inválida.");
        }
        ObjectId usuarioId = new ObjectId(subject);
        UsuarioDocument usuario = usuarioRepository
                .findById(usuarioId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));
        if (usuario.getAreaId() == null) {
            return new ResponsableAreaContextResponse(false, null, null, null, List.of());
        }
        ObjectId areaId = usuario.getAreaId();
        var areaOpt = areaRepository.findById(areaId);
        String nombre = areaOpt.map(AreaDocument::getNombre).orElse("Área no disponible en catálogo");
        String descripcion = areaOpt
                .map(AreaDocument::getDescripcion)
                .map(d -> d != null ? d : "")
                .orElse("Tu usuario apunta a un área que fue eliminada. Pedí al administrador que actualice tu perfil.");
        List<PoliticaAreaResumenDto> politicas = politicaNegocioRepository.findByNodoConAreaId(areaId).stream()
                .sorted(Comparator.comparing(PoliticaNegocioDocument::getNombre, String.CASE_INSENSITIVE_ORDER))
                .map(this::toPoliticaResumen)
                .toList();
        return new ResponsableAreaContextResponse(true, areaId.toHexString(), nombre, descripcion, politicas);
    }

    private PoliticaAreaResumenDto toPoliticaResumen(PoliticaNegocioDocument p) {
        return new PoliticaAreaResumenDto(
                p.getId().toHexString(),
                p.getNombre(),
                p.getVersion(),
                p.getEstado());
    }
}
