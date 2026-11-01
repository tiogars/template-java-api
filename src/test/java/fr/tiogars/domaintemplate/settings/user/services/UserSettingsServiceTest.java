package fr.tiogars.domaintemplate.settings.user.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;

import fr.tiogars.domaintemplate.domains.auth.role.entities.RoleEntity;
import fr.tiogars.domaintemplate.domains.auth.role.services.RoleService;
import fr.tiogars.domaintemplate.domains.auth.user.entities.UserEntity;
import fr.tiogars.domaintemplate.domains.auth.user.repositories.UserRepository;
import fr.tiogars.domaintemplate.settings.user.entities.UserSettings;
import fr.tiogars.domaintemplate.settings.user.entities.UserSettingsEntity;
import fr.tiogars.domaintemplate.settings.user.models.UpdateUserSettingsRequest;
import fr.tiogars.domaintemplate.settings.user.repositories.UserSettingsRepository;

@ExtendWith(MockitoExtension.class)
class UserSettingsServiceTest {

    private static final Instant TIMESTAMP = Instant.parse("2026-01-02T03:04:05.123456789Z").truncatedTo(ChronoUnit.MICROS);
    private static final Instant CREATED = Instant.parse("2025-01-01T00:00:00Z");

    @Mock
    private UserSettingsRepository repository;

    @Mock
    private UserRepository userRepository;

    private UserSettingsService service;

    @BeforeEach
    void setUp() {
        service = new UserSettingsService(repository, userRepository, Clock.fixed(TIMESTAMP, ZoneOffset.UTC));
    }

    @Test
    void findByUsername_returnsDefaultsWhenNothingIsPersisted() {
        UserEntity alice = user(7L, RoleService.USER);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(repository.findByUserId(7L)).thenReturn(Optional.empty());

        assertThat(service.findByUsername("alice"))
                .contains(new UserSettings(7L, null, UserSettingsService.DEFAULT_VIEW_MODE, null, null));
    }

    @Test
    void findByUsername_returnsEmptyForUnknownAccount() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThat(service.findByUsername("ghost")).isEmpty();
    }

    @Test
    void updateByUsername_createsSettingsWhenMissing() {
        UserEntity alice = user(7L, RoleService.USER);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));
        when(repository.findByUserId(7L)).thenReturn(Optional.empty());
        stubSave();

        assertThat(service.updateByUsername("alice", new UpdateUserSettingsRequest("dark")))
                .contains(new UserSettings(7L, null, "dark", TIMESTAMP, TIMESTAMP));
    }

    @Test
    void updateByUsername_updatesExistingSettings() {
        UserEntity user = user(7L, RoleService.ADMIN);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(repository.findByUserId(7L)).thenReturn(Optional.of(UserSettingsEntity.forUser(user, "light", CREATED)));
        stubSave();

        assertThat(service.updateByUsername("admin", new UpdateUserSettingsRequest("dark")))
                .contains(new UserSettings(7L, null, "dark", CREATED, TIMESTAMP));
    }

    @Test
    void findByUserId_returnsSettingsOfUserRoleAccount() {
        UserEntity user = user(9L, RoleService.USER);
        when(userRepository.findById(9L)).thenReturn(Optional.of(user));
        when(repository.findByUserId(9L)).thenReturn(Optional.of(UserSettingsEntity.forUser(user, "light", CREATED)));

        assertThat(service.findByUserId(9L)).contains(new UserSettings(9L, null, "light", CREATED, CREATED));
    }

    @Test
    void findByUserId_returnsEmptyForAccountWithoutUserRole() {
        UserEntity admin = user(1L, RoleService.ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThat(service.findByUserId(1L)).isEmpty();
        verify(repository, never()).findByUserId(any());
    }

    @Test
    void updateByUserId_returnsEmptyForUnknownOrNonUserAccount() {
        UserEntity admin = user(1L, RoleService.ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(userRepository.findById(404L)).thenReturn(Optional.empty());

        assertThat(service.updateByUserId(1L, new UpdateUserSettingsRequest("dark"))).isEmpty();
        assertThat(service.updateByUserId(404L, new UpdateUserSettingsRequest("dark"))).isEmpty();
        verify(repository, never()).save(any());
    }

    @Test
    void updateByUserId_updatesUserRoleAccount() {
        UserEntity bob = user(9L, RoleService.USER);
        when(userRepository.findById(9L)).thenReturn(Optional.of(bob));
        when(repository.findByUserId(9L)).thenReturn(Optional.empty());
        stubSave();

        assertThat(service.updateByUserId(9L, new UpdateUserSettingsRequest("light")))
                .contains(new UserSettings(9L, null, "light", TIMESTAMP, TIMESTAMP));
    }

    @Test
    void findAnonymous_returnsDefaultsWhenNothingIsPersisted() {
        when(repository.findByRoleCode(RoleService.ANONYMOUS)).thenReturn(Optional.empty());

        assertThat(service.findAnonymous())
                .isEqualTo(new UserSettings(null, RoleService.ANONYMOUS, UserSettingsService.DEFAULT_VIEW_MODE, null, null));
    }

    @Test
    void updateAnonymous_createsThenUpdatesRoleSettings() {
        when(repository.findByRoleCode(RoleService.ANONYMOUS)).thenReturn(Optional.empty());
        stubSave();

        assertThat(service.updateAnonymous(new UpdateUserSettingsRequest("dark")))
                .isEqualTo(new UserSettings(null, RoleService.ANONYMOUS, "dark", TIMESTAMP, TIMESTAMP));

        UserSettingsEntity existing = UserSettingsEntity.forRole(RoleService.ANONYMOUS, "dark", CREATED);
        when(repository.findByRoleCode(RoleService.ANONYMOUS)).thenReturn(Optional.of(existing));

        assertThat(service.updateAnonymous(new UpdateUserSettingsRequest("light")))
                .isEqualTo(new UserSettings(null, RoleService.ANONYMOUS, "light", CREATED, TIMESTAMP));
    }

    private void stubSave() {
        when(repository.save(any(UserSettingsEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private static UserEntity user(Long id, String roleCode) {
        UserEntity user = mock(UserEntity.class, withSettings().strictness(Strictness.LENIENT));
        when(user.getId()).thenReturn(id);
        when(user.getRoles()).thenReturn(Set.of(new RoleEntity(roleCode, roleCode, CREATED, CREATED)));
        return user;
    }
}
