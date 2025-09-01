// RoleService.java
package com.userservice.service;

import com.userservice.dto.*;
import com.userservice.entity.*;
import com.userservice.exception.*;
import com.userservice.repository.*;
import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    public List<RoleResponse> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream()
                .map(this::convertToRoleResponse)
                .collect(Collectors.toList());
    }

    public RoleResponse getRoleById(Long roleId) throws RoleNotFoundException {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException("Permission not found", roleId.toString()));
        return convertToRoleResponse(role);
    }

    public RoleResponse getRoleByName(String roleName) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RoleNotFoundException("Role not found with name: " + roleName, roleName));
        return convertToRoleResponse(role);
    }

    public RoleResponse createRole(RoleCreateRequest request) {
        if (roleRepository.existsByName(request.getName())) {
            throw new DuplicateUserException("Role with name already exists: " + request.getName(),
                    "name", request.getName());
        }

        Role role = Role.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        Role savedRole = roleRepository.save(role);

        return convertToRoleResponse(savedRole);
    }

    public RoleResponse updateRole(Long roleId, RoleCreateRequest request) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException("Role not found", roleId.toString()));

        // Check if name is being changed and if new name already exists
        if (!role.getName().equals(request.getName()) &&
                roleRepository.existsByName(request.getName())) {
            throw new DuplicateUserException("Role with name already exists: " + request.getName(),
                    "name", request.getName());
        }

        role.setName(request.getName());
        role.setDescription(request.getDescription());

        Role updatedRole = roleRepository.save(role);
        return convertToRoleResponse(updatedRole);
    }

    public void deleteRole(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException("Role not found", roleId.toString()));

        // Check if role is assigned to any users
        List<UserRole> userRoles = userRoleRepository.findByRoleId(roleId);
        if (!userRoles.isEmpty()) {
            throw new ValidationException("Cannot delete role. It is assigned to " + userRoles.size() + " users");
        }

        roleRepository.delete(role);
    }

    public List<RoleResponse> getAvailableRoles() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream()
                .map(role -> new RoleResponse(
                        role.getId(),
                        role.getName(),
                        role.getDescription(),
                        getPermissionCountForRole(role.getId())
                ))
                .collect(Collectors.toList());
    }

    public RoleDistributionResponse getRoleDistribution() {
        List<Object[]> distribution = userRoleRepository.findRoleDistribution();
        List<RoleDistributionResponse.RoleCount> roleCounts = distribution.stream()
                .map(row -> new RoleDistributionResponse.RoleCount(
                        (String) row[0],
                        ((Number) row[1]).longValue()))
                .collect(Collectors.toList());

        return new RoleDistributionResponse(roleCounts);
    }

    public void assignPermissionToRole(Long roleId, Long permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException("Role not found", roleId.toString()));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new PermissionNotFoundException("Permission not found", permissionId.toString(),null));

        // Check if permission already assigned
        if (rolePermissionRepository.findByRoleIdAndPermissionId(roleId, permissionId).isPresent()) {
            throw new ValidationException("Permission already assigned to role");
        }

        RolePermission rolePermission = new RolePermission(role, permission);
        rolePermissionRepository.save(rolePermission);
    }

    public void removePermissionFromRole(Long roleId, Long permissionId) {
        // Verify role and permission exist
        roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException("Role not found"));
        permissionRepository.findById(permissionId)
                .orElseThrow(() -> new PermissionNotFoundException("Permission not found", permissionId.toString(),null));

        rolePermissionRepository.deleteByRoleIdAndPermissionId(roleId, permissionId);
    }

    public List<Permission> getRolePermissions(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException("Role not found", roleId.toString()));

        return role.getRolePermissions().stream()
                .map(RolePermission::getPermission)
                .collect(Collectors.toList());
    }

    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    public Permission createPermission(String name, String resource, String action, String description) {
        if (permissionRepository.findByName(name).isPresent()) {
            throw new ValidationException("Permission with name already exists: " + name);
        }

        Permission permission = Permission.builder()
                .name("READ_USER")
                .resource("User")
                .action("READ")
                .description("Can read user data")
                .build();
        return permissionRepository.save(permission);
    }

    private Long getPermissionCountForRole(Long roleId) {
        Role role = roleRepository.findById(roleId).orElse(null);
        return role != null ? (long) role.getRolePermissions().size() : 0L;
    }

    private RoleResponse convertToRoleResponse(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getDescription(),
                getPermissionCountForRole(role.getId())
        );
    }
}