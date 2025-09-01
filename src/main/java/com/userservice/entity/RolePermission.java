package com.userservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "role_permissions")
@EntityListeners(AuditingEntityListener.class)
@Data   // Generates getters, setters, equals, hashCode, toString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "role_permission_seq")
    @SequenceGenerator(name = "role_permission_seq", sequenceName = "role_permission_sequence", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Permission permission;

    @CreatedDate
    @Column(name = "assigned_at", nullable = false, updatable = false)
    private LocalDateTime assignedAt;

    // Custom constructor like your original
    public RolePermission(Role role, Permission permission) {
        this.role = role;
        this.permission = permission;
    }
}
