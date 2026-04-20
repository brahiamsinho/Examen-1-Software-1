package com.plataforma.tramites.modules.admin.service;

import com.plataforma.tramites.modules.admin.document.BitacoraDocument;
import com.plataforma.tramites.modules.admin.dto.BitacoraResponse;
import com.plataforma.tramites.modules.admin.dto.PagedResponse;
import com.plataforma.tramites.modules.admin.repository.BitacoraRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AdminBitacoraService {

    private final BitacoraRepository bitacoraRepository;

    public AdminBitacoraService(BitacoraRepository bitacoraRepository) {
        this.bitacoraRepository = bitacoraRepository;
    }

    public PagedResponse<BitacoraResponse> listar(Pageable pageable) {
        Page<BitacoraDocument> page = bitacoraRepository.findAllByOrderByFechaDesc(pageable);
        var content = page.getContent().stream().map(AdminBitacoraService::toResponse).toList();
        return new PagedResponse<>(
                content,
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize());
    }

    private static BitacoraResponse toResponse(BitacoraDocument b) {
        BitacoraResponse r = new BitacoraResponse();
        r.setId(b.getId().toHexString());
        r.setFecha(b.getFecha());
        r.setActorUsuarioId(b.getActorUsuarioId());
        r.setActorCorreo(b.getActorCorreo());
        r.setAccion(b.getAccion());
        r.setEntidad(b.getEntidad());
        r.setDetalle(b.getDetalle());
        return r;
    }
}
