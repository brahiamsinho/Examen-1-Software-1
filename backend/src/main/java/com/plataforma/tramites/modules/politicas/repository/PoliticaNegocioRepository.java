package com.plataforma.tramites.modules.politicas.repository;

import com.plataforma.tramites.modules.politicas.document.PoliticaNegocioDocument;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PoliticaNegocioRepository extends MongoRepository<PoliticaNegocioDocument, ObjectId> {
}
