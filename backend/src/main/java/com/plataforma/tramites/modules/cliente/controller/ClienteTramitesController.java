package com.plataforma.tramites.modules.cliente.controller;

import com.plataforma.tramites.modules.cliente.dto.ClienteInicioTramiteJsonRequest;
import com.plataforma.tramites.modules.cliente.dto.ClienteInicioTramiteResponse;
import com.plataforma.tramites.modules.cliente.service.ClienteTramitesService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/cliente/tramites")
public class ClienteTramitesController {

    private final ClienteTramitesService clienteTramitesService;

    public ClienteTramitesController(ClienteTramitesService clienteTramitesService) {
        this.clienteTramitesService = clienteTramitesService;
    }

    @PostMapping(value = "/inicio-con-documento", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteInicioTramiteResponse inicioJson(@Valid @RequestBody ClienteInicioTramiteJsonRequest body) {
        String clienteId = SecurityContextHolder.getContext().getAuthentication().getName();
        return clienteTramitesService.iniciarConDocumentoJson(clienteId, body);
    }

    @PostMapping(value = "/inicio-con-documento", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteInicioTramiteResponse inicioMultipart(
            @RequestPart(value = "asunto", required = false) String asunto,
            @RequestPart(value = "descripcion", required = false) String descripcion,
            @RequestPart(value = "prioridad", required = false) String prioridad,
            @RequestPart("archivo") MultipartFile archivo) {
        String clienteId = SecurityContextHolder.getContext().getAuthentication().getName();
        return clienteTramitesService.iniciarConDocumentoMultipart(clienteId, asunto, descripcion, prioridad, archivo);
    }
}
