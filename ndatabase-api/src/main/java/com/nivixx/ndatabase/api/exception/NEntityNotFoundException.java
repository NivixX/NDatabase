package com.nivixx.ndatabase.api.exception;

public class NEntityNotFoundException extends NDatabaseException {
    public NEntityNotFoundException() {
    }

    public NEntityNotFoundException(String message) {
        super(message);
    }

    public NEntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NEntityNotFoundException(Throwable cause) {
        super(cause);
    }

    public NEntityNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
