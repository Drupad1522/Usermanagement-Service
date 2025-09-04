// HealthController.java
package com.userservice.controller;

import com.userservice.dto.ApiResponse;
import com.userservice.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/health")
@CrossOrigin(origins = "*", maxAge = 3600)
public class HealthController {
    
    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);
    
    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private AuditService auditService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        logger.debug("Health check requested");
        
        Map<String, Object> healthStatus = new HashMap<>();
        healthStatus.put("status", "UP");
        healthStatus.put("timestamp", LocalDateTime.now());
        healthStatus.put("service", "user-management-service");
        healthStatus.put("version", "1.0.0");
        
        // Check database connectivity
        try (Connection connection = dataSource.getConnection()) {
            healthStatus.put("database", "Connected");
            healthStatus.put("databaseUrl", connection.getMetaData().getURL());
            healthStatus.put("databaseProductName", connection.getMetaData().getDatabaseProductName());
        } catch (Exception e) {
            logger.error("Database connection failed", e);
            healthStatus.put("database", "Disconnected");
            healthStatus.put("databaseError", e.getMessage());
        }
        
        ApiResponse<Map<String, Object>> response = ApiResponse.success(healthStatus);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getServiceInfo() {
        logger.debug("Service info requested");
        
        Map<String, Object> info = new HashMap<>();
        info.put("serviceName", "User Management Service");
        info.put("description", "Microservice for user authentication and management");
        info.put("version", "1.0.0");
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("springBootVersion", "3.2.5");
        info.put("buildTime", LocalDateTime.now());
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("POST /auth/register", "Register new user");
        endpoints.put("POST /auth/login", "User login");
        endpoints.put("POST /auth/logout", "User logout");
        endpoints.put("POST /auth/refresh", "Refresh JWT token");
        endpoints.put("GET /auth/me", "Get current user info");
        endpoints.put("GET /users/{id}", "Get user by ID");
        endpoints.put("PUT /users/{id}/profile", "Update user profile");
        endpoints.put("GET /users/search", "Search users");
        endpoints.put("GET /roles", "Get all roles");
        endpoints.put("POST /roles", "Create new role");
        endpoints.put("GET /health", "Health check");
        
        info.put("availableEndpoints", endpoints);
        
        ApiResponse<Map<String, Object>> response = ApiResponse.success(info);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemStats() {
        logger.debug("System stats requested");
        
        Map<String, Object> stats = new HashMap<>();
        
        // System information
        Runtime runtime = Runtime.getRuntime();
        stats.put("totalMemory", runtime.totalMemory());
        stats.put("freeMemory", runtime.freeMemory());
        stats.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
        stats.put("maxMemory", runtime.maxMemory());
        stats.put("availableProcessors", runtime.availableProcessors());
        
        // Application stats
        try {
            Long activeUsersLastHour = auditService.getActiveUsersCount(LocalDateTime.now().minusHours(1));
            stats.put("activeUsersLastHour", activeUsersLastHour);
            
            List<Object[]> actionStats = auditService.getActionStatistics(LocalDateTime.now().minusDays(1));
            stats.put("actionStatsLast24Hours", actionStats);
        } catch (Exception e) {
            logger.error("Error getting audit stats", e);
            stats.put("auditStatsError", e.getMessage());
        }
        
        ApiResponse<Map<String, Object>> response = ApiResponse.success(stats);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/ready")
    public ResponseEntity<ApiResponse<Map<String, Object>>> readinessCheck() {
        Map<String, Object> readiness = new HashMap<>();
        readiness.put("status", "READY");
        readiness.put("timestamp", LocalDateTime.now());
        
        boolean isReady = true;
        
        // Check database
        try (Connection connection = dataSource.getConnection()) {
            readiness.put("database", "READY");
        } catch (Exception e) {
            readiness.put("database", "NOT_READY");
            readiness.put("databaseError", e.getMessage());
            isReady = false;
        }
        
        readiness.put("overallStatus", isReady ? "READY" : "NOT_READY");
        
        ApiResponse<Map<String, Object>> response = ApiResponse.success(readiness);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/live")
    public ResponseEntity<ApiResponse<Map<String, Object>>> livenessCheck() {
        Map<String, Object> liveness = new HashMap<>();
        liveness.put("status", "ALIVE");
        liveness.put("timestamp", LocalDateTime.now());
        liveness.put("uptime", System.currentTimeMillis());
        
        ApiResponse<Map<String, Object>> response = ApiResponse.success(liveness);
        return ResponseEntity.ok(response);
    }
}