package com.plataforma.tramites.modules.tramites.repository;

import com.plataforma.tramites.modules.tramites.document.RecorridoTramiteDocument;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RecorridoTramiteRepository extends MongoRepository<RecorridoTramiteDocument, ObjectId> {

    List<RecorridoTramiteDocument> findByTramiteIdOrderByFechaEntradaAsc(ObjectId tramiteId);
}
