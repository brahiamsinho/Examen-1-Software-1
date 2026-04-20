package com.plataforma.tramites.modules.admin.service;

import com.plataforma.tramites.modules.admin.dto.RolAdminCreateRequest;
import com.plataforma.tramites.modules.admin.dto.RolAdminResponse;
import com.plataforma.tramites.modules.admin.dto.RolAdminUpdateRequest;
import com.plataforma.tramites.modules.admin.repository.PermisoRepository;
import com.plataforma.tramites.modules.seguridad.document.RolDocument;
import com.plataforma.tramites.modules.seguridad.repository.RolRepository;
import com.plataforma.tramites.modules.seguridad.repository.UsuarioRepository;
import com.plataforma.tramites.shared.exception.ApiException;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminRolService {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PermisoRepository permisoRepository;
    private final AdminAuditService adminAuditService;

    public AdminRolService(
            RolRepository rolRepository,
            UsuarioRepository usuarioRepository,
            PermisoRepository permisoRepository,
            AdminAuditService adminAuditService) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.permisoRepository = permisoRepository;
        this.adminAuditService = adminAuditService;
    }

    public List<RolAdminResponse> listar() {
        return rolRepository.findAll().stream().map(AdminRolService::toResponse).toList();
    }

    public RolAdminResponse crear(RolAdminCreateRequest req) {
        String codigo = req.getCodigo().trim();
        if (rolRepository.findByCodigo(codigo).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe un rol con ese código.");
        }
        RolDocument r = new RolDocument();
        r.setCodigo(codigo);
        r.setNombre(req.getNombre().trim());
        r.setPermisoCodigos(new ArrayList<>());
        RolDocument saved = rolRepository.save(r);
        adminAuditService.registrar("ROL_CREAR", "Rol", "codigo=" + saved.getCodigo());
        return toResponse(saved);
    }

    public RolAdminResponse actualizar(String id, RolAdminUpdateRequest req) {
        RolDocument r = rolRepository
                .findById(parseId(id))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Rol no encontrado."));
        if (req.getNombre() != null && !req.getNombre().isBlank()) {
            r.setNombre(req.getNombre().trim());
        }
        if (req.getPermisoCodigos() != null) {
            validarPermisos(req.getPermisoCodigos());
            r.setPermisoCodigos(new ArrayList<>(req.getPermisoCodigos()));
        }
        RolDocument saved = rolRepository.save(r);
        adminAuditService.registrar("ROL_ACTUALIZAR", "Rol", "id=" + saved.getId().toHexString());
        return toResponse(saved);
    }

    public void eliminar(String id) {
        RolDocument r = rolRepository
                .findById(parseId(id))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Rol no encontrado."));
        long usuarios = usuarioRepository.countByRolId(r.getId());
        if (usuarios > 0) {
            throw new ApiException(
                    HttpStatus.CONFLICT, "No se puede eliminar el rol: hay " + usuarios + " usuario(s) asignados.");
        }
        rolRepository.deleteById(r.getId());
        adminAuditService.registrar("ROL_ELIMINAR", "Rol", "codigo=" + r.getCodigo());
    }

    private void validarPermisos(List<String> codigos) {
        for (String c : codigos) {
            if (c == null || c.isBlank()) {
                continue;
            }
            String code = c.trim();
            if (!permisoRepository.existsByCodigo(code)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Permiso inexistente: " + code);
            }
        }
    }

    private static ObjectId parseId(String id) {
        if (!ObjectId.isValid(id)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Id inválido.");
        }
        return new ObjectId(id);
    }

    private static RolAdminResponse toResponse(RolDocument r) {
        RolAdminResponse dto = new RolAdminResponse();
        dto.setId(r.getId().toHexString());
        dto.setCodigo(r.getCodigo());
        dto.setNombre(r.getNombre());
        dto.setPermisoCodigos(r.getPermisoCodigos() != null ? new ArrayList<>(r.getPermisoCodigos()) : new ArrayList<>());
        return dto;
    }
}
