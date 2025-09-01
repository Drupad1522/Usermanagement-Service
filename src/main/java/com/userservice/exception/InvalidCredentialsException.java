package com.userservice.exception;

public class InvalidCredentialsException extends RuntimeException {
    private final String email;
    private final String errorCode;

    public InvalidCredentialsException(String message, String email, String errorCode) {
        super(message);
        this.email = email;
        this.errorCode = errorCode;
    }

    public String getEmail() {
        return email;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
