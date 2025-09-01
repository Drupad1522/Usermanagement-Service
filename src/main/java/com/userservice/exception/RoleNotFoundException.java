package com.userservice.exception;

public class RoleNotFoundException extends RuntimeException {
    private String roleName;

    public RoleNotFoundException(String message) {
        super(message);
    }

    public RoleNotFoundException(String message, String roleName) {
        super(message);
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }
}
