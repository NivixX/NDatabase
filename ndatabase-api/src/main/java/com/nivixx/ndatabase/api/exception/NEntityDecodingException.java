package com.nivixx.ndatabase.api.exception;

public class NEntityDecodingException extends NDatabaseException {
    public NEntityDecodingException() {
    }

    public NEntityDecodingException(String message) {
        super(message);
    }

    public NEntityDecodingException(String message, Throwable cause) {
        super(message, cause);
    }

    public NEntityDecodingException(Throwable cause) {
        super(cause);
    }

    public NEntityDecodingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
