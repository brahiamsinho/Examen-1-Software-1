package com.plataforma.tramites.modules.seguimiento.repository;

import com.plataforma.tramites.modules.seguimiento.document.BitacoraEventoDocument;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BitacoraEventoRepository extends MongoRepository<BitacoraEventoDocument, ObjectId> {

    Page<BitacoraEventoDocument> findByTramiteIdOrderByFechaHoraDesc(ObjectId tramiteId, Pageable pageable);
}
