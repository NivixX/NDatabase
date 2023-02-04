package com.nivixx.ndatabase.api.exception;

public class NEntityEncodingException extends NDatabaseException {
    public NEntityEncodingException() {
    }

    public NEntityEncodingException(String message) {
        super(message);
    }

    public NEntityEncodingException(String message, Throwable cause) {
        super(message, cause);
    }

    public NEntityEncodingException(Throwable cause) {
        super(cause);
    }

    public NEntityEncodingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
