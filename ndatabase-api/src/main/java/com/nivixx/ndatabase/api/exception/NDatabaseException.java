package com.nivixx.ndatabase.api.exception;

public class NDatabaseException extends RuntimeException {
    public NDatabaseException() {
    }

    public NDatabaseException(String message) {
        super(message);
    }

    public NDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public NDatabaseException(Throwable cause) {
        super(cause);
    }

    public NDatabaseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
