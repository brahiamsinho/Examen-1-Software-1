package com.plataforma.tramites.modules.politicas.collab;

import com.plataforma.tramites.modules.seguridad.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * Autentica el handshake WebSocket con JWT en query {@code access_token} (el API {@code WebSocket} del navegador no
 * envía cabecera {@code Authorization}).
 */
@Component
public class PoliticasCollabJwtHandshakeInterceptor implements HandshakeInterceptor {

    public static final String QUERY_PARAM_TOKEN = "access_token";

    private final JwtService jwtService;

    public PoliticasCollabJwtHandshakeInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) {
        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            return false;
        }
        HttpServletRequest req = servletRequest.getServletRequest();
        String token = req.getParameter(QUERY_PARAM_TOKEN);
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            Claims claims = jwtService.parseClaims(token.trim());
            attributes.put(PoliticasCollabAttributes.USER_ID, claims.getSubject());
            attributes.put(PoliticasCollabAttributes.EMAIL, claims.get("email", String.class));
            attributes.put(PoliticasCollabAttributes.ROL, claims.get("rol", String.class));
            attributes.put(PoliticasCollabAttributes.DISPLAY_NAME, defaultDisplayName(claims));
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static String defaultDisplayName(Claims claims) {
        String email = claims.get("email", String.class);
        if (email != null && !email.isBlank()) {
            return email.trim();
        }
        return claims.getSubject();
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {
        // no-op
    }
}
