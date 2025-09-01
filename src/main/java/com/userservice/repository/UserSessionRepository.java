package com.userservice.repository;

import com.userservice.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    
    Optional<UserSession> findByToken(String token);
    
    @Query("SELECT s FROM UserSession s WHERE s.user.id = :userId AND s.isActive = true")
    List<UserSession> findActiveSessionsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT s FROM UserSession s WHERE s.expiresAt < :currentTime")
    List<UserSession> findExpiredSessions(@Param("currentTime") LocalDateTime currentTime);
    
    @Modifying
    @Transactional
    @Query("UPDATE UserSession s SET s.isActive = false WHERE s.user.id = :userId")
    void deactivateAllUserSessions(@Param("userId") Long userId);
    
    @Modifying
    @Transactional
    @Query("UPDATE UserSession s SET s.isActive = false WHERE s.expiresAt < :currentTime")
    void deactivateExpiredSessions(@Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT COUNT(s) FROM UserSession s WHERE s.user.id = :userId AND s.isActive = true")
    Long countActiveSessionsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT DATE(s.createdAt), COUNT(s) FROM UserSession s " +
           "WHERE s.createdAt >= :startDate GROUP BY DATE(s.createdAt) ORDER BY DATE(s.createdAt)")
    List<Object[]> findDailyLoginStats(@Param("startDate") LocalDateTime startDate);
}