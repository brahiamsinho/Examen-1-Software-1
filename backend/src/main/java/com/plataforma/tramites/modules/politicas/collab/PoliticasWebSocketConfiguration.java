package com.plataforma.tramites.modules.politicas.collab;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Configuration
@EnableWebSocket
public class PoliticasWebSocketConfiguration implements WebSocketConfigurer {

    private final PoliticasCollabWebSocketHandler politicasCollabWebSocketHandler;
    private final PoliticasCollabJwtHandshakeInterceptor politicasCollabJwtHandshakeInterceptor;

    @Value("${app.cors.allowed-origins:http://localhost:4200}")
    private String allowedOrigins;

    public PoliticasWebSocketConfiguration(
            PoliticasCollabWebSocketHandler politicasCollabWebSocketHandler,
            PoliticasCollabJwtHandshakeInterceptor politicasCollabJwtHandshakeInterceptor) {
        this.politicasCollabWebSocketHandler = politicasCollabWebSocketHandler;
        this.politicasCollabJwtHandshakeInterceptor = politicasCollabJwtHandshakeInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        Set<String> patterns = new LinkedHashSet<>();
        java.util.Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(patterns::add);
        patterns.addAll(
                List.of("http://localhost:*", "http://127.0.0.1:*", "https://localhost:*", "https://127.0.0.1:*"));
        if (patterns.isEmpty()) {
            patterns.add("http://localhost:4200");
        }
        registry.addHandler(politicasCollabWebSocketHandler, "/ws/politicas")
                .addInterceptors(politicasCollabJwtHandshakeInterceptor)
                .setAllowedOriginPatterns(patterns.toArray(String[]::new));
    }
}
