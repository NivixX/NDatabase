package com.nivixx.ndatabase.api.exception;

public class NEntityNotFound extends NDatabaseException {
    public NEntityNotFound() {
    }

    public NEntityNotFound(String message) {
        super(message);
    }

    public NEntityNotFound(String message, Throwable cause) {
        super(message, cause);
    }

    public NEntityNotFound(Throwable cause) {
        super(cause);
    }

    public NEntityNotFound(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
