package com.plataforma.tramites.modules.tramites.repository;

import com.plataforma.tramites.modules.tramites.document.TramiteDocument;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface TramiteRepository extends MongoRepository<TramiteDocument, ObjectId> {

    Optional<TramiteDocument> findByCodigo(String codigo);

    Optional<TramiteDocument> findTopByOrderByNumeroTurnoDesc();

    Page<TramiteDocument> findAllByOrderByFechaRegistroDesc(Pageable pageable);

    List<TramiteDocument> findByEstadoOrderByFechaRegistroAscNumeroTurnoAsc(String estado);

    List<TramiteDocument> findByEstadoAndPrioridadOrderByFechaRegistroAscNumeroTurnoAsc(
            String estado, String prioridad);

    Page<TramiteDocument> findByPoliticaIdIsNullOrderByFechaRegistroDesc(Pageable pageable);
}
