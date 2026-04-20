package com.plataforma.tramites.modules.seguimiento.exception;

public class SeguimientoModuleException extends RuntimeException {

    public SeguimientoModuleException(String message) {
        super(message);
    }

    public SeguimientoModuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
