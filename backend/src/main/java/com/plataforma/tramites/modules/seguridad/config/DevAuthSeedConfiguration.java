package com.plataforma.tramites.modules.seguridad.config;

import com.plataforma.tramites.modules.admin.document.PermisoDocument;
import com.plataforma.tramites.modules.admin.repository.PermisoRepository;
import com.plataforma.tramites.modules.seguridad.document.RolDocument;
import com.plataforma.tramites.modules.seguridad.document.UsuarioDocument;
import com.plataforma.tramites.modules.seguridad.model.PortalRol;
import com.plataforma.tramites.modules.seguridad.repository.RolRepository;
import com.plataforma.tramites.modules.seguridad.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Carga actores (roles + usuarios demo) en desarrollo de forma idempotente.
 * Desactivar en producción:
 * {@code app.dev.auth-seed-enabled=false}.
 */
@Configuration
public class DevAuthSeedConfiguration {

    private static final Logger log = LoggerFactory.getLogger(DevAuthSeedConfiguration.class);
    private static final String PASSWORD_DEMO = "demo123";

    @Bean
    @ConditionalOnProperty(
            prefix = "app.dev",
            name = "auth-seed-enabled",
            havingValue = "true",
            matchIfMissing = true)
    ApplicationRunner seedAuthData(
            RolRepository rolRepository,
            UsuarioRepository usuarioRepository,
            PermisoRepository permisoRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            log.warn("Semilla auth-dev habilitada: verificando actores (roles + usuarios demo).");

            Arrays.stream(PortalRol.values()).forEach(portalRol -> upsertRol(rolRepository, portalRol));
            ensurePermisosCatalog(permisoRepository);
            syncAdminRolePermisos(rolRepository, permisoRepository);

            String hashDemo = passwordEncoder.encode(PASSWORD_DEMO);
            upsertUsuario(
                    usuarioRepository,
                    rolRepository,
                    "admin@tramites.local",
                    "Admin",
                    "Sistema",
                    hashDemo,
                    PortalRol.ADMINISTRADOR);
            upsertUsuario(
                    usuarioRepository,
                    rolRepository,
                    "politicas@tramites.local",
                    "Disenador",
                    "Politicas",
                    hashDemo,
                    PortalRol.DISENADOR_POLITICAS);
            upsertUsuario(
                    usuarioRepository,
                    rolRepository,
                    "area@tramites.local",
                    "Responsable",
                    "Area",
                    hashDemo,
                    PortalRol.RESPONSABLE_AREA);
            upsertUsuario(
                    usuarioRepository,
                    rolRepository,
                    "cliente@tramites.local",
                    "Cliente",
                    "Demo",
                    hashDemo,
                    PortalRol.CLIENTE);
        };
    }

    private static RolDocument upsertRol(RolRepository rolRepository, PortalRol portalRol) {
        RolDocument rol = rolRepository
                .findByCodigo(portalRol.getCodigo())
                .orElseGet(RolDocument::new);
        rol.setCodigo(portalRol.getCodigo());
        rol.setNombre(portalRol.getNombre());
        if (rol.getPermisoCodigos() == null) {
            rol.setPermisoCodigos(new ArrayList<>());
        }
        return rolRepository.save(rol);
    }

    private static void ensurePermisosCatalog(PermisoRepository permisoRepository) {
        List<String[]> defs = List.of(
                new String[] {"SEG_USUARIOS_VER", "Ver usuarios", "seguridad", "Listar y consultar usuarios"},
                new String[] {
                    "SEG_USUARIOS_EDITAR", "Gestionar usuarios", "seguridad", "Crear, editar o eliminar usuarios"
                },
                new String[] {"SEG_ROLES_VER", "Ver roles", "seguridad", "Consultar roles del sistema"},
                new String[] {"SEG_ROLES_EDITAR", "Gestionar roles", "seguridad", "Crear o editar roles y permisos"},
                new String[] {"SEG_PERMISOS_VER", "Ver permisos", "seguridad", "Consultar catálogo de permisos"},
                new String[] {
                    "SEG_PERMISOS_EDITAR", "Gestionar permisos", "seguridad", "Crear o editar permisos del catálogo"
                },
                new String[] {"SEG_BITACORA_VER", "Ver bitácora", "seguridad", "Auditoría de acciones administrativas"});
        for (String[] row : defs) {
            upsertPermiso(permisoRepository, row[0], row[1], row[2], row[3]);
        }
    }

    private static void upsertPermiso(
            PermisoRepository permisoRepository, String codigo, String nombre, String modulo, String descripcion) {
        PermisoDocument p =
                permisoRepository.findByCodigo(codigo).orElseGet(PermisoDocument::new);
        p.setCodigo(codigo);
        p.setNombre(nombre);
        p.setModulo(modulo);
        p.setDescripcion(descripcion);
        permisoRepository.save(p);
    }

    private static void syncAdminRolePermisos(RolRepository rolRepository, PermisoRepository permisoRepository) {
        List<String> codigos = permisoRepository.findAll().stream()
                .map(PermisoDocument::getCodigo)
                .sorted()
                .toList();
        rolRepository
                .findByCodigo(PortalRol.ADMINISTRADOR.getCodigo())
                .ifPresent(admin -> {
                    admin.setPermisoCodigos(new ArrayList<>(codigos));
                    rolRepository.save(admin);
                });
    }

    private static void upsertUsuario(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            String correo,
            String nombres,
            String apellidos,
            String hash,
            PortalRol portalRol) {
        RolDocument rol = rolRepository.findByCodigo(portalRol.getCodigo())
                .orElseThrow(() -> new IllegalStateException("Rol no encontrado para " + portalRol.getCodigo()));

        Optional<UsuarioDocument> existente = usuarioRepository.findByCorreo(correo);
        UsuarioDocument usuario = existente.orElseGet(UsuarioDocument::new);
        usuario.setCorreo(correo);
        usuario.setNombres(nombres);
        usuario.setApellidos(apellidos);
        usuario.setTelefono(usuario.getTelefono() == null ? "" : usuario.getTelefono());
        usuario.setEstado(true);
        usuario.setRolId(rol.getId());
        if (existente.isEmpty()) {
            usuario.setContrasena(hash);
            log.warn("Semilla auth-dev: usuario '{}' creado con password demo.", correo);
        }
        usuarioRepository.save(usuario);
    }
}
