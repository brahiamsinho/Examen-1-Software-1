package com.plataforma.tramites.modules.seguridad.repository;

import com.plataforma.tramites.modules.seguridad.document.UsuarioDocument;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UsuarioRepository extends MongoRepository<UsuarioDocument, ObjectId> {

    Optional<UsuarioDocument> findByCorreo(String correo);

    long countByRolId(ObjectId rolId);
}
