package com.userservice.repository;

import com.userservice.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    Optional<Role> findByName(String name);
    boolean existsByName(String name);
    
    @Query("SELECT r FROM Role r JOIN r.rolePermissions rp JOIN rp.permission p " +
           "WHERE p.name = :permissionName")
    List<Role> findRolesByPermission(@Param("permissionName") String permissionName);
    
    @Query("SELECT r, COUNT(ur) FROM Role r LEFT JOIN r.userRoles ur GROUP BY r")
    List<Object[]> findRolesWithUserCount();
    
    @Query("SELECT r FROM Role r WHERE SIZE(r.rolePermissions) >= :minPermissions")
    List<Role> findRolesWithMinimumPermissions(@Param("minPermissions") int minPermissions);
    
    @Query("SELECT DISTINCT r FROM Role r JOIN r.userRoles ur WHERE ur.user.id = :userId")
    List<Role> findRolesByUserId(@Param("userId") Long userId);
}