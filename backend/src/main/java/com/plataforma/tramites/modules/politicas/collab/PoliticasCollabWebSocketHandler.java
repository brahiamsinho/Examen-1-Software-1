package com.plataforma.tramites.modules.politicas.collab;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.Set;

/**
 * Colaboración en tiempo real del lienzo X6: salas por política, presencia y broadcast de cambios del grafo.
 */
@Component
public class PoliticasCollabWebSocketHandler extends TextWebSocketHandler {

    private static final Set<String> ROLES_ALLOWED = Set.of("DISENADOR_POLITICAS", "ADMINISTRADOR");

    private final ObjectMapper objectMapper;
    private final PoliticasCollabRoomRegistry rooms;

    public PoliticasCollabWebSocketHandler(ObjectMapper objectMapper, PoliticasCollabRoomRegistry rooms) {
        this.objectMapper = objectMapper;
        this.rooms = rooms;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String rol = (String) session.getAttributes().get(PoliticasCollabAttributes.ROL);
        if (rol == null || !ROLES_ALLOWED.contains(rol)) {
            session.close(CloseStatus.POLICY_VIOLATION.withReason("Rol no autorizado para colaboración en políticas."));
            return;
        }
        ObjectNode ack = objectMapper.createObjectNode();
        ack.put("type", "SESSION_ACK");
        ack.put("sessionId", session.getId());
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(ack)));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonNode root;
        try {
            root = objectMapper.readTree(message.getPayload());
        } catch (Exception e) {
            sendError(session, "JSON inválido.");
            return;
        }
        String type = text(root, "type");
        if (type == null || type.isBlank()) {
            sendError(session, "Falta type.");
            return;
        }
        switch (type) {
            case "JOIN" -> handleJoin(session, root);
            case "LEAVE" -> handleLeave(session);
            case "PRESENCE" -> handlePresence(session, root);
            case "GRAPH_UPDATE" -> handleGraphUpdate(session, root);
            default -> sendError(session, "type desconocido: " + type);
        }
    }

    private void handleJoin(WebSocketSession session, JsonNode root) throws Exception {
        String politicaId = text(root, "politicaId");
        if (!isValidPoliticaId(politicaId)) {
            sendError(session, "politicaId inválido.");
            return;
        }
        rooms.removeSessionEverywhere(session);
        session.getAttributes().put(PoliticasCollabAttributes.POLITICA_ID, politicaId);
        rooms.addToRoom(politicaId, session);
        broadcastPresenceSync(politicaId);
    }

    private void handleLeave(WebSocketSession session) throws Exception {
        String politicaId = (String) session.getAttributes().get(PoliticasCollabAttributes.POLITICA_ID);
        if (politicaId != null) {
            rooms.removeFromRoom(politicaId, session);
            session.getAttributes().remove(PoliticasCollabAttributes.POLITICA_ID);
            broadcastPresenceSync(politicaId);
        }
    }

    private void handlePresence(WebSocketSession session, JsonNode root) throws Exception {
        String politicaId = (String) session.getAttributes().get(PoliticasCollabAttributes.POLITICA_ID);
        if (politicaId == null) {
            sendError(session, "Unite a una sala con JOIN primero.");
            return;
        }
        String dn = text(root, "displayName");
        if (dn != null && !dn.isBlank()) {
            session.getAttributes().put(PoliticasCollabAttributes.DISPLAY_NAME, dn.trim());
        }
        broadcastPresenceSync(politicaId);
    }

    private void handleGraphUpdate(WebSocketSession session, JsonNode root) throws Exception {
        String politicaId = (String) session.getAttributes().get(PoliticasCollabAttributes.POLITICA_ID);
        if (politicaId == null) {
            sendError(session, "Unite a una sala con JOIN primero.");
            return;
        }
        JsonNode cells = root.get("cells");
        if (cells == null || !cells.isArray()) {
            sendError(session, "GRAPH_UPDATE requiere cells (array).");
            return;
        }
        long revision = rooms.nextRevision(politicaId);
        ObjectNode out = objectMapper.createObjectNode();
        out.put("type", "GRAPH_UPDATE");
        out.put("politicaId", politicaId);
        out.put("revision", revision);
        out.put("sourceSessionId", session.getId());
        out.set("cells", cells);
        rooms.broadcastJson(politicaId, session, objectMapper.writeValueAsString(out));
    }

    private void broadcastPresenceSync(String politicaId) throws Exception {
        List<PoliticasCollabRoomRegistry.PeerInfo> peers = rooms.listPeers(politicaId);
        ObjectNode out = objectMapper.createObjectNode();
        out.put("type", "PRESENCE_SYNC");
        out.put("politicaId", politicaId);
        ArrayNode arr = out.putArray("peers");
        for (PoliticasCollabRoomRegistry.PeerInfo p : peers) {
            ObjectNode o = arr.addObject();
            o.put("sessionId", p.sessionId());
            o.put("displayName", p.displayName() != null ? p.displayName() : "");
        }
        rooms.broadcastJson(politicaId, null, objectMapper.writeValueAsString(out));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String politicaId = (String) session.getAttributes().get(PoliticasCollabAttributes.POLITICA_ID);
        rooms.removeSessionEverywhere(session);
        if (politicaId != null) {
            try {
                broadcastPresenceSync(politicaId);
            } catch (Exception ignored) {
                // cierre en curso
            }
        }
    }

    private void sendError(WebSocketSession session, String msg) throws Exception {
        ObjectNode o = objectMapper.createObjectNode();
        o.put("type", "ERROR");
        o.put("message", msg);
        if (session.isOpen()) {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(o)));
        }
    }

    private static String text(JsonNode n, String field) {
        JsonNode v = n.get(field);
        return v != null && v.isTextual() ? v.asText() : null;
    }

    private static boolean isValidPoliticaId(String id) {
        if (id == null || id.isBlank()) {
            return false;
        }
        try {
            new ObjectId(id.trim());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
