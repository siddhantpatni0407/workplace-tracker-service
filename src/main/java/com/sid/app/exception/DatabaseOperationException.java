package com.sid.app.exception;

/**
 * Custom exception for database operation failures including:
 * - Database creation failures
 * - Connection issues
 * - Other database-related operations
 */

/**
 * @author Siddhant Patni
 */
public class DatabaseOperationException extends RuntimeException {

    /**
     * Constructs a new database operation exception with the specified detail message.
     *
     * @param message the detail message
     */
    public DatabaseOperationException(String message) {
        super(message);
    }

    /**
     * Constructs a new database operation exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public DatabaseOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new database operation exception with the specified cause.
     *
     * @param cause the cause of the exception
     */
    public DatabaseOperationException(Throwable cause) {
        super(cause);
    }

}