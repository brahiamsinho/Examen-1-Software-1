package com.plataforma.tramites.modules.seguridad.service;

import com.plataforma.tramites.modules.seguridad.document.RolDocument;
import com.plataforma.tramites.modules.seguridad.document.UsuarioDocument;
import com.plataforma.tramites.modules.seguridad.dto.LoginRequest;
import com.plataforma.tramites.modules.seguridad.dto.LoginResponse;
import com.plataforma.tramites.modules.seguridad.model.PortalRol;
import com.plataforma.tramites.modules.seguridad.repository.RolRepository;
import com.plataforma.tramites.modules.seguridad.repository.UsuarioRepository;
import com.plataforma.tramites.shared.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        String portalRol = request.getPortalRol() == null ? "" : request.getPortalRol().trim();
        PortalRol portal = PortalRol.fromCodigo(portalRol)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "portalRol inválido. Valores permitidos: " + PortalRol.codigos()));

        UsuarioDocument usuario = usuarioRepository
                .findByCorreo(request.getCorreo().trim().toLowerCase())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas."));

        if (!usuario.isEstado()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Usuario inactivo.");
        }

        if (!passwordEncoder.matches(request.getContrasena(), usuario.getContrasena())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas.");
        }

        RolDocument rol = rolRepository
                .findById(usuario.getRolId())
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Rol no encontrado."));

        if (!rol.getCodigo().equals(portal.getCodigo())) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "Este acceso es solo para el portal " + portal.getCodigo() + ".");
        }

        String token = jwtService.generateToken(
                usuario.getId().toHexString(), usuario.getCorreo(), rol.getCodigo());

        LoginResponse response = new LoginResponse();
        response.setAccessToken(token);
        response.setExpiresInSeconds(jwtService.getExpirationMillis() / 1000);
        response.setRolCodigo(rol.getCodigo());
        response.setNombres(usuario.getNombres());
        response.setApellidos(usuario.getApellidos());
        response.setCorreo(usuario.getCorreo());
        return response;
    }
}
