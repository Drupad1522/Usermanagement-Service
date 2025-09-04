// AuthController.java
package com.userservice.controller;

import com.userservice.dto.*;
import com.userservice.service.AuthService;
import com.userservice.service.UserService;
import com.userservice.util.RequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountLockedException;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private RequestUtil requestUtil;
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> registerUser(
            @Valid @RequestBody UserRegistrationRequest request,
            HttpServletRequest httpRequest) {
        
        logger.info("Registration attempt for email: {}", request.getEmail());
        
        String ipAddress = requestUtil.getClientIpAddress(httpRequest);
        UserResponse userResponse = userService.registerUser(request, ipAddress);
        
        ApiResponse<UserResponse> response = ApiResponse.success(
                "User registered successfully", userResponse);
        
        logger.info("User registered successfully: {}", userResponse.getUsername());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> loginUser(
            @Valid @RequestBody UserLoginRequest request,
            HttpServletRequest httpRequest) throws AccountLockedException {
        
        logger.info("Login attempt for email: {}", request.getEmail());
        
        String ipAddress = requestUtil.getClientIpAddress(httpRequest);
        String userAgent = requestUtil.getUserAgent(httpRequest);
        
        LoginResponse loginResponse = authService.authenticateUser(request, ipAddress, userAgent);
        
        ApiResponse<LoginResponse> response = ApiResponse.success(
                "Login successful", loginResponse);
        
        logger.info("User logged in successfully: {}", loginResponse.getUsername());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest httpRequest) {
        String token = requestUtil.getAuthTokenFromRequest(httpRequest);
        String ipAddress = requestUtil.getClientIpAddress(httpRequest);
        
        if (token != null) {
            authService.logout(token, ipAddress);
            logger.info("User logged out successfully");
        }
        
        ApiResponse<String> response = ApiResponse.success("Logout successful", null);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<String>> logoutAllSessions(
            @RequestParam Long userId,
            HttpServletRequest httpRequest) {
        
        String ipAddress = requestUtil.getClientIpAddress(httpRequest);
        authService.logoutAllSessions(userId, ipAddress);
        
        ApiResponse<String> response = ApiResponse.success("All sessions logged out", null);
        logger.info("All sessions logged out for user: {}", userId);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(
            @RequestParam String refreshToken,
            HttpServletRequest httpRequest) {
        
        String ipAddress = requestUtil.getClientIpAddress(httpRequest);
        LoginResponse loginResponse = authService.refreshToken(refreshToken, ipAddress);
        
        ApiResponse<LoginResponse> response = ApiResponse.success(
                "Token refreshed successfully", loginResponse);
        
        logger.info("Token refreshed for user: {}", loginResponse.getUsername());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(
            @RequestParam String token) {
        
        boolean isValid = authService.validateToken(token);
        ApiResponse<Boolean> response = ApiResponse.success(
                "Token validation result", isValid);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(HttpServletRequest httpRequest) {
        String token = requestUtil.getAuthTokenFromRequest(httpRequest);
        
        if (token == null) {
            ApiResponse<UserResponse> response = ApiResponse.error("No token provided");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        UserResponse userResponse = authService.getCurrentUser(token);
        ApiResponse<UserResponse> response = ApiResponse.success(userResponse);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/sessions/{userId}")
    public ResponseEntity<ApiResponse<Object>> getUserSessions(@PathVariable Long userId) {
        Long sessionCount = authService.getActiveSessionCount(userId);
        
        ApiResponse<Object> response = ApiResponse.success(
                "Active session count retrieved", 
                sessionCount);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/invalidate-session")
    public ResponseEntity<ApiResponse<String>> invalidateSession(
            @RequestParam String token,
            HttpServletRequest httpRequest) {
        
        String ipAddress = requestUtil.getClientIpAddress(httpRequest);
        authService.invalidateUserSession(token, ipAddress);
        
        ApiResponse<String> response = ApiResponse.success("Session invalidated", null);
        return ResponseEntity.ok(response);
    }
}