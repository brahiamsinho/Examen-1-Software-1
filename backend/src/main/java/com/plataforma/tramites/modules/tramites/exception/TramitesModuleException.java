package com.plataforma.tramites.modules.tramites.exception;

public class TramitesModuleException extends RuntimeException {

    public TramitesModuleException(String message) {
        super(message);
    }

    public TramitesModuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
