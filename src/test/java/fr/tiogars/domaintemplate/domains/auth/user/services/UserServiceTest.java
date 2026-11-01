package fr.tiogars.domaintemplate.domains.auth.user.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import fr.tiogars.domaintemplate.domains.auth.role.entities.Role;
import fr.tiogars.domaintemplate.domains.auth.role.entities.RoleEntity;
import fr.tiogars.domaintemplate.domains.auth.role.services.RoleService;
import fr.tiogars.domaintemplate.domains.auth.user.entities.User;
import fr.tiogars.domaintemplate.domains.auth.user.entities.UserEntity;
import fr.tiogars.domaintemplate.domains.auth.user.models.CreateUserRequest;
import fr.tiogars.domaintemplate.domains.auth.user.models.UpdateUserRequest;
import fr.tiogars.domaintemplate.domains.auth.user.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    private static final Instant CLOCK_INSTANT = Instant.parse("2026-08-30T12:00:00.123456789Z");
    private static final Instant TIMESTAMP = Instant.parse("2026-08-30T12:00:00.123456Z");

    @Mock
    private UserRepository repository;

    @Mock
    private RoleService roleService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(repository, roleService, passwordEncoder, Clock.fixed(CLOCK_INSTANT, ZoneOffset.UTC));
    }

    @Test
    public void create_hashesPasswordAndDoesNotExposeIt() {
        RoleEntity userRole = role(RoleService.USER, "User");
        stubSave();
        when(repository.findByUsername("operator")).thenReturn(Optional.empty());
        when(roleService.findEntityByCode(RoleService.USER)).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("secret")).thenReturn("hashed-secret");

        User created = service.create(new CreateUserRequest("operator", "secret", true, Set.of(RoleService.USER))).orElseThrow();

        assertEquals("operator", created.username());
        assertEquals(List.of(RoleService.USER), roleCodes(created));
        assertEquals(TIMESTAMP, created.createdDate());
        verify(passwordEncoder).encode("secret");
    }

    @Test
    public void update_keepsPasswordHash_whenPasswordIsBlank() {
        RoleEntity userRole = role(RoleService.USER, "User");
        UserEntity existing = user("operator", "old-hash", userRole);
        stubSave();
        when(repository.findById(42L)).thenReturn(Optional.of(existing));
        when(repository.findByUsername("operator")).thenReturn(Optional.of(existing));
        when(roleService.findEntityByCode(RoleService.USER)).thenReturn(Optional.of(userRole));

        service.update(42L, new UpdateUserRequest("operator", " ", true, Set.of(RoleService.USER))).orElseThrow();

        assertEquals("old-hash", existing.getPasswordHash());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    public void forceAdminUser_replacesPasswordAndAdminRole() {
        RoleEntity userRole = role(RoleService.USER, "User");
        RoleEntity adminRole = role(RoleService.ADMIN, "Administrator");
        UserEntity existing = user("admin", "old-hash", userRole);
        stubSave();
        when(roleService.findEntityByCode(RoleService.ADMIN)).thenReturn(Optional.of(adminRole));
        when(repository.findByUsername("admin")).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("new-secret")).thenReturn("new-hash");

        User admin = service.forceAdminUser("admin", "new-secret");

        assertTrue(admin.enabled());
        assertEquals(List.of(RoleService.ADMIN), roleCodes(admin));
        assertEquals("new-hash", existing.getPasswordHash());
    }

    private static RoleEntity role(String code, String label) {
        Instant createdDate = Instant.parse("2026-08-01T00:00:00Z");
        return new RoleEntity(code, label, createdDate, createdDate);
    }

    private static UserEntity user(String username, String passwordHash, RoleEntity role) {
        Instant createdDate = Instant.parse("2026-08-01T00:00:00Z");
        return new UserEntity(username, passwordHash, true, Set.of(role), createdDate, createdDate);
    }

    private static List<String> roleCodes(User user) {
        List<String> codes = new ArrayList<>();
        for (Role role : user.roles()) {
            codes.add(role.code());
        }
        return codes;
    }

    private void stubSave() {
        when(repository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }
}