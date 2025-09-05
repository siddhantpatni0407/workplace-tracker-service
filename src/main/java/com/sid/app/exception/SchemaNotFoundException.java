package com.sid.app.exception;

/**
 * @author Siddhant Patni
 */
public class SchemaNotFoundException extends RuntimeException {

    public SchemaNotFoundException(String message) {
        super(message);
    }

}