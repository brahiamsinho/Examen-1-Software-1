package com.plataforma.tramites.modules.seguimiento.repository;

import com.plataforma.tramites.modules.seguimiento.document.NotificacionDocument;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NotificacionRepository extends MongoRepository<NotificacionDocument, ObjectId> {

    Page<NotificacionDocument> findByUsuarioIdOrderByFechaEnvioDesc(ObjectId usuarioId, Pageable pageable);
}
