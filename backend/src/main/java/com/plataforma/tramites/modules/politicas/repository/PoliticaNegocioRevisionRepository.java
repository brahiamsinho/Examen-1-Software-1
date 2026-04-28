package com.plataforma.tramites.modules.politicas.repository;

import com.plataforma.tramites.modules.politicas.document.PoliticaNegocioRevisionDocument;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PoliticaNegocioRevisionRepository extends MongoRepository<PoliticaNegocioRevisionDocument, ObjectId> {

    Optional<PoliticaNegocioRevisionDocument> findFirstByPoliticaIdOrderByRevisionDesc(ObjectId politicaId);

    Page<PoliticaNegocioRevisionDocument> findAllByPoliticaIdOrderByRevisionDesc(ObjectId politicaId, Pageable pageable);

    Optional<PoliticaNegocioRevisionDocument> findByPoliticaIdAndRevision(ObjectId politicaId, long revision);

    void deleteAllByPoliticaId(ObjectId politicaId);
}
