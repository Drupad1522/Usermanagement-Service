package com.userservice.exception;

public class AccountLockedException extends RuntimeException {
    private final String status;

    public AccountLockedException(String message, String status) {
        super(message);
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
