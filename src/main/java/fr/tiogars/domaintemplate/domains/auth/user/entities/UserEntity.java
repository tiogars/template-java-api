package fr.tiogars.domaintemplate.domains.auth.user.entities;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

import fr.tiogars.domaintemplate.domains.auth.role.entities.RoleEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

/** Persists a user account and its password hash. */
@Entity
@Table(name = "auth_users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private boolean enabled;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "auth_user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<RoleEntity> roles = new LinkedHashSet<>();

    @Column(nullable = false, updatable = false)
    private Instant createdDate;

    @Column(nullable = false)
    private Instant lastModifiedDate;

    protected UserEntity() {
        // required by JPA
    }

    public UserEntity(
            String username,
            String passwordHash,
            boolean enabled,
            Set<RoleEntity> roles,
            Instant createdDate,
            Instant lastModifiedDate) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.enabled = enabled;
        this.roles = new LinkedHashSet<>(roles);
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
    }

    public void update(String username, String passwordHash, boolean enabled, Set<RoleEntity> roles, Instant lastModifiedDate) {
        this.username = username;
        if (passwordHash != null) {
            this.passwordHash = passwordHash;
        }
        this.enabled = enabled;
        this.roles = new LinkedHashSet<>(roles);
        this.lastModifiedDate = lastModifiedDate;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Set<RoleEntity> getRoles() {
        return roles;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }
}