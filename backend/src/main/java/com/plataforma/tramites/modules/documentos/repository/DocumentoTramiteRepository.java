package com.plataforma.tramites.modules.documentos.repository;

import com.plataforma.tramites.modules.documentos.document.DocumentoTramiteDocument;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DocumentoTramiteRepository extends MongoRepository<DocumentoTramiteDocument, ObjectId> {

    List<DocumentoTramiteDocument> findByTramiteIdOrderByFechaCargaDesc(ObjectId tramiteId);
}
