package com.nivixx.ndatabase.api.exception;

public class DatabaseCreationException extends NDatabaseException {
    public DatabaseCreationException() {
    }

    public DatabaseCreationException(String message) {
        super(message);
    }

    public DatabaseCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseCreationException(Throwable cause) {
        super(cause);
    }

    public DatabaseCreationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
