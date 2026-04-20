package com.plataforma.tramites.shared.config;

import org.springframework.context.annotation.Configuration;

/**
 * Punto de extensión para caché, pub/sub o rate limiting con Redis.
 * Spring Boot ya auto-configura {@link org.springframework.data.redis.connection.RedisConnectionFactory}.
 */
@Configuration
public class RedisInfrastructureConfiguration {
}
