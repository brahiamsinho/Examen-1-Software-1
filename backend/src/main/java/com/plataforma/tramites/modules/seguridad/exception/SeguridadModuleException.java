package com.plataforma.tramites.modules.seguridad.exception;

public class SeguridadModuleException extends RuntimeException {

    public SeguridadModuleException(String message) {
        super(message);
    }

    public SeguridadModuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
