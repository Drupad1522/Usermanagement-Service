// AuditService.java
package com.userservice.service;

import com.userservice.entity.AuditLog;
import com.userservice.entity.User;
import com.userservice.repository.AuditLogRepository;
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
public class AuditService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public void logAction(User user, String action, String resource,
                          String ipAddress, AuditLog.ActionStatus status) {
        AuditLog auditLog = new AuditLog(user, action, resource, ipAddress, status);
        auditLogRepository.save(auditLog);
    }

    public void logAction(User user, String action, String resource,
                          String ipAddress, AuditLog.ActionStatus status, String details) {
        AuditLog auditLog = new AuditLog(user, action, resource, ipAddress, status);
        auditLog.setDetails(details);
        auditLogRepository.save(auditLog);
    }

    public void logActionWithUserAgent(User user, String action, String resource,
                                       String ipAddress, String userAgent,
                                       AuditLog.ActionStatus status) {
        AuditLog auditLog = new AuditLog(user, action, resource, ipAddress, status);
        auditLog.setUserAgent(userAgent);
        auditLogRepository.save(auditLog);
    }

    public void logActionWithDetails(User user, String action, String resource,
                                     String ipAddress, String userAgent,
                                     AuditLog.ActionStatus status, String details) {
        AuditLog auditLog = new AuditLog(user, action, resource, ipAddress, status);
        auditLog.setUserAgent(userAgent);
        auditLog.setDetails(details);
        auditLogRepository.save(auditLog);
    }

    public Page<AuditLog> getUserAuditHistory(Long userId, Pageable pageable) {
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
    }

    public List<AuditLog> getFailedLoginAttempts(String ipAddress, LocalDateTime since) {
        return auditLogRepository.findFailedAttemptsByIp(ipAddress, since);
    }

    public List<AuditLog> getUserActions(Long userId, List<String> actions,
                                         LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findUserActionsBetween(userId, actions, startDate, endDate);
    }

    public List<Object[]> getActionStatistics(LocalDateTime since) {
        return auditLogRepository.findMostFrequentActions(since);
    }

    public Long getActiveUsersCount(LocalDateTime since) {
        return auditLogRepository.countActiveUsersInPeriod(since);
    }

    public List<AuditLog> getRecentAuditLogs(String action, int limit) {
        return auditLogRepository.findByActionOrderByTimestampDesc(action)
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<AuditLog> getAuditLogsByStatus(AuditLog.ActionStatus status, LocalDateTime since) {
        return auditLogRepository.findByStatusSince(status, since);
    }

    public List<AuditLog> getSecurityEvents(LocalDateTime since) {
        List<String> securityActions = List.of("LOGIN_FAILED", "LOGIN_BLOCKED",
                "UNAUTHORIZED_ACCESS", "PERMISSION_DENIED");
        return auditLogRepository.findByActionOrderByTimestampDesc("LOGIN_FAILED")
                .stream()
                .filter(log -> log.getTimestamp().isAfter(since))
                .collect(Collectors.toList());
    }

    public Long getFailedLoginAttemptsCount(String ipAddress, LocalDateTime since) {
        return (long) auditLogRepository.findFailedAttemptsByIp(ipAddress, since).size();
    }

    public void logSecurityEvent(User user, String action, String resource,
                                 String ipAddress, String userAgent, String details) {
        logActionWithDetails(user, action, resource, ipAddress, userAgent,
                AuditLog.ActionStatus.FAILED, details);
    }

    public void cleanupOldAuditLogs(LocalDateTime cutoffDate) {
        List<AuditLog> oldLogs = auditLogRepository.findAll()
                .stream()
                .filter(log -> log.getTimestamp().isBefore(cutoffDate))
                .collect(Collectors.toList());

        auditLogRepository.deleteAll(oldLogs);
    }
}