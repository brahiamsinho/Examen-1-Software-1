package com.plataforma.tramites.modules.politicas.collab;

/**
 * Claves en {@link org.springframework.web.socket.WebSocketSession#getAttributes()} tras el handshake JWT.
 */
public final class PoliticasCollabAttributes {

    public static final String USER_ID = "collab.userId";
    public static final String EMAIL = "collab.email";
    public static final String ROL = "collab.rol";
    public static final String DISPLAY_NAME = "collab.displayName";
    public static final String POLITICA_ID = "collab.politicaId";

    private PoliticasCollabAttributes() {}
}
