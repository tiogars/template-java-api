package fr.tiogars.domaintemplate.domains.auth.role.entities;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Persists an authorization role. */
@Entity
@Table(name = "auth_roles")
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String code;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false, updatable = false)
    private Instant createdDate;

    @Column(nullable = false)
    private Instant lastModifiedDate;

    protected RoleEntity() {
        // required by JPA
    }

    public RoleEntity(String code, String label, Instant createdDate, Instant lastModifiedDate) {
        this.code = code;
        this.label = label;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
    }

    public void update(String label, Instant lastModifiedDate) {
        this.label = label;
        this.lastModifiedDate = lastModifiedDate;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }
}