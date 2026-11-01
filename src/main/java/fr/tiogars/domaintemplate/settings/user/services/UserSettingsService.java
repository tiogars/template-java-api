package fr.tiogars.domaintemplate.settings.user.services;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.tiogars.domaintemplate.domains.auth.role.services.RoleService;
import fr.tiogars.domaintemplate.domains.auth.user.entities.UserEntity;
import fr.tiogars.domaintemplate.domains.auth.user.repositories.UserRepository;
import fr.tiogars.domaintemplate.settings.user.entities.UserSettings;
import fr.tiogars.domaintemplate.settings.user.entities.UserSettingsEntity;
import fr.tiogars.domaintemplate.settings.user.models.UpdateUserSettingsRequest;
import fr.tiogars.domaintemplate.settings.user.repositories.UserSettingsRepository;

/** Coordinates user settings, the ANONYMOUS role settings and their defaults. */
@Service
public class UserSettingsService {

    public static final String DEFAULT_VIEW_MODE = "system";

    private final Clock clock;
    private final UserRepository userRepository;
    private final UserSettingsRepository repository;

    @Autowired
    public UserSettingsService(UserSettingsRepository repository, UserRepository userRepository) {
        this(repository, userRepository, Clock.systemUTC());
    }

    UserSettingsService(UserSettingsRepository repository, UserRepository userRepository, Clock clock) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    /** Returns the settings of the authenticated account, or empty when the account does not exist. */
    @Transactional(readOnly = true)
    public Optional<UserSettings> findByUsername(String username) {
        return userRepository.findByUsername(username).map(this::readUserSettings);
    }

    /** Updates the settings of the authenticated account, or returns empty when the account does not exist. */
    @Transactional
    public Optional<UserSettings> updateByUsername(String username, UpdateUserSettingsRequest request) {
        return userRepository.findByUsername(username).map(user -> writeUserSettings(user, request));
    }

    /** Returns the settings of an account holding the USER role, or empty otherwise. */
    @Transactional(readOnly = true)
    public Optional<UserSettings> findByUserId(Long userId) {
        return findManagedUser(userId).map(this::readUserSettings);
    }

    /** Updates the settings of an account holding the USER role, or returns empty otherwise. */
    @Transactional
    public Optional<UserSettings> updateByUserId(Long userId, UpdateUserSettingsRequest request) {
        return findManagedUser(userId).map(user -> writeUserSettings(user, request));
    }

    @Transactional(readOnly = true)
    public UserSettings findAnonymous() {
        return repository.findByRoleCode(RoleService.ANONYMOUS)
                .map(UserSettingsService::toRecord)
                .orElseGet(() -> defaults(null, RoleService.ANONYMOUS));
    }

    @Transactional
    public UserSettings updateAnonymous(UpdateUserSettingsRequest request) {
        Instant timestamp = currentTimestamp();
        UserSettingsEntity entity = repository.findByRoleCode(RoleService.ANONYMOUS)
                .map(existing -> {
                    existing.update(request.viewMode(), timestamp);
                    return existing;
                })
                .orElseGet(() -> UserSettingsEntity.forRole(RoleService.ANONYMOUS, request.viewMode(), timestamp));
        return toRecord(repository.save(entity));
    }

    private Optional<UserEntity> findManagedUser(Long userId) {
        return userRepository.findById(userId)
                .filter(user -> user.getRoles().stream().anyMatch(role -> RoleService.USER.equals(role.getCode())));
    }

    private UserSettings readUserSettings(UserEntity user) {
        return repository.findByUserId(user.getId())
                .map(UserSettingsService::toRecord)
                .orElseGet(() -> defaults(user.getId(), null));
    }

    private UserSettings writeUserSettings(UserEntity user, UpdateUserSettingsRequest request) {
        Instant timestamp = currentTimestamp();
        UserSettingsEntity entity = repository.findByUserId(user.getId())
                .map(existing -> {
                    existing.update(request.viewMode(), timestamp);
                    return existing;
                })
                .orElseGet(() -> UserSettingsEntity.forUser(user, request.viewMode(), timestamp));
        return toRecord(repository.save(entity));
    }

    private Instant currentTimestamp() {
        return Instant.now(clock).truncatedTo(ChronoUnit.MICROS);
    }

    private static UserSettings defaults(Long userId, String roleCode) {
        return new UserSettings(userId, roleCode, DEFAULT_VIEW_MODE, null, null);
    }

    public static UserSettings toRecord(UserSettingsEntity entity) {
        return new UserSettings(
                entity.getUser() == null ? null : entity.getUser().getId(),
                entity.getRoleCode(),
                entity.getViewMode(),
                entity.getCreatedDate(),
                entity.getLastModifiedDate());
    }
}
