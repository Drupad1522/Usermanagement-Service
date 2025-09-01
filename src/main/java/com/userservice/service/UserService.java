// UserService.java
package com.userservice.service;

import com.userservice.dto.*;
import com.userservice.entity.*;
import com.userservice.exception.*;
import com.userservice.repository.*;
import com.userservice.util.PasswordUtil;
import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private AuditService auditService;

    @Autowired
    private PasswordUtil passwordUtil;

    public UserResponse registerUser(UserRegistrationRequest request, String ipAddress) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateUserException("Email already registered", "email", request.getEmail());
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateUserException("Username already taken", "username", request.getUsername());
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordUtil.encodePassword(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setStatus(User.UserStatus.ACTIVE);

        User savedUser = userRepository.save(user);

        // Assign default role
        Role defaultRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RoleNotFoundException("Default role 'USER' not found", "USER"));

        UserRole userRole = new UserRole(savedUser, defaultRole, null);
        userRoleRepository.save(userRole);

        // Log registration
        auditService.logAction(savedUser, "USER_REGISTRATION", "USER",
                ipAddress, AuditLog.ActionStatus.SUCCESS);

        return convertToUserResponse(savedUser);
    }

    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId, userId.toString()));
        return convertToUserResponse(user);
    }

    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email, email));
        return convertToUserResponse(user);
    }

    public UserResponse updateUserProfile(Long userId, UserUpdateRequest request, String ipAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found", userId.toString()));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        User updatedUser = userRepository.save(user);

        auditService.logAction(user, "PROFILE_UPDATE", "USER",
                ipAddress, AuditLog.ActionStatus.SUCCESS);

        return convertToUserResponse(updatedUser);
    }

    public void assignRoleToUser(Long userId, Long roleId, Long assignedBy, String ipAddress) throws RoleNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found", userId.toString()));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException("Role not Found",roleId.toString()));

        // Check if role already assigned
        if (userRoleRepository.findByUserIdAndRoleId(userId, roleId).isPresent()) {
            throw new ValidationException("Role already assigned to user");
        }

        UserRole userRole = new UserRole(user, role, assignedBy);
        userRoleRepository.save(userRole);

        auditService.logAction(user, "ROLE_ASSIGNMENT", "USER_ROLE",
                ipAddress, AuditLog.ActionStatus.SUCCESS);
    }

    public void removeRoleFromUser(Long userId, Long roleId, String ipAddress) throws RoleNotFoundException {
        // Verify user and role exist
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found", userId.toString()));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException("Role not Found",roleId.toString()));

        userRoleRepository.deleteByUserIdAndRoleId(userId, roleId);

        auditService.logAction(user, "ROLE_REMOVAL", "USER_ROLE",
                ipAddress, AuditLog.ActionStatus.SUCCESS);
    }

    public Page<UserResponse> searchUsers(String keyword, Pageable pageable) {
        Page<User> users = userRepository.searchUsers(keyword, pageable);
        return users.map(this::convertToUserResponse);
    }

    public List<UserResponse> getUsersByRole(String roleName) {
        List<User> users = userRepository.findUsersByRoleName(roleName);
        return users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    public UserStatsResponse getUserStats() {
        Long totalUsers = userRepository.count();
        Long activeUsers = userRepository.countUsersByStatus(User.UserStatus.ACTIVE);
        Long inactiveUsers = userRepository.countUsersByStatus(User.UserStatus.INACTIVE);
        Long suspendedUsers = userRepository.countUsersByStatus(User.UserStatus.SUSPENDED);

        return new UserStatsResponse(totalUsers, activeUsers, inactiveUsers, suspendedUsers);
    }

    public List<UserResponse> getActiveUsers() {
        List<User> activeUsers = userRepository.findActiveUsers(LocalDateTime.now());
        return activeUsers.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    public void suspendUser(Long userId, String ipAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found", userId.toString()));

        user.setStatus(User.UserStatus.SUSPENDED);
        userRepository.save(user);

        auditService.logAction(user, "USER_SUSPENDED", "USER",
                ipAddress, AuditLog.ActionStatus.SUCCESS);
    }

    public void reactivateUser(Long userId, String ipAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found", userId.toString()));

        user.setStatus(User.UserStatus.ACTIVE);
        userRepository.save(user);

        auditService.logAction(user, "USER_REACTIVATED", "USER",
                ipAddress, AuditLog.ActionStatus.SUCCESS);
    }

    public void changeUserPassword(Long userId, String currentPassword, String newPassword, String ipAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found", userId.toString()));

        // Verify current password
        if (!passwordUtil.matches(currentPassword, user.getPassword())) {
            auditService.logAction(user, "PASSWORD_CHANGE_FAILED", "USER",
                    ipAddress, AuditLog.ActionStatus.FAILED);
            throw new InvalidCredentialsException("Current password is incorrect", user.getEmail(), "WRONG_CURRENT_PASSWORD");
        }

        // Update password
        user.setPassword(passwordUtil.encodePassword(newPassword));
        userRepository.save(user);

        auditService.logAction(user, "PASSWORD_CHANGED", "USER",
                ipAddress, AuditLog.ActionStatus.SUCCESS);
    }

    private UserResponse convertToUserResponse(User user) {
        List<String> roles = user.getUserRoles().stream()
                .map(ur -> ur.getRole().getName())
                .collect(Collectors.toList());

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getStatus().toString(),
                roles,
                user.getCreatedAt()
        );
    }
}