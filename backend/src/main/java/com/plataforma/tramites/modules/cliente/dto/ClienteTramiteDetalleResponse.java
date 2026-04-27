package com.plataforma.tramites.modules.cliente.dto;

import com.plataforma.tramites.modules.tramites.dto.RecorridoTramiteResponse;
import com.plataforma.tramites.modules.tramites.dto.TramiteResponse;

import java.util.List;

/** Estado del expediente visible para el portal cliente (trámite + historial de recorridos). */
public record ClienteTramiteDetalleResponse(TramiteResponse tramite, List<RecorridoTramiteResponse> recorridos) {}
