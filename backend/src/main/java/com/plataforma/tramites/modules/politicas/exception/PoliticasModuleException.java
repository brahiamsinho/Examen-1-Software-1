package com.plataforma.tramites.modules.politicas.exception;

public class PoliticasModuleException extends RuntimeException {

    public PoliticasModuleException(String message) {
        super(message);
    }

    public PoliticasModuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
