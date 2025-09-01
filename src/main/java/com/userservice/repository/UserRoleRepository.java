package com.userservice.repository;

import com.userservice.entity.UserRole;
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
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    
    @Query("SELECT ur FROM UserRole ur WHERE ur.user.id = :userId")
    List<UserRole> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT ur FROM UserRole ur WHERE ur.role.id = :roleId")
    List<UserRole> findByRoleId(@Param("roleId") Long roleId);
    
    @Query("SELECT ur FROM UserRole ur WHERE ur.user.id = :userId AND ur.role.id = :roleId")
    Optional<UserRole> findByUserIdAndRoleId(@Param("userId") Long userId, 
                                           @Param("roleId") Long roleId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM UserRole ur WHERE ur.user.id = :userId AND ur.role.id = :roleId")
    void deleteByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);
    
    @Query("SELECT ur FROM UserRole ur WHERE ur.assignedAt BETWEEN :startDate AND :endDate")
    List<UserRole> findRoleAssignmentsBetween(@Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT r.name, COUNT(ur) FROM UserRole ur JOIN ur.role r GROUP BY r.name")
    List<Object[]> findRoleDistribution();
}
