package com.plataforma.tramites.modules.cliente.service;

import com.plataforma.tramites.modules.cliente.dto.ClienteInicioTramiteJsonRequest;
import com.plataforma.tramites.modules.cliente.dto.ClienteInicioTramiteResponse;
import com.plataforma.tramites.modules.cliente.dto.ClienteTramiteDetalleResponse;
import com.plataforma.tramites.modules.tramites.dto.TramiteResponse;
import com.plataforma.tramites.modules.documentos.dto.DocumentoTramiteCreateRequest;
import com.plataforma.tramites.modules.documentos.dto.DocumentoTramiteDto;
import com.plataforma.tramites.modules.documentos.service.DocumentosService;
import com.plataforma.tramites.modules.tramites.dto.RecorridoTramiteRequest;
import com.plataforma.tramites.modules.tramites.dto.TramiteResponse;
import com.plataforma.tramites.modules.tramites.service.TramitesService;
import com.plataforma.tramites.shared.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class ClienteTramitesService {

    private static final Logger log = LoggerFactory.getLogger(ClienteTramitesService.class);
    private static final String DOC_ESTADO_CARGADO = "CARGADO";
    private static final String RECORRIDO_ESTADO = "ACTIVO";

    private final String intakeNodeId;
    private final String intakeNodeName;
    private final String intakeAreaId;
    private final TramitesService tramitesService;
    private final DocumentosService documentosService;
    private final Path uploadRoot;

    public ClienteTramitesService(
            TramitesService tramitesService,
            DocumentosService documentosService,
            @Value("${app.workflow.intake-node-id:ATENCION_CLIENTE}") String intakeNodeId,
            @Value("${app.workflow.intake-node-name:Atencion al cliente}") String intakeNodeName,
            @Value("${app.workflow.intake-area-id:}") String intakeAreaId,
            @Value("${app.storage.upload-dir:./data/uploads}") String uploadDir) {
        this.intakeNodeId = intakeNodeId.trim();
        this.intakeNodeName = intakeNodeName.trim();
        this.intakeAreaId = intakeAreaId != null ? intakeAreaId.trim() : "";
        this.tramitesService = tramitesService;
        this.documentosService = documentosService;
        this.uploadRoot = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    public ClienteInicioTramiteResponse iniciarConDocumentoJson(String clienteIdHex, ClienteInicioTramiteJsonRequest body) {
        TramiteResponse tramite = crearTramiteIngreso(clienteIdHex, body);
        registrarIngreso(tramite.id(), clienteIdHex);
        DocumentoTramiteDto doc = registrarDocumento(
                tramite.id(),
                intakeNodeId,
                body.getNombreArchivo(),
                body.getTipoArchivo(),
                body.getRutaArchivo().trim());
        return armarRespuesta(tramite, doc.id(), doc.rutaArchivo());
    }

    /** Listado paginado de trámites del cliente autenticado. */
    public Page<TramiteResponse> listarMisTramites(String clienteIdHex, int page, int size) {
        int p = Math.max(0, page);
        int s = Math.min(100, Math.max(1, size));
        return tramitesService.listarPorClienteId(clienteIdHex, PageRequest.of(p, s));
    }

    /** Detalle y recorridos; falla si el trámite no pertenece al cliente. */
    public ClienteTramiteDetalleResponse detalleMiTramite(String clienteIdHex, String tramiteIdHex) {
        TramiteResponse t = tramitesService.obtenerDeCliente(tramiteIdHex, clienteIdHex);
        return new ClienteTramiteDetalleResponse(t, tramitesService.listarRecorridosInterno(tramiteIdHex));
    }

    public ClienteInicioTramiteResponse iniciarConDocumentoMultipart(
            String clienteIdHex,
            String asunto,
            String descripcion,
            String prioridad,
            MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El archivo es obligatorio.");
        }
        ClienteInicioTramiteJsonRequest synthetic = new ClienteInicioTramiteJsonRequest();
        synthetic.setAsunto(asunto);
        synthetic.setDescripcion(descripcion);
        synthetic.setPrioridad(prioridad);
        synthetic.setNombreArchivo(safeFilename(archivo.getOriginalFilename()));
        synthetic.setTipoArchivo(
                archivo.getContentType() != null && !archivo.getContentType().isBlank()
                        ? archivo.getContentType().trim()
                        : "application/octet-stream");
        TramiteResponse tramite = crearTramiteIngreso(clienteIdHex, synthetic);
        registrarIngreso(tramite.id(), clienteIdHex);
        Path destino = guardarArchivoEnDisco(tramite.id(), synthetic.getNombreArchivo(), archivo);
        String rutaRelativa = uploadRoot.relativize(destino).toString().replace('\\', '/');
        DocumentoTramiteDto doc = registrarDocumento(
                tramite.id(), intakeNodeId, synthetic.getNombreArchivo(), synthetic.getTipoArchivo(), rutaRelativa);
        return armarRespuesta(tramite, doc.id(), rutaRelativa);
    }

    private TramiteResponse crearTramiteIngreso(String clienteIdHex, ClienteInicioTramiteJsonRequest body) {
        String prioridad =
                body.getPrioridad() == null || body.getPrioridad().isBlank()
                        ? "MEDIA"
                        : body.getPrioridad().trim();
        String asunto =
                body.getAsunto() == null || body.getAsunto().isBlank()
                        ? "Solicitud con documentación"
                        : body.getAsunto().trim();
        String descripcion =
                body.getDescripcion() == null || body.getDescripcion().isBlank()
                        ? "Ingreso inicial en atencion al cliente."
                        : body.getDescripcion().trim();
        return tramitesService.crearIngresoCliente(clienteIdHex, asunto, descripcion, prioridad, intakeNodeId, intakeAreaId);
    }

    private void registrarIngreso(String tramiteId, String clienteIdHex) {
        RecorridoTramiteRequest rec = new RecorridoTramiteRequest();
        rec.setNodoId(intakeNodeId);
        if (!intakeAreaId.isBlank()) {
            rec.setAreaId(intakeAreaId);
        }
        rec.setUsuarioId(clienteIdHex);
        rec.setEstado(RECORRIDO_ESTADO);
        rec.setObservacion("Ingreso inicial de solicitud en atencion al cliente.");
        tramitesService.registrarRecorrido(tramiteId, rec);
    }

    private DocumentoTramiteDto registrarDocumento(
            String tramiteId, String nodoId, String nombreArchivo, String tipoArchivo, String rutaArchivo) {
        DocumentoTramiteCreateRequest d = new DocumentoTramiteCreateRequest();
        d.setTramiteId(tramiteId);
        d.setNodoId(nodoId);
        d.setNombreArchivo(nombreArchivo);
        d.setTipoArchivo(tipoArchivo);
        d.setRutaArchivo(rutaArchivo);
        d.setEstado(DOC_ESTADO_CARGADO);
        return documentosService.crearDocumento(d);
    }

    private ClienteInicioTramiteResponse armarRespuesta(
            TramiteResponse tramite,
            String documentoId,
            String ruta) {
        ClienteInicioTramiteResponse r = new ClienteInicioTramiteResponse();
        r.setTramiteId(tramite.id());
        r.setCodigoTramite(tramite.codigo());
        r.setDocumentoId(documentoId);
        r.setPoliticaId(tramite.politicaId());
        r.setNodoIngresoId(intakeNodeId);
        r.setNodoIngresoNombre(intakeNodeName);
        r.setRutaArchivoGuardada(ruta);
        return r;
    }

    private Path guardarArchivoEnDisco(String tramiteIdHex, String nombreArchivo, MultipartFile archivo) {
        try {
            Files.createDirectories(uploadRoot);
            Path dir = uploadRoot.resolve(tramiteIdHex).normalize();
            if (!dir.startsWith(uploadRoot)) {
                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Ruta de almacenamiento inválida.");
            }
            Files.createDirectories(dir);
            String unique = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
            Path target = dir.resolve(unique + "_" + nombreArchivo).normalize();
            if (!target.startsWith(uploadRoot)) {
                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Ruta de almacenamiento inválida.");
            }
            try (InputStream in = archivo.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return target;
        } catch (IOException ex) {
            log.warn("No se pudo escribir en {}: {}", uploadRoot, ex.getMessage());
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo guardar el archivo.");
        }
    }

    private static String safeFilename(String original) {
        if (original == null || original.isBlank()) {
            return "documento";
        }
        String n = original.replace('\\', '_').replace('/', '_').replace("..", "_");
        if (n.isBlank()) {
            return "documento";
        }
        return n.length() > 200 ? n.substring(0, 200) : n;
    }
}
