package com.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data                   // Generates getters, setters, toString, equals, hashCode
@NoArgsConstructor      // Generates no-arg constructor
@AllArgsConstructor     // Generates all-args constructor
@Builder                // Enables builder pattern
public class UserResponse {
    
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String status;
    private List<String> roles;
    private LocalDateTime createdAt;
}
