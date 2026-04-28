package com.plataforma.tramites.modules.politicas.service;

import com.plataforma.tramites.modules.politicas.document.PoliticaNegocioDocument;
import com.plataforma.tramites.modules.politicas.dto.PoliticaNegocioResponse;
import com.plataforma.tramites.modules.politicas.dto.PoliticaUpsertRequest;
import com.plataforma.tramites.modules.politicas.repository.PoliticaNegocioRepository;
import com.plataforma.tramites.modules.politicas.repository.PoliticaNegocioRevisionRepository;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * {@code reemplazar} debe inicializar {@code lockVersion} en documentos viejos sin el campo
 * (migración legacy), aceptar {@code lockVersion: 0} en el cuerpo y persistir sin error.
 */
@DataMongoTest
@Testcontainers
@Import(PoliticasDominioService.class)
class PoliticasDominioServiceLegacyLockMongoIT {

    @Container
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7.0");

    @DynamicPropertySource
    static void mongoProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO::getReplicaSetUrl);
    }

    @Autowired
    private PoliticaNegocioRepository politicaNegocioRepository;
    @Autowired
    private PoliticaNegocioRevisionRepository politicaNegocioRevisionRepository;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private PoliticasDominioService politicasDominioService;

    @AfterEach
    void tearDown() {
        politicaNegocioRevisionRepository.deleteAll();
        politicaNegocioRepository.deleteAll();
    }

    @Test
    void reemplazar_politicaSinCampoLockVersion_inicializaLockYGuarda() {
        ObjectId id = new ObjectId();
        insertLegacyPoliticaSinLockVersion(id);

        PoliticaNegocioDocument antes = politicaNegocioRepository.findById(id).orElseThrow();
        assertNotNull(antes);
        // Documento leído sin el campo en BSON → null
        assertEquals(null, antes.getLockVersion());

        PoliticaUpsertRequest body = upsertMismaForma("LegacyPol", "desc");
        body.setLockVersion(0L);

        PoliticaNegocioResponse out =
                politicasDominioService.reemplazar(id.toHexString(), body);

        assertEquals(1L, out.lockVersion());
        PoliticaNegocioDocument despues = politicaNegocioRepository.findById(id).orElseThrow();
        assertNotNull(despues.getLockVersion());
        assertEquals(1L, despues.getLockVersion());
    }

    /** Inserta en Mongo sin clave {@code lockVersion} (como políticas anteriores al optimistic lock). */
    private void insertLegacyPoliticaSinLockVersion(ObjectId id) {
        Document n1 = new Document();
        n1.append("idNodo", "n1");
        n1.append("nombre", "Inicio");
        n1.append("tipoNodo", "INICIO");
        n1.append("orden", 0);
        n1.append("esInicial", true);
        n1.append("esFinal", false);
        n1.append("asignacionesResponsable", List.of());
        Document n2 = new Document();
        n2.append("idNodo", "n2");
        n2.append("nombre", "Fin");
        n2.append("tipoNodo", "FIN");
        n2.append("orden", 1);
        n2.append("esInicial", false);
        n2.append("esFinal", true);
        n2.append("asignacionesResponsable", List.of());
        Document c1 = new Document();
        c1.append("idConexion", "c1");
        c1.append("tipoFlujo", "SECUENCIAL");
        c1.append("origenNodoId", "n1");
        c1.append("destinoNodoId", "n2");
        Document root = new Document();
        root.append("_id", id);
        root.append("nombre", "LegacyPol");
        root.append("descripcion", "desc");
        root.append("version", 1);
        root.append("estado", "BORRADOR");
        root.append("fechaCreacion", Instant.now());
        root.append("nodos", List.of(n1, n2));
        root.append("conexiones", List.of(c1));
        mongoTemplate.getCollection("politicas_negocio").insertOne(root);
    }

    private static PoliticaUpsertRequest upsertMismaForma(String nombre, String descripcion) {
        PoliticaUpsertRequest.NodoPoliticaRequest n1 = new PoliticaUpsertRequest.NodoPoliticaRequest();
        n1.setIdNodo("n1");
        n1.setNombre("Inicio");
        n1.setTipoNodo("INICIO");
        n1.setOrden(0);
        n1.setEsInicial(true);
        n1.setEsFinal(false);
        PoliticaUpsertRequest.NodoPoliticaRequest n2 = new PoliticaUpsertRequest.NodoPoliticaRequest();
        n2.setIdNodo("n2");
        n2.setNombre("Fin");
        n2.setTipoNodo("FIN");
        n2.setOrden(1);
        n2.setEsInicial(false);
        n2.setEsFinal(true);
        PoliticaUpsertRequest.ConexionFlujoRequest c1 = new PoliticaUpsertRequest.ConexionFlujoRequest();
        c1.setIdConexion("c1");
        c1.setTipoFlujo("SECUENCIAL");
        c1.setOrigenNodoId("n1");
        c1.setDestinoNodoId("n2");
        PoliticaUpsertRequest req = new PoliticaUpsertRequest();
        req.setNombre(nombre);
        req.setDescripcion(descripcion);
        req.setVersion(1);
        req.setEstado("BORRADOR");
        req.setNodos(List.of(n1, n2));
        req.setConexiones(List.of(c1));
        return req;
    }
}
