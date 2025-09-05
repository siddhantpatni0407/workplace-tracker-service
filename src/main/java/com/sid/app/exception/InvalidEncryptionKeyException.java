package com.sid.app.exception;

public class InvalidEncryptionKeyException extends RuntimeException {
    public InvalidEncryptionKeyException(String message, Throwable cause) {
        super(message, cause);
    }
}