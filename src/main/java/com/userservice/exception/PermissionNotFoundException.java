package com.userservice.exception;

public class PermissionNotFoundException extends RuntimeException {
    private final String field;
    private final Object value;

    public PermissionNotFoundException(String message, String field, Object value) {
        super(message + " - " + field + ": " + value);
        this.field = field;
        this.value = value;
    }

    public String getField() {
        return field;
    }

    public Object getValue() {
        return value;
    }
}
