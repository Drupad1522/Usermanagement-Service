package com.userservice.exception;

public class DuplicateUserException extends RuntimeException {
    private final String field;
    private final String value;

    public DuplicateUserException(String message, String field, String value) {
        super(message);
        this.field = field;
        this.value = value;
    }

    public String getField() {
        return field;
    }

    public String getValue() {
        return value;
    }
}
