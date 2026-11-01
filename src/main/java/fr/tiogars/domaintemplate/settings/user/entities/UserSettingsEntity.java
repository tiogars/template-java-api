package fr.tiogars.domaintemplate.settings.user.entities;

import java.time.Instant;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import fr.tiogars.domaintemplate.domains.auth.user.entities.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/** Persists the settings owned either by one user account or by a role profile such as ANONYMOUS. */
@Entity
@Table(name = "user_settings")
public class UserSettingsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserEntity user;

    @Column(name = "role_code", unique = true, length = 64)
    private String roleCode;

    @Column(name = "view_mode", nullable = false, length = 16)
    private String viewMode;

    @Column(nullable = false, updatable = false)
    private Instant createdDate;

    @Column(nullable = false)
    private Instant lastModifiedDate;

    protected UserSettingsEntity() {
        // required by JPA
    }

    private UserSettingsEntity(UserEntity user, String roleCode, String viewMode, Instant createdDate,
            Instant lastModifiedDate) {
        this.user = user;
        this.roleCode = roleCode;
        this.viewMode = viewMode;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
    }

    public static UserSettingsEntity forUser(UserEntity user, String viewMode, Instant timestamp) {
        return new UserSettingsEntity(user, null, viewMode, timestamp, timestamp);
    }

    public static UserSettingsEntity forRole(String roleCode, String viewMode, Instant timestamp) {
        return new UserSettingsEntity(null, roleCode, viewMode, timestamp, timestamp);
    }

    public void update(String viewMode, Instant lastModifiedDate) {
        this.viewMode = viewMode;
        this.lastModifiedDate = lastModifiedDate;
    }

    public Long getId() {
        return id;
    }

    public UserEntity getUser() {
        return user;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public String getViewMode() {
        return viewMode;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }
}
