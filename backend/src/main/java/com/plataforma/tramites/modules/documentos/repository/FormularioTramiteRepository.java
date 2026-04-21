package com.plataforma.tramites.modules.documentos.repository;

import com.plataforma.tramites.modules.documentos.document.FormularioTramiteDocument;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FormularioTramiteRepository extends MongoRepository<FormularioTramiteDocument, ObjectId> {

    List<FormularioTramiteDocument> findByTramiteIdOrderByFechaRegistroDesc(ObjectId tramiteId);
}
