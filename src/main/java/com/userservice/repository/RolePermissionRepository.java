package com.userservice.repository;

import com.userservice.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    
    @Query("SELECT rp FROM RolePermission rp WHERE rp.role.id = :roleId")
    List<RolePermission> findByRoleId(@Param("roleId") Long roleId);
    
    @Query("SELECT rp FROM RolePermission rp WHERE rp.permission.id = :permissionId")
    List<RolePermission> findByPermissionId(@Param("permissionId") Long permissionId);
    
    @Query("SELECT rp FROM RolePermission rp WHERE rp.role.id = :roleId AND rp.permission.id = :permissionId")
    Optional<RolePermission> findByRoleIdAndPermissionId(@Param("roleId") Long roleId, 
                                                        @Param("permissionId") Long permissionId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM RolePermission rp WHERE rp.role.id = :roleId AND rp.permission.id = :permissionId")
    void deleteByRoleIdAndPermissionId(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);
    
    @Query("SELECT COUNT(rp) FROM RolePermission rp WHERE rp.role.id = :roleId")
    Long countPermissionsByRoleId(@Param("roleId") Long roleId);
    
    @Query("SELECT p.name, COUNT(rp) FROM RolePermission rp JOIN rp.permission p GROUP BY p.name")
    List<Object[]> findPermissionUsageStats();
}