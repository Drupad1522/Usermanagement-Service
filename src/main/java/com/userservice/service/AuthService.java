// AuthService.java
package com.userservice.service;

import com.userservice.dto.*;
import com.userservice.entity.*;
import com.userservice.exception.*;
import com.userservice.repository.*;
import com.userservice.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.login.AccountLockedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSessionRepository sessionRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private AuditService auditService;

    @Autowired
    private PasswordUtil passwordUtil;

    @Autowired
    private JwtUtil jwtUtil;

    public LoginResponse authenticateUser(UserLoginRequest request, String ipAddress, String userAgent) throws AccountLockedException {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password",
                        request.getEmail(), "USER_NOT_FOUND"));

        if (!passwordUtil.matches(request.getPassword(), user.getPassword())) {
            auditService.logAction(user, "LOGIN_FAILED", "AUTH",
                    ipAddress, AuditLog.ActionStatus.FAILED);
            throw new InvalidCredentialsException("Invalid email or password",
                    request.getEmail(), "WRONG_PASSWORD");
        }

        if (user.getStatus() != User.UserStatus.ACTIVE) {
            auditService.logAction(user, "LOGIN_BLOCKED", "AUTH",
                    ipAddress, AuditLog.ActionStatus.FAILED);
            throw new AccountLockedException();
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        // Create session
        UserSession session = new UserSession();
        session.setUser(user);
        session.setToken(token);
        session.setExpiresAt(LocalDateTime.now().plusDays(1));
        session.setIpAddress(ipAddress);
        session.setUserAgent(userAgent);
        sessionRepository.save(session);

        // Get user permissions
        List<Permission> permissions = permissionRepository.findPermissionsByUserId(user.getId());
        List<String> permissionNames = permissions.stream()
                .map(Permission::getName)
                .collect(Collectors.toList());

        // Get user roles
        List<String> roles = user.getUserRoles().stream()
                .map(ur -> ur.getRole().getName())
                .collect(Collectors.toList());

        auditService.logAction(user, "LOGIN_SUCCESS", "AUTH",
                ipAddress, AuditLog.ActionStatus.SUCCESS);

        return new LoginResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                token,
                refreshToken,
                jwtUtil.getExpirationTime(),
                roles,
                permissionNames
        );
    }

    public void logout(String token, String ipAddress) {
        UserSession session = sessionRepository.findByToken(token)
                .orElseThrow(() -> new SessionNotFoundException("Invalid session token", token));

        session.setIsActive(false);
        sessionRepository.save(session);

        auditService.logAction(session.getUser(), "LOGOUT", "AUTH",
                ipAddress, AuditLog.ActionStatus.SUCCESS);
    }

    public void logoutAllSessions(Long userId, String ipAddress) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found", userId.toString()));

        sessionRepository.deactivateAllUserSessions(userId);

        auditService.logAction(user, "LOGOUT_ALL", "AUTH",
                ipAddress, AuditLog.ActionStatus.SUCCESS);
    }

    public LoginResponse refreshToken(String refreshToken, String ipAddress) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new InvalidCredentialsException("Invalid refresh token", null, "EXPIRED_TOKEN");
        }

        String email = jwtUtil.extractUsername(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found for token", email));

        String newToken = jwtUtil.generateToken(email);
        String newRefreshToken = jwtUtil.generateRefreshToken(email);

        // Update session
        UserSession session = sessionRepository.findByToken(refreshToken).orElse(null);
        if (session != null) {
            session.setToken(newToken);
            session.setExpiresAt(LocalDateTime.now().plusDays(1));
            sessionRepository.save(session);
        }

        List<String> roles = user.getUserRoles().stream()
                .map(ur -> ur.getRole().getName())
                .collect(Collectors.toList());

        List<Permission> permissions = permissionRepository.findPermissionsByUserId(user.getId());
        List<String> permissionNames = permissions.stream()
                .map(Permission::getName)
                .collect(Collectors.toList());

        auditService.logAction(user, "TOKEN_REFRESHED", "AUTH",
                ipAddress, AuditLog.ActionStatus.SUCCESS);

        return new LoginResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                newToken,
                newRefreshToken,
                jwtUtil.getExpirationTime(),
                roles,
                permissionNames
        );
    }

    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    public UserResponse getCurrentUser(String token) {
        if (!jwtUtil.validateToken(token)) {
            throw new InvalidCredentialsException("Invalid token", null, "INVALID_TOKEN");
        }

        String email = jwtUtil.extractUsername(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found for token", email));

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

    public void cleanupExpiredSessions() {
        sessionRepository.deactivateExpiredSessions(LocalDateTime.now());
    }

    public List<UserSession> getActiveSessions(Long userId) {
        return sessionRepository.findActiveSessionsByUserId(userId);
    }

    public Long getActiveSessionCount(Long userId) {
        return sessionRepository.countActiveSessionsByUserId(userId);
    }

    public void invalidateUserSession(String token, String ipAddress) {
        UserSession session = sessionRepository.findByToken(token)
                .orElseThrow(() -> new SessionNotFoundException("Session not found", token));

        session.setIsActive(false);
        sessionRepository.save(session);

        auditService.logAction(session.getUser(), "SESSION_INVALIDATED", "AUTH",
                ipAddress, AuditLog.ActionStatus.SUCCESS);
    }
}