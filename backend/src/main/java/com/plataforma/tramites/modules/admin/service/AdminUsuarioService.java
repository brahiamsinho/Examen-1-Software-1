package com.plataforma.tramites.modules.admin.service;

import com.plataforma.tramites.modules.admin.dto.PagedResponse;
import com.plataforma.tramites.modules.admin.dto.UsuarioAdminCreateRequest;
import com.plataforma.tramites.modules.admin.dto.UsuarioAdminResponse;
import com.plataforma.tramites.modules.admin.dto.UsuarioAdminUpdateRequest;
import com.plataforma.tramites.modules.seguridad.document.RolDocument;
import com.plataforma.tramites.modules.seguridad.document.UsuarioDocument;
import com.plataforma.tramites.modules.seguridad.repository.RolRepository;
import com.plataforma.tramites.modules.seguridad.repository.UsuarioRepository;
import com.plataforma.tramites.shared.exception.ApiException;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminAuditService adminAuditService;

    public AdminUsuarioService(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder,
            AdminAuditService adminAuditService) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminAuditService = adminAuditService;
    }

    public PagedResponse<UsuarioAdminResponse> listar(Pageable pageable) {
        Page<UsuarioDocument> page = usuarioRepository.findAll(pageable);
        Map<ObjectId, String> rolCodigoPorId = rolRepository.findAll().stream()
                .collect(Collectors.toMap(RolDocument::getId, RolDocument::getCodigo, (a, b) -> a));
        var content = page.getContent().stream()
                .map(u -> toResponse(u, rolCodigoPorId.get(u.getRolId())))
                .toList();
        return new PagedResponse<>(
                content,
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize());
    }

    public UsuarioAdminResponse crear(UsuarioAdminCreateRequest req) {
        String correo = req.getCorreo().trim().toLowerCase();
        if (usuarioRepository.findByCorreo(correo).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe un usuario con ese correo.");
        }
        RolDocument rol = rolRepository
                .findByCodigo(req.getRolCodigo().trim())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Rol no encontrado."));
        UsuarioDocument u = new UsuarioDocument();
        u.setCorreo(correo);
        u.setNombres(req.getNombres().trim());
        u.setApellidos(req.getApellidos().trim());
        u.setTelefono(req.getTelefono() != null ? req.getTelefono().trim() : "");
        u.setEstado(req.isEstado());
        u.setContrasena(passwordEncoder.encode(req.getContrasena()));
        u.setRolId(rol.getId());
        UsuarioDocument saved = usuarioRepository.save(u);
        adminAuditService.registrar(
                "USUARIO_CREAR", "Usuario", "correo=" + saved.getCorreo() + ", rol=" + rol.getCodigo());
        return toResponse(saved, rol.getCodigo());
    }

    public UsuarioAdminResponse actualizar(String id, UsuarioAdminUpdateRequest req) {
        ObjectId oid = parseId(id);
        UsuarioDocument u = usuarioRepository
                .findById(oid)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));
        forbidSelfLockout(oid, req.getEstado());
        if (req.getCorreo() != null && !req.getCorreo().isBlank()) {
            String nuevo = req.getCorreo().trim().toLowerCase();
            usuarioRepository
                    .findByCorreo(nuevo)
                    .filter(other -> !other.getId().equals(oid))
                    .ifPresent(x -> {
                        throw new ApiException(HttpStatus.CONFLICT, "Correo ya usado por otro usuario.");
                    });
            u.setCorreo(nuevo);
        }
        if (req.getNombres() != null) {
            u.setNombres(req.getNombres().trim());
        }
        if (req.getApellidos() != null) {
            u.setApellidos(req.getApellidos().trim());
        }
        if (req.getTelefono() != null) {
            u.setTelefono(req.getTelefono().trim());
        }
        if (req.getEstado() != null) {
            u.setEstado(req.getEstado());
        }
        if (req.getContrasena() != null && !req.getContrasena().isBlank()) {
            u.setContrasena(passwordEncoder.encode(req.getContrasena()));
        }
        String rolCodigo = null;
        if (req.getRolCodigo() != null && !req.getRolCodigo().isBlank()) {
            RolDocument rol = rolRepository
                    .findByCodigo(req.getRolCodigo().trim())
                    .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Rol no encontrado."));
            u.setRolId(rol.getId());
            rolCodigo = rol.getCodigo();
        }
        UsuarioDocument saved = usuarioRepository.save(u);
        if (rolCodigo == null) {
            rolCodigo = rolRepository.findById(saved.getRolId()).map(RolDocument::getCodigo).orElse("");
        }
        adminAuditService.registrar("USUARIO_ACTUALIZAR", "Usuario", "id=" + saved.getId().toHexString());
        return toResponse(saved, rolCodigo);
    }

    public void eliminar(String id) {
        ObjectId oid = parseId(id);
        adminAuditService
                .currentActorObjectId()
                .filter(self -> self.equals(oid))
                .ifPresent(x -> {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "No podés eliminar tu propio usuario.");
                });
        if (!usuarioRepository.existsById(oid)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado.");
        }
        usuarioRepository.deleteById(oid);
        adminAuditService.registrar("USUARIO_ELIMINAR", "Usuario", "id=" + oid.toHexString());
    }

    private void forbidSelfLockout(ObjectId targetId, Boolean nuevoEstado) {
        if (nuevoEstado == null || nuevoEstado) {
            return;
        }
        adminAuditService
                .currentActorObjectId()
                .filter(self -> self.equals(targetId))
                .ifPresent(x -> {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "No podés desactivar tu propio usuario.");
                });
    }

    private static ObjectId parseId(String id) {
        if (!ObjectId.isValid(id)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Id inválido.");
        }
        return new ObjectId(id);
    }

    private static UsuarioAdminResponse toResponse(UsuarioDocument u, String rolCodigo) {
        UsuarioAdminResponse r = new UsuarioAdminResponse();
        r.setId(u.getId().toHexString());
        r.setCorreo(u.getCorreo());
        r.setNombres(u.getNombres());
        r.setApellidos(u.getApellidos());
        r.setTelefono(u.getTelefono());
        r.setEstado(u.isEstado());
        r.setRolId(u.getRolId() != null ? u.getRolId().toHexString() : null);
        r.setRolCodigo(rolCodigo != null ? rolCodigo : "");
        return r;
    }
}
