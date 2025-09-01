package com.userservice.repository;

import com.userservice.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Basic finder methods
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    
    // Advanced JPQL Queries
    
    @Query("SELECT u FROM User u WHERE u.status = :status")
    List<User> findByStatus(@Param("status") User.UserStatus status);
    
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    List<User> findUsersCreatedBetween(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT u FROM User u JOIN u.userRoles ur JOIN ur.role r WHERE r.name = :roleName")
    List<User> findUsersByRoleName(@Param("roleName") String roleName);
    
    @Query("SELECT u FROM User u JOIN u.userRoles ur JOIN ur.role r " +
           "JOIN r.rolePermissions rp JOIN rp.permission p " +
           "WHERE p.name = :permissionName")
    List<User> findUsersByPermission(@Param("permissionName") String permissionName);
    
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = :status")
    Long countUsersByStatus(@Param("status") User.UserStatus status);
    
    @Query("SELECT u FROM User u WHERE u.id IN " +
           "(SELECT s.user.id FROM UserSession s WHERE s.isActive = true " +
           "AND s.expiresAt > :currentTime)")
    List<User> findActiveUsers(@Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT u FROM User u WHERE SIZE(u.userRoles) > :roleCount")
    List<User> findUsersWithMultipleRoles(@Param("roleCount") int roleCount);
}
