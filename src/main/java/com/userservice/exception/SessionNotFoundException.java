package com.userservice.exception;

public class SessionNotFoundException extends RuntimeException {
    private final String token;

    public SessionNotFoundException(String message, String token) {
        super(message);
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
