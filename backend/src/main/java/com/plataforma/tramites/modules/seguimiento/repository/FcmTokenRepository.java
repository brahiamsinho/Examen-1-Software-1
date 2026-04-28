package com.plataforma.tramites.modules.seguimiento.repository;

import com.plataforma.tramites.modules.seguimiento.document.FcmTokenDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FcmTokenRepository extends MongoRepository<FcmTokenDocument, String> {

    List<FcmTokenDocument> findByUsuarioId(String usuarioId);

    void deleteByUsuarioIdAndFcmToken(String usuarioId, String fcmToken);

    void deleteByFcmToken(String fcmToken);
}
