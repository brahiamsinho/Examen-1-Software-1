package com.plataforma.tramites.modules.politicas.repository;

import com.plataforma.tramites.modules.politicas.document.PoliticaNegocioDocument;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PoliticaNegocioRepository extends MongoRepository<PoliticaNegocioDocument, ObjectId> {

    Optional<PoliticaNegocioDocument> findByNombreIgnoreCaseAndVersion(String nombre, int version);

    Page<PoliticaNegocioDocument> findAllByOrderByFechaCreacionDesc(Pageable pageable);

    @Query("{ 'nodos.areaId': ?0 }")
    List<PoliticaNegocioDocument> findByNodoConAreaId(ObjectId areaId);
}
