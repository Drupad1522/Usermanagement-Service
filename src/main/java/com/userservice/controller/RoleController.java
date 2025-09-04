// RoleController.java
package com.userservice.controller;

import com.userservice.dto.*;
import com.userservice.entity.Permission;
import com.userservice.service.RoleService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RoleController {
    
    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);
    
    @Autowired
    private RoleService roleService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        logger.debug("Fetching all roles");
        
        List<RoleResponse> roles = roleService.getAllRoles();
        ApiResponse<List<RoleResponse>> response = ApiResponse.success(roles);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(@PathVariable Long id) {
        logger.debug("Fetching role with ID: {}", id);
        
        RoleResponse role = roleService.getRoleById(id);
        ApiResponse<RoleResponse> response = ApiResponse.success(role);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/name/{name}")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleByName(@PathVariable String name) {
        logger.debug("Fetching role with name: {}", name);
        
        RoleResponse role = roleService.getRoleByName(name);
        ApiResponse<RoleResponse> response = ApiResponse.success(role);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAvailableRoles() {
        logger.debug("Fetching available roles for registration");
        
        List<RoleResponse> roles = roleService.getAvailableRoles();
        ApiResponse<List<RoleResponse>> response = ApiResponse.success(
                "Available roles retrieved", roles);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(
            @Valid @RequestBody RoleCreateRequest request) {
        
        logger.info("Creating new role: {}", request.getName());
        
        RoleResponse roleResponse = roleService.createRole(request);
        ApiResponse<RoleResponse> response = ApiResponse.success(
                "Role created successfully", roleResponse);
        
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleCreateRequest request) {
        
        logger.info("Updating role with ID: {}", id);
        
        RoleResponse roleResponse = roleService.updateRole(id, request);
        ApiResponse<RoleResponse> response = ApiResponse.success(
                "Role updated successfully", roleResponse);
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteRole(@PathVariable Long id) {
        logger.info("Deleting role with ID: {}", id);
        
        roleService.deleteRole(id);
        ApiResponse<String> response = ApiResponse.success("Role deleted successfully", null);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/distribution")
    public ResponseEntity<ApiResponse<RoleDistributionResponse>> getRoleDistribution() {
        logger.debug("Fetching role distribution statistics");
        
        RoleDistributionResponse distribution = roleService.getRoleDistribution();
        ApiResponse<RoleDistributionResponse> response = ApiResponse.success(distribution);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{roleId}/permissions/{permissionId}")
    public ResponseEntity<ApiResponse<String>> assignPermissionToRole(
            @PathVariable Long roleId,
            @PathVariable Long permissionId) {
        
        logger.info("Assigning permission {} to role {}", permissionId, roleId);
        
        roleService.assignPermissionToRole(roleId, permissionId);
        ApiResponse<String> response = ApiResponse.success(
                "Permission assigned to role successfully", null);
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    public ResponseEntity<ApiResponse<String>> removePermissionFromRole(
            @PathVariable Long roleId,
            @PathVariable Long permissionId) {
        
        logger.info("Removing permission {} from role {}", permissionId, roleId);
        
        roleService.removePermissionFromRole(roleId, permissionId);
        ApiResponse<String> response = ApiResponse.success(
                "Permission removed from role successfully", null);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{roleId}/permissions")
    public ResponseEntity<ApiResponse<List<Permission>>> getRolePermissions(@PathVariable Long roleId) {
        logger.debug("Fetching permissions for role: {}", roleId);
        
        List<Permission> permissions = roleService.getRolePermissions(roleId);
        ApiResponse<List<Permission>> response = ApiResponse.success(
                "Role permissions retrieved", permissions);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/permissions")
    public ResponseEntity<ApiResponse<List<Permission>>> getAllPermissions() {
        logger.debug("Fetching all available permissions");
        
        List<Permission> permissions = roleService.getAllPermissions();
        ApiResponse<List<Permission>> response = ApiResponse.success(
                "All permissions retrieved", permissions);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/permissions")
    public ResponseEntity<ApiResponse<Permission>> createPermission(
            @RequestParam String name,
            @RequestParam String resource,
            @RequestParam String action,
            @RequestParam(required = false) String description) {
        
        logger.info("Creating new permission: {}", name);
        
        Permission permission = roleService.createPermission(name, resource, action, description);
        ApiResponse<Permission> response = ApiResponse.success(
                "Permission created successfully", permission);
        
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}