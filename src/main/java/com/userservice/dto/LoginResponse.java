package com.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data                   // generates getters, setters, toString, equals, hashCode
@NoArgsConstructor      // generates no-arg constructor
@AllArgsConstructor     // generates all-args constructor
@Builder                // enables builder pattern
public class LoginResponse {
    
    private Long userId;
    private String username;
    private String email;
    private String token;
    private String refreshToken;
    private Long expiresIn;
    private List<String> roles;
    private List<String> permissions;
}
