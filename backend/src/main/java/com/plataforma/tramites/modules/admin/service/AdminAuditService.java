package com.plataforma.tramites.modules.admin.service;

import com.plataforma.tramites.modules.admin.document.BitacoraDocument;
import com.plataforma.tramites.modules.admin.repository.BitacoraRepository;
import com.plataforma.tramites.modules.seguridad.repository.UsuarioRepository;
import org.bson.types.ObjectId;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class AdminAuditService {

    private final BitacoraRepository bitacoraRepository;
    private final UsuarioRepository usuarioRepository;

    public AdminAuditService(BitacoraRepository bitacoraRepository, UsuarioRepository usuarioRepository) {
        this.bitacoraRepository = bitacoraRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public void registrar(String accion, String entidad, String detalle) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String actorId = "";
        String actorCorreo = "";
        if (auth != null && auth.isAuthenticated() && auth.getName() != null) {
            actorId = auth.getName();
            if (ObjectId.isValid(actorId)) {
                actorCorreo = usuarioRepository
                        .findById(new ObjectId(actorId))
                        .map(u -> u.getCorreo() != null ? u.getCorreo() : "")
                        .orElse("");
            }
        }
        BitacoraDocument doc = new BitacoraDocument();
        doc.setFecha(Instant.now());
        doc.setActorUsuarioId(actorId);
        doc.setActorCorreo(actorCorreo);
        doc.setAccion(truncate(accion, 120));
        doc.setEntidad(truncate(entidad, 120));
        doc.setDetalle(truncate(detalle, 2000));
        bitacoraRepository.save(doc);
    }

    public Optional<ObjectId> currentActorObjectId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) {
            return Optional.empty();
        }
        if (!ObjectId.isValid(auth.getName())) {
            return Optional.empty();
        }
        return Optional.of(new ObjectId(auth.getName()));
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max);
    }
}
