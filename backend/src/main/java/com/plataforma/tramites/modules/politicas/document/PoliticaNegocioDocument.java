package com.plataforma.tramites.modules.politicas.document;

import com.plataforma.tramites.modules.politicas.model.ConexionFlujoEmbeddable;
import com.plataforma.tramites.modules.politicas.model.NodoPoliticaEmbeddable;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Colección {@code politicas_negocio} (script.db): nodos y conexiones embebidos.
 */
@Document(collection = "politicas_negocio")
public class PoliticaNegocioDocument {

    @Id
    private ObjectId id;
    private String nombre;
    private String descripcion;
    private int version;
    private String estado;
    private Instant fechaCreacion;
    /** Fuente canónica del diseño BPMN (XML). */
    private String bpmnXml;
    private List<NodoPoliticaEmbeddable> nodos = new ArrayList<>();
    private List<ConexionFlujoEmbeddable> conexiones = new ArrayList<>();

    /**
     * Control de concurrencia optimista (independiente de {@link #version} de negocio). Se expone en API como
     * {@code lockVersion}; debe enviarse en cada PUT y se incrementa en el servidor al guardar.
     */
    @Version
    private Long lockVersion;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Instant fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getBpmnXml() {
        return bpmnXml;
    }

    public void setBpmnXml(String bpmnXml) {
        this.bpmnXml = bpmnXml;
    }

    public List<NodoPoliticaEmbeddable> getNodos() {
        return nodos;
    }

    public void setNodos(List<NodoPoliticaEmbeddable> nodos) {
        this.nodos = nodos != null ? nodos : new ArrayList<>();
    }

    public List<ConexionFlujoEmbeddable> getConexiones() {
        return conexiones;
    }

    public void setConexiones(List<ConexionFlujoEmbeddable> conexiones) {
        this.conexiones = conexiones != null ? conexiones : new ArrayList<>();
    }

    public Long getLockVersion() {
        return lockVersion;
    }

    public void setLockVersion(Long lockVersion) {
        this.lockVersion = lockVersion;
    }
}
