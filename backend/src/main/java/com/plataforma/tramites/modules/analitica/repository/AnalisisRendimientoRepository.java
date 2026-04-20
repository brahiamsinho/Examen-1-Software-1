package com.plataforma.tramites.modules.analitica.repository;

import com.plataforma.tramites.modules.analitica.document.AnalisisRendimientoDocument;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AnalisisRendimientoRepository extends MongoRepository<AnalisisRendimientoDocument, ObjectId> {

    List<AnalisisRendimientoDocument> findByPoliticaIdOrderByFechaAnalisisDesc(ObjectId politicaId);
}
