// UserController.java
package com.userservice.controller;

import com.userservice.dto.*;
import com.userservice.service.UserService;
import com.userservice.util.RequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private RequestUtil requestUtil;
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        logger.debug("Fetching user with ID: {}", id);
        
        UserResponse userResponse = userService.getUserById(id);
        ApiResponse<UserResponse> response = ApiResponse.success(userResponse);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(@PathVariable String email) {
        logger.debug("Fetching user with email: {}", email);
        
        UserResponse userResponse = userService.getUserByEmail(email);
        ApiResponse<UserResponse> response = ApiResponse.success(userResponse);
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request,
            HttpServletRequest httpRequest) {
        
        logger.info("Updating profile for user ID: {}", id);
        
        String ipAddress = requestUtil.getClientIpAddress(httpRequest);
        UserResponse userResponse = userService.updateUserProfile(id, request, ipAddress);
        
        ApiResponse<UserResponse> response = ApiResponse.success(
                "Profile updated successfully", userResponse);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<ApiResponse<String>> assignRole(
            @PathVariable Long userId,
            @PathVariable Long roleId,
            @RequestParam(required = false) Long assignedBy,
            HttpServletRequest httpRequest) {
        
        logger.info("Assigning role {} to user {}", roleId, userId);
        
        String ipAddress = requestUtil.getClientIpAddress(httpRequest);
        userService.assignRoleToUser(userId, roleId, assignedBy, ipAddress);
        
        ApiResponse<String> response = ApiResponse.success("Role assigned successfully", null);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<ApiResponse<String>> removeRole(
            @PathVariable Long userId,
            @PathVariable Long roleId,
            HttpServletRequest httpRequest) {
        
        logger.info("Removing role {} from user {}", roleId, userId);
        
        String ipAddress = requestUtil.getClientIpAddress(httpRequest);
        userService.removeRoleFromUser(userId, roleId, ipAddress);
        
        ApiResponse<String> response = ApiResponse.success("Role removed successfully", null);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        logger.debug("Searching users with keyword: {}", keyword);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<UserResponse> users = userService.searchUsers(keyword, pageable);
        ApiResponse<Page<UserResponse>> response = ApiResponse.success(users);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/role/{roleName}")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByRole(
            @PathVariable String roleName) {
        
        logger.debug("Fetching users with role: {}", roleName);
        
        List<UserResponse> users = userService.getUsersByRole(roleName);
        ApiResponse<List<UserResponse>> response = ApiResponse.success(users);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<UserStatsResponse>> getUserStats() {
        logger.debug("Fetching user statistics");
        
        UserStatsResponse stats = userService.getUserStats();
        ApiResponse<UserStatsResponse> response = ApiResponse.success(stats);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getActiveUsers() {
        logger.debug("Fetching active users");
        
        List<UserResponse> activeUsers = userService.getActiveUsers();
        ApiResponse<List<UserResponse>> response = ApiResponse.success(
                "Active users retrieved", activeUsers);
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{userId}/suspend")
    public ResponseEntity<ApiResponse<String>> suspendUser(
            @PathVariable Long userId,
            HttpServletRequest httpRequest) {
        
        logger.info("Suspending user: {}", userId);
        
        String ipAddress = requestUtil.getClientIpAddress(httpRequest);
        userService.suspendUser(userId, ipAddress);
        
        ApiResponse<String> response = ApiResponse.success("User suspended successfully", null);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{userId}/reactivate")
    public ResponseEntity<ApiResponse<String>> reactivateUser(
            @PathVariable Long userId,
            HttpServletRequest httpRequest) {
        
        logger.info("Reactivating user: {}", userId);
        
        String ipAddress = requestUtil.getClientIpAddress(httpRequest);
        userService.reactivateUser(userId, ipAddress);
        
        ApiResponse<String> response = ApiResponse.success("User reactivated successfully", null);
        return ResponseEntity.ok(response);
    }
//
//    @PutMapping("/{userId}/password")
//    public ResponseEntity<ApiResponse<String>> changePassword(
//            @PathVariable Long userId,
//            @RequestBody PasswordChangeRequest request,
//            HttpServletRequest httpRequest) {
//
//        logger.info("Password change request for user: {}", userId);
//
//        String ipAddress = requestUtil.getClientIpAddress(httpRequest);
//        userService.changeUserPassword(userId, request.getCurrentPassword(),
//                                     request.getNewPassword(), ipAddress);
//
//        ApiResponse<String> response = ApiResponse.success("Password changed successfully", null);
//        return ResponseEntity.ok(response);
//    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        logger.debug("Fetching all users with pagination");
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Using empty keyword to get all users
        Page<UserResponse> users = userService.searchUsers("", pageable);
        ApiResponse<Page<UserResponse>> response = ApiResponse.success(users);
        
        return ResponseEntity.ok(response);
    }
}