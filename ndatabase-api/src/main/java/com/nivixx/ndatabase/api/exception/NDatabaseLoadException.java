package com.nivixx.ndatabase.api.exception;

public class NDatabaseLoadException extends Exception {
    public NDatabaseLoadException() {
    }

    public NDatabaseLoadException(String message) {
        super(message);
    }

    public NDatabaseLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    public NDatabaseLoadException(Throwable cause) {
        super(cause);
    }

    public NDatabaseLoadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
