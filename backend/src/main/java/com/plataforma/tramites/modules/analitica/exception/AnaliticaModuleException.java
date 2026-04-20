package com.plataforma.tramites.modules.analitica.exception;

public class AnaliticaModuleException extends RuntimeException {

    public AnaliticaModuleException(String message) {
        super(message);
    }

    public AnaliticaModuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
