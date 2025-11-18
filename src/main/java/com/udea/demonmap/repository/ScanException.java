package com.udea.demonmap.repository;

/**
 * Excepción personalizada para errores de escaneo.
 * Proporciona contexto específico sobre problemas durante el escaneo de red.
 */
public class ScanException extends Exception {
    
    public ScanException(String message) {
        super(message);
    }
    
    public ScanException(String message, Throwable cause) {
        super(message, cause);
    }
}
