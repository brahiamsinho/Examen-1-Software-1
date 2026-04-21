package com.plataforma.tramites.modules.seguridad.service;

import com.plataforma.tramites.modules.seguridad.document.AreaDocument;
import com.plataforma.tramites.modules.seguridad.dto.AreaResponse;
import com.plataforma.tramites.modules.seguridad.dto.AreaUpsertRequest;
import com.plataforma.tramites.modules.seguridad.repository.AreaRepository;
import com.plataforma.tramites.shared.exception.ApiException;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AreaCatalogoService {

    private final AreaRepository areaRepository;

    public AreaCatalogoService(AreaRepository areaRepository) {
        this.areaRepository = areaRepository;
    }

    public List<AreaResponse> listar() {
        return areaRepository.findAll().stream().map(this::toResponse).toList();
    }

    public AreaResponse obtener(String id) {
        return toResponse(buscar(id));
    }

    public AreaResponse crear(AreaUpsertRequest body) {
        if (areaRepository.findByNombreIgnoreCase(body.getNombre().trim()).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe un área con ese nombre.");
        }
        AreaDocument a = new AreaDocument();
        a.setNombre(body.getNombre().trim());
        a.setDescripcion(body.getDescripcion().trim());
        a.setEstado(body.isEstado());
        return toResponse(areaRepository.save(a));
    }

    public AreaResponse actualizar(String id, AreaUpsertRequest body) {
        AreaDocument a = buscar(id);
        areaRepository
                .findByNombreIgnoreCase(body.getNombre().trim())
                .filter(other -> !other.getId().equals(a.getId()))
                .ifPresent(x -> {
                    throw new ApiException(HttpStatus.CONFLICT, "Ya existe otra área con ese nombre.");
                });
        a.setNombre(body.getNombre().trim());
        a.setDescripcion(body.getDescripcion().trim());
        a.setEstado(body.isEstado());
        return toResponse(areaRepository.save(a));
    }

    public void eliminar(String id) {
        areaRepository.deleteById(parseId(id));
    }

    private AreaDocument buscar(String id) {
        return areaRepository
                .findById(parseId(id))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Área no encontrada."));
    }

    private AreaResponse toResponse(AreaDocument a) {
        return new AreaResponse(
                a.getId().toHexString(), a.getNombre(), a.getDescripcion(), a.isEstado());
    }

    private static ObjectId parseId(String id) {
        try {
            return new ObjectId(id);
        } catch (IllegalArgumentException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Id de área inválido.");
        }
    }
}
