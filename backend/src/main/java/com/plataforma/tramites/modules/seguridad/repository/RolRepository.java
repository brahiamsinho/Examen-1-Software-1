package com.plataforma.tramites.modules.seguridad.repository;

import com.plataforma.tramites.modules.seguridad.document.RolDocument;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RolRepository extends MongoRepository<RolDocument, ObjectId> {

    Optional<RolDocument> findByCodigo(String codigo);
}
