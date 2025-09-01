package com.userservice.exception;

public class UserNotFoundException extends RuntimeException {
    private final String identifier;

    public UserNotFoundException(String message, String identifier) {
        super(message);
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
}
