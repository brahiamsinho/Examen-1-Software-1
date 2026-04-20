package com.plataforma.tramites.modules.admin.service;

import com.plataforma.tramites.modules.admin.document.PermisoDocument;
import com.plataforma.tramites.modules.admin.dto.PermisoCreateRequest;
import com.plataforma.tramites.modules.admin.dto.PermisoResponse;
import com.plataforma.tramites.modules.admin.dto.PermisoUpdateRequest;
import com.plataforma.tramites.modules.admin.repository.PermisoRepository;
import com.plataforma.tramites.modules.seguridad.document.RolDocument;
import com.plataforma.tramites.modules.seguridad.repository.RolRepository;
import com.plataforma.tramites.shared.exception.ApiException;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminPermisoService {

    private final PermisoRepository permisoRepository;
    private final RolRepository rolRepository;
    private final AdminAuditService adminAuditService;

    public AdminPermisoService(
            PermisoRepository permisoRepository, RolRepository rolRepository, AdminAuditService adminAuditService) {
        this.permisoRepository = permisoRepository;
        this.rolRepository = rolRepository;
        this.adminAuditService = adminAuditService;
    }

    public List<PermisoResponse> listar() {
        return permisoRepository.findAll().stream().map(AdminPermisoService::toResponse).toList();
    }

    public PermisoResponse crear(PermisoCreateRequest req) {
        String codigo = req.getCodigo().trim();
        if (permisoRepository.existsByCodigo(codigo)) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe un permiso con ese código.");
        }
        PermisoDocument p = new PermisoDocument();
        p.setCodigo(codigo);
        p.setNombre(req.getNombre().trim());
        p.setDescripcion(req.getDescripcion() != null ? req.getDescripcion().trim() : "");
        p.setModulo(req.getModulo().trim());
        PermisoDocument saved = permisoRepository.save(p);
        adminAuditService.registrar("PERMISO_CREAR", "Permiso", "codigo=" + saved.getCodigo());
        return toResponse(saved);
    }

    public PermisoResponse actualizar(String id, PermisoUpdateRequest req) {
        PermisoDocument p = permisoRepository
                .findById(parseId(id))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Permiso no encontrado."));
        if (req.getNombre() != null && !req.getNombre().isBlank()) {
            p.setNombre(req.getNombre().trim());
        }
        if (req.getDescripcion() != null) {
            p.setDescripcion(req.getDescripcion().trim());
        }
        if (req.getModulo() != null && !req.getModulo().isBlank()) {
            p.setModulo(req.getModulo().trim());
        }
        PermisoDocument saved = permisoRepository.save(p);
        adminAuditService.registrar("PERMISO_ACTUALIZAR", "Permiso", "codigo=" + saved.getCodigo());
        return toResponse(saved);
    }

    public void eliminar(String id) {
        PermisoDocument p = permisoRepository
                .findById(parseId(id))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Permiso no encontrado."));
        String codigo = p.getCodigo();
        for (RolDocument rol : rolRepository.findAll()) {
            List<String> pc = rol.getPermisoCodigos();
            if (pc != null && pc.contains(codigo)) {
                throw new ApiException(
                        HttpStatus.CONFLICT,
                        "No se puede eliminar: el permiso está asignado al rol " + rol.getCodigo() + ".");
            }
        }
        permisoRepository.deleteById(p.getId());
        adminAuditService.registrar("PERMISO_ELIMINAR", "Permiso", "codigo=" + codigo);
    }

    private static ObjectId parseId(String id) {
        if (!ObjectId.isValid(id)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Id inválido.");
        }
        return new ObjectId(id);
    }

    private static PermisoResponse toResponse(PermisoDocument p) {
        PermisoResponse r = new PermisoResponse();
        r.setId(p.getId().toHexString());
        r.setCodigo(p.getCodigo());
        r.setNombre(p.getNombre());
        r.setDescripcion(p.getDescripcion());
        r.setModulo(p.getModulo());
        return r;
    }
}
