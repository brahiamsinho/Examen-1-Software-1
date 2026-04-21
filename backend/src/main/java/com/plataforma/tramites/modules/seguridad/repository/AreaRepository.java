package com.plataforma.tramites.modules.seguridad.repository;

import com.plataforma.tramites.modules.seguridad.document.AreaDocument;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AreaRepository extends MongoRepository<AreaDocument, ObjectId> {

    Optional<AreaDocument> findByNombreIgnoreCase(String nombre);
}
