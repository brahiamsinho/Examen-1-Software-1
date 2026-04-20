package com.plataforma.tramites.modules.documentos.exception;

public class DocumentosModuleException extends RuntimeException {

    public DocumentosModuleException(String message) {
        super(message);
    }

    public DocumentosModuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
