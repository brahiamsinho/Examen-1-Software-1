package com.plataforma.tramites.modules.politicas.document;

import com.plataforma.tramites.modules.politicas.model.ConexionFlujoEmbeddable;
import com.plataforma.tramites.modules.politicas.model.NodoPoliticaEmbeddable;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Snapshots inmutables de {@link PoliticaNegocioDocument} tras cada guardado exitoso (control de versiones / auditoría).
 */
@Document(collection = "politicas_negocio_revisiones")
@CompoundIndex(name = "uniq_politica_revision", def = "{'politicaId': 1, 'revision': 1}", unique = true)
public class PoliticaNegocioRevisionDocument {

    @Id
    private ObjectId id;
    private ObjectId politicaId;
    private long revision;
    private Instant guardadoEn;
    private String nombre;
    private String descripcion;
    private int versionNegocio;
    private String estado;
    private String bpmnXml;
    private Instant fechaCreacionPolitica;
    private Long lockVersionAlGuardar;
    private List<NodoPoliticaEmbeddable> nodos = new ArrayList<>();
    private List<ConexionFlujoEmbeddable> conexiones = new ArrayList<>();

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public ObjectId getPoliticaId() {
        return politicaId;
    }

    public void setPoliticaId(ObjectId politicaId) {
        this.politicaId = politicaId;
    }

    public long getRevision() {
        return revision;
    }

    public void setRevision(long revision) {
        this.revision = revision;
    }

    public Instant getGuardadoEn() {
        return guardadoEn;
    }

    public void setGuardadoEn(Instant guardadoEn) {
        this.guardadoEn = guardadoEn;
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

    public int getVersionNegocio() {
        return versionNegocio;
    }

    public void setVersionNegocio(int versionNegocio) {
        this.versionNegocio = versionNegocio;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getBpmnXml() {
        return bpmnXml;
    }

    public void setBpmnXml(String bpmnXml) {
        this.bpmnXml = bpmnXml;
    }

    public Instant getFechaCreacionPolitica() {
        return fechaCreacionPolitica;
    }

    public void setFechaCreacionPolitica(Instant fechaCreacionPolitica) {
        this.fechaCreacionPolitica = fechaCreacionPolitica;
    }

    public Long getLockVersionAlGuardar() {
        return lockVersionAlGuardar;
    }

    public void setLockVersionAlGuardar(Long lockVersionAlGuardar) {
        this.lockVersionAlGuardar = lockVersionAlGuardar;
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
}
