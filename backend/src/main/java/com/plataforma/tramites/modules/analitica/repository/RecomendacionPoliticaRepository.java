package com.plataforma.tramites.modules.analitica.repository;

import com.plataforma.tramites.modules.analitica.document.RecomendacionPoliticaDocument;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RecomendacionPoliticaRepository extends MongoRepository<RecomendacionPoliticaDocument, ObjectId> {

    List<RecomendacionPoliticaDocument> findByPoliticaIdOrderByFechaGeneracionDesc(ObjectId politicaId);
}
