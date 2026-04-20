package com.plataforma.tramites.modules.seguridad.controller;

import com.plataforma.tramites.modules.seguridad.dto.LoginRequest;
import com.plataforma.tramites.modules.seguridad.dto.LoginResponse;
import com.plataforma.tramites.modules.seguridad.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
