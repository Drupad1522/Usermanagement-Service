package com.userservice.repository;

import com.userservice.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    
    Optional<Permission> findByName(String name);
    List<Permission> findByResource(String resource);
    List<Permission> findByAction(String action);
    
    @Query("SELECT p FROM Permission p WHERE p.resource = :resource AND p.action = :action")
    Optional<Permission> findByResourceAndAction(@Param("resource") String resource, 
                                                @Param("action") String action);
    
    @Query("SELECT DISTINCT p FROM Permission p JOIN p.rolePermissions rp " +
           "JOIN rp.role r JOIN r.userRoles ur WHERE ur.user.id = :userId")
    List<Permission> findPermissionsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT p FROM Permission p WHERE p.resource IN :resources")
    List<Permission> findByResources(@Param("resources") List<String> resources);
    
    @Query("SELECT p.resource, COUNT(p) FROM Permission p GROUP BY p.resource")
    List<Object[]> findPermissionCountByResource();
}