package com.userservice.repository;

import com.userservice.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    @Query("SELECT a FROM AuditLog a WHERE a.user.id = :userId ORDER BY a.timestamp DESC")
    Page<AuditLog> findByUserIdOrderByTimestampDesc(@Param("userId") Long userId, Pageable pageable);
    
    @Query("SELECT a FROM AuditLog a WHERE a.action = :action ORDER BY a.timestamp DESC")
    List<AuditLog> findByActionOrderByTimestampDesc(@Param("action") String action);
    
    @Query("SELECT a FROM AuditLog a WHERE a.status = :status AND a.timestamp >= :since")
    List<AuditLog> findByStatusSince(@Param("status") AuditLog.ActionStatus status, 
                                    @Param("since") LocalDateTime since);
    
    @Query("SELECT a FROM AuditLog a WHERE a.user.id = :userId AND a.action IN :actions " +
           "AND a.timestamp BETWEEN :startDate AND :endDate")
    List<AuditLog> findUserActionsBetween(@Param("userId") Long userId,
                                         @Param("actions") List<String> actions,
                                         @Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a.action, COUNT(a) FROM AuditLog a WHERE a.timestamp >= :since " +
           "GROUP BY a.action ORDER BY COUNT(a) DESC")
    List<Object[]> findMostFrequentActions(@Param("since") LocalDateTime since);
    
    @Query("SELECT a FROM AuditLog a WHERE a.ipAddress = :ipAddress " +
           "AND a.status = 'FAILED' AND a.timestamp >= :since")
    List<AuditLog> findFailedAttemptsByIp(@Param("ipAddress") String ipAddress,
                                         @Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(DISTINCT a.user.id) FROM AuditLog a WHERE a.timestamp >= :since")
    Long countActiveUsersInPeriod(@Param("since") LocalDateTime since);
}