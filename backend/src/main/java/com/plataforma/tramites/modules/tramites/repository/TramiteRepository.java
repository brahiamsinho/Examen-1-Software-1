package com.plataforma.tramites.modules.tramites.repository;

import com.plataforma.tramites.modules.tramites.document.TramiteDocument;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface TramiteRepository extends MongoRepository<TramiteDocument, ObjectId> {

    Optional<TramiteDocument> findByCodigo(String codigo);
}
