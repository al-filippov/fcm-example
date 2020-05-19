package org.example;

public class FcmException extends RuntimeException {
    public FcmException() {
    }

    public FcmException(String message) {
        super(message);
    }

    public FcmException(String message, Throwable cause) {
        super(message, cause);
    }

    public FcmException(Throwable cause) {
        super(cause);
    }

    public FcmException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
