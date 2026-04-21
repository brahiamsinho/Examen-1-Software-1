package com.plataforma.tramites.modules.seguridad.service;

import com.plataforma.tramites.modules.seguridad.repository.UsuarioRepository;
import com.plataforma.tramites.shared.dto.InfraHealthResponse;
import com.plataforma.tramites.shared.dto.ModuleStatusResponse;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Service;

@Service
public class SeguridadService {

    private final UsuarioRepository usuarioRepository;
    private final RedisConnectionFactory redisConnectionFactory;

    public SeguridadService(UsuarioRepository usuarioRepository, RedisConnectionFactory redisConnectionFactory) {
        this.usuarioRepository = usuarioRepository;
        this.redisConnectionFactory = redisConnectionFactory;
    }

    public ModuleStatusResponse moduleStatus() {
        return new ModuleStatusResponse("seguridad", "bootstrap");
    }

    public InfraHealthResponse checkInfra() {
        boolean mongoOk = false;
        String mongoDetail = "ok";
        try {
            mongoOk = usuarioRepository.count() >= 0;
        } catch (DataAccessException ex) {
            mongoDetail = ex.getMessage();
        }
        boolean redisOk = false;
        String redisDetail = "ok";
        try (RedisConnection c = redisConnectionFactory.getConnection()) {
            String pong = c.ping();
            redisOk = "PONG".equalsIgnoreCase(pong);
            if (!redisOk) {
                redisDetail = "respuesta inesperada: " + pong;
            }
        } catch (Exception ex) {
            redisDetail = ex.getMessage();
        }
        return new InfraHealthResponse(mongoOk, redisOk, mongoDetail, redisDetail);
    }
}
