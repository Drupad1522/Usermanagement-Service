package com.userservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "permissions")
@EntityListeners(AuditingEntityListener.class)
@Data   // generates getters, setters, equals, hashCode, toString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "permission_seq")
    @SequenceGenerator(name = "permission_seq", sequenceName = "permission_sequence", allocationSize = 1)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    @NotBlank(message = "Permission name is required")
    private String name;

    @Column(nullable = false, length = 50)
    @NotBlank(message = "Resource is required")
    private String resource;

    @Column(nullable = false, length = 50)
    @NotBlank(message = "Action is required")
    private String action;

    @Column(length = 255)
    private String description;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "permission", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude // prevents infinite recursion in logs
    private Set<RolePermission> rolePermissions = new HashSet<>();
}
