package com.plataforma.tramites.modules.admin.repository;

import com.plataforma.tramites.modules.admin.document.PermisoDocument;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PermisoRepository extends MongoRepository<PermisoDocument, ObjectId> {

    Optional<PermisoDocument> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
}
