package com.plataforma.tramites.modules.tramites.service;

import com.plataforma.tramites.modules.politicas.document.PoliticaNegocioDocument;
import com.plataforma.tramites.modules.politicas.model.NodoPoliticaEmbeddable;
import com.plataforma.tramites.modules.politicas.repository.PoliticaNegocioRepository;
import com.plataforma.tramites.modules.seguridad.document.UsuarioDocument;
import com.plataforma.tramites.modules.seguridad.repository.UsuarioRepository;
import com.plataforma.tramites.modules.tramites.document.TramiteDocument;
import com.plataforma.tramites.modules.tramites.dto.RecorridoTramiteRequest;
import com.plataforma.tramites.shared.exception.ApiException;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TramiteFlujoAutorizacionServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PoliticaNegocioRepository politicaNegocioRepository;

    @Test
    void assertResponsablePuedeActuarEnNodo_adminBypass() {
        TramiteFlujoAutorizacionService service =
                new TramiteFlujoAutorizacionService(usuarioRepository, politicaNegocioRepository);
        Authentication admin = auth(new ObjectId().toHexString(), TramiteFlujoAutorizacionService.ROL_ADMIN);
        PoliticaNegocioDocument p = politicaNodo("n1", new ObjectId());

        service.assertResponsablePuedeActuarEnNodo(admin, p, "n1");
    }

    @Test
    void assertResponsablePuedeActuarEnNodo_responsableAreaMismatch_forbidden() {
        TramiteFlujoAutorizacionService service =
                new TramiteFlujoAutorizacionService(usuarioRepository, politicaNegocioRepository);
        ObjectId userId = new ObjectId();
        ObjectId userArea = new ObjectId();
        ObjectId nodeArea = new ObjectId();
        Authentication responsable = auth(userId.toHexString(), TramiteFlujoAutorizacionService.ROL_RESPONSABLE);
        UsuarioDocument u = new UsuarioDocument();
        u.setId(userId);
        u.setAreaId(userArea);
        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(u));

        ApiException ex = assertThrows(
                ApiException.class,
                () -> service.assertResponsablePuedeActuarEnNodo(responsable, politicaNodo("n1", nodeArea), "n1"));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatus());
        assertEquals(true, ex.getMessage().contains("Tu área no corresponde"));
    }

    @Test
    void assertPuedeConsultarTramiteStaff_responsableCanSeeByParallelPendingArea() {
        TramiteFlujoAutorizacionService service =
                new TramiteFlujoAutorizacionService(usuarioRepository, politicaNegocioRepository);
        ObjectId userId = new ObjectId();
        ObjectId areaLegal = new ObjectId();
        Authentication responsable = auth(userId.toHexString(), TramiteFlujoAutorizacionService.ROL_RESPONSABLE);

        UsuarioDocument u = new UsuarioDocument();
        u.setId(userId);
        u.setAreaId(areaLegal);
        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(u));

        ObjectId politicaId = new ObjectId();
        TramiteDocument t = new TramiteDocument();
        t.setPoliticaId(politicaId);
        t.setParaleloSplitNodoId("n_split");
        t.setParaleloRamasPendientes(List.of("n_legal", "n_rrhh"));

        PoliticaNegocioDocument p = new PoliticaNegocioDocument();
        p.setId(politicaId);
        p.setNodos(List.of(
                nodo("n_legal", areaLegal),
                nodo("n_rrhh", new ObjectId())));
        when(politicaNegocioRepository.findById(politicaId)).thenReturn(Optional.of(p));

        service.assertPuedeConsultarTramiteStaff(responsable, t);
    }

    @Test
    void assertPuedeRegistrarRecorridoViaApiTramites_responsableSetsAreaFromNodo() {
        TramiteFlujoAutorizacionService service =
                new TramiteFlujoAutorizacionService(usuarioRepository, politicaNegocioRepository);
        ObjectId userId = new ObjectId();
        ObjectId areaLegal = new ObjectId();
        ObjectId politicaId = new ObjectId();
        Authentication responsable = auth(userId.toHexString(), TramiteFlujoAutorizacionService.ROL_RESPONSABLE);

        UsuarioDocument u = new UsuarioDocument();
        u.setId(userId);
        u.setAreaId(areaLegal);
        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(u));

        PoliticaNegocioDocument p = politicaNodo("n_legal", areaLegal);
        p.setId(politicaId);
        when(politicaNegocioRepository.findById(politicaId)).thenReturn(Optional.of(p));

        TramiteDocument t = new TramiteDocument();
        t.setPoliticaId(politicaId);
        RecorridoTramiteRequest body = new RecorridoTramiteRequest();
        body.setNodoId("n_legal");
        body.setEstado("ACTIVO");

        service.assertPuedeRegistrarRecorridoViaApiTramites(responsable, t, body);

        assertEquals(areaLegal.toHexString(), body.getAreaId());
    }

    private static Authentication auth(String userIdHex, String role) {
        return new UsernamePasswordAuthenticationToken(
                userIdHex,
                "n/a",
                List.of(new SimpleGrantedAuthority(role)));
    }

    private static PoliticaNegocioDocument politicaNodo(String idNodo, ObjectId areaId) {
        PoliticaNegocioDocument p = new PoliticaNegocioDocument();
        p.setNodos(List.of(nodo(idNodo, areaId)));
        return p;
    }

    private static NodoPoliticaEmbeddable nodo(String idNodo, ObjectId areaId) {
        NodoPoliticaEmbeddable n = new NodoPoliticaEmbeddable();
        n.setIdNodo(idNodo);
        n.setAreaId(areaId);
        return n;
    }
}
