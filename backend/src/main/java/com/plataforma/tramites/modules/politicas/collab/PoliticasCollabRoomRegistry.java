package com.plataforma.tramites.modules.politicas.collab;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Salas en memoria por {@code politicaId} y contador de revisión monotónico por sala (broadcast ordenado lógico).
 */
@Component
public class PoliticasCollabRoomRegistry {

    private final ConcurrentHashMap<String, ConcurrentHashMap<String, WebSocketSession>> rooms = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> revisionByRoom = new ConcurrentHashMap<>();

    public void addToRoom(String politicaId, WebSocketSession session) {
        rooms.computeIfAbsent(politicaId, k -> new ConcurrentHashMap<>()).put(session.getId(), session);
    }

    public void removeFromRoom(String politicaId, WebSocketSession session) {
        ConcurrentHashMap<String, WebSocketSession> room = rooms.get(politicaId);
        if (room == null) {
            return;
        }
        room.remove(session.getId());
        if (room.isEmpty()) {
            rooms.remove(politicaId, room);
            revisionByRoom.remove(politicaId);
        }
    }

    /** Elimina la sesión de cualquier sala donde estuviera. */
    public void removeSessionEverywhere(WebSocketSession session) {
        String pid = (String) session.getAttributes().get(PoliticasCollabAttributes.POLITICA_ID);
        if (pid != null) {
            removeFromRoom(pid, session);
        }
    }

    public long nextRevision(String politicaId) {
        return revisionByRoom.computeIfAbsent(politicaId, k -> new AtomicLong()).incrementAndGet();
    }

    public void broadcastJson(String politicaId, WebSocketSession exclude, String jsonPayload) {
        ConcurrentHashMap<String, WebSocketSession> room = rooms.get(politicaId);
        if (room == null || room.isEmpty()) {
            return;
        }
        TextMessage msg = new TextMessage(jsonPayload);
        for (WebSocketSession s : room.values()) {
            if (exclude != null && exclude.getId().equals(s.getId())) {
                continue;
            }
            if (s.isOpen()) {
                try {
                    synchronized (s) {
                        s.sendMessage(msg);
                    }
                } catch (IOException ignored) {
                    // sesión rota; el cierre la limpiará
                }
            }
        }
    }

    public List<PeerInfo> listPeers(String politicaId) {
        ConcurrentHashMap<String, WebSocketSession> room = rooms.get(politicaId);
        if (room == null) {
            return List.of();
        }
        List<PeerInfo> out = new ArrayList<>();
        for (Map.Entry<String, WebSocketSession> e : room.entrySet()) {
            WebSocketSession s = e.getValue();
            String name = (String) s.getAttributes().getOrDefault(PoliticasCollabAttributes.DISPLAY_NAME, "");
            out.add(new PeerInfo(e.getKey(), name));
        }
        return out;
    }

    public record PeerInfo(String sessionId, String displayName) {}
}
