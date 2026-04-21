package com.plataforma.tramites.modules.analitica.service;

import com.plataforma.tramites.modules.analitica.document.AnalisisRendimientoDocument;
import com.plataforma.tramites.modules.analitica.document.RecomendacionPoliticaDocument;
import com.plataforma.tramites.modules.analitica.dto.AnalisisRendimientoCreateRequest;
import com.plataforma.tramites.modules.analitica.dto.AnalisisRendimientoResponse;
import com.plataforma.tramites.modules.analitica.dto.RecomendacionPoliticaCreateRequest;
import com.plataforma.tramites.modules.analitica.dto.RecomendacionPoliticaResponse;
import com.plataforma.tramites.modules.analitica.repository.AnalisisRendimientoRepository;
import com.plataforma.tramites.modules.analitica.repository.RecomendacionPoliticaRepository;
import com.plataforma.tramites.shared.dto.ModuleStatusResponse;
import com.plataforma.tramites.shared.exception.ApiException;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Service
public class AnaliticaService {

    private final AnalisisRendimientoRepository analisisRendimientoRepository;
    private final RecomendacionPoliticaRepository recomendacionPoliticaRepository;

    public AnaliticaService(
            AnalisisRendimientoRepository analisisRendimientoRepository,
            RecomendacionPoliticaRepository recomendacionPoliticaRepository) {
        this.analisisRendimientoRepository = analisisRendimientoRepository;
        this.recomendacionPoliticaRepository = recomendacionPoliticaRepository;
    }

    public ModuleStatusResponse moduleStatus() {
        return new ModuleStatusResponse("analitica", "bootstrap");
    }

    public List<AnalisisRendimientoResponse> listarAnalisisPorPolitica(String politicaId) {
        ObjectId pid = parseOid(politicaId, "politicaId");
        return analisisRendimientoRepository.findByPoliticaIdOrderByFechaAnalisisDesc(pid).stream()
                .map(this::toAnalisisResponse)
                .toList();
    }

    public AnalisisRendimientoResponse crearAnalisis(AnalisisRendimientoCreateRequest body) {
        AnalisisRendimientoDocument d = new AnalisisRendimientoDocument();
        d.setPoliticaId(parseOid(body.getPoliticaId(), "politicaId"));
        if (body.getTramiteIds() != null) {
            d.setTramiteIds(body.getTramiteIds().stream().map(s -> parseOid(s, "tramiteId en lista")).toList());
        } else {
            d.setTramiteIds(Collections.emptyList());
        }
        d.setFechaAnalisis(Instant.now());
        d.setTiempoPromedio(body.getTiempoPromedio());
        d.setCuelloBotellaDetectado(body.getCuelloBotellaDetectado().trim());
        d.setObservacion(body.getObservacion());
        return toAnalisisResponse(analisisRendimientoRepository.save(d));
    }

    public List<RecomendacionPoliticaResponse> listarRecomendacionesPorPolitica(String politicaId) {
        ObjectId pid = parseOid(politicaId, "politicaId");
        return recomendacionPoliticaRepository.findByPoliticaIdOrderByFechaGeneracionDesc(pid).stream()
                .map(this::toRecomResponse)
                .toList();
    }

    public RecomendacionPoliticaResponse crearRecomendacion(RecomendacionPoliticaCreateRequest body) {
        RecomendacionPoliticaDocument r = new RecomendacionPoliticaDocument();
        r.setPoliticaId(parseOid(body.getPoliticaId(), "politicaId"));
        r.setUsuarioId(parseOid(body.getUsuarioId(), "usuarioId"));
        r.setFechaGeneracion(Instant.now());
        r.setPoliticaSugerida(body.getPoliticaSugerida().trim());
        r.setProbabilidadExito(body.getProbabilidadExito());
        r.setTiempoEstimado(body.getTiempoEstimado());
        r.setObservacion(body.getObservacion());
        return toRecomResponse(recomendacionPoliticaRepository.save(r));
    }

    private AnalisisRendimientoResponse toAnalisisResponse(AnalisisRendimientoDocument d) {
        List<String> tids =
                d.getTramiteIds() == null ? List.of() : d.getTramiteIds().stream().map(ObjectId::toHexString).toList();
        return new AnalisisRendimientoResponse(
                d.getId().toHexString(),
                d.getPoliticaId().toHexString(),
                tids,
                d.getFechaAnalisis(),
                d.getTiempoPromedio() != null ? d.getTiempoPromedio() : 0d,
                d.getCuelloBotellaDetectado(),
                d.getObservacion());
    }

    private RecomendacionPoliticaResponse toRecomResponse(RecomendacionPoliticaDocument r) {
        return new RecomendacionPoliticaResponse(
                r.getId().toHexString(),
                r.getPoliticaId().toHexString(),
                r.getUsuarioId().toHexString(),
                r.getFechaGeneracion(),
                r.getPoliticaSugerida(),
                r.getProbabilidadExito(),
                r.getTiempoEstimado(),
                r.getObservacion());
    }

    private static ObjectId parseOid(String hex, String ctx) {
        try {
            return new ObjectId(hex);
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "ObjectId inválido: " + ctx);
        }
    }
}
