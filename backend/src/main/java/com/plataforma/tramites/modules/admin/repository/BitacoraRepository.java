package com.plataforma.tramites.modules.admin.repository;

import com.plataforma.tramites.modules.admin.document.BitacoraDocument;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BitacoraRepository extends MongoRepository<BitacoraDocument, ObjectId> {

    Page<BitacoraDocument> findAllByOrderByFechaDesc(Pageable pageable);
}
