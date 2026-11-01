package fr.tiogars.domaintemplate.domains.auth.role.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.tiogars.domaintemplate.domains.auth.role.entities.Role;
import fr.tiogars.domaintemplate.domains.auth.role.entities.RoleEntity;
import fr.tiogars.domaintemplate.domains.auth.role.models.CreateRoleRequest;
import fr.tiogars.domaintemplate.domains.auth.role.repositories.RoleRepository;

@ExtendWith(MockitoExtension.class)
public class RoleServiceTest {

    private static final Instant CLOCK_INSTANT = Instant.parse("2026-08-30T12:00:00.123456789Z");
    private static final Instant TIMESTAMP = Instant.parse("2026-08-30T12:00:00.123456Z");

    @Mock
    private RoleRepository repository;

    private RoleService service;

    @BeforeEach
    void setUp() {
        service = new RoleService(repository, Clock.fixed(CLOCK_INSTANT, ZoneOffset.UTC));
    }

    @Test
    public void seedDefaultRoles_upsertsAnonymousAdminAndUser() {
        stubSave();
        when(repository.findByCode(RoleService.ANONYMOUS)).thenReturn(Optional.empty());
        when(repository.findByCode(RoleService.ADMIN)).thenReturn(Optional.of(role(RoleService.ADMIN, "Old")));
        when(repository.findByCode(RoleService.USER)).thenReturn(Optional.empty());

        List<Role> roles = service.seedDefaultRoles();

        assertEquals(List.of(RoleService.ANONYMOUS, RoleService.ADMIN, RoleService.USER),
                roles.stream().map(role -> role.code()).toList());
        assertEquals("Anonymous", roles.getFirst().label());
        assertEquals("Administrator", roles.get(1).label());
        assertEquals(TIMESTAMP, roles.get(1).lastModifiedDate());
    }

    @Test
    public void create_returnsEmpty_whenCodeAlreadyExists() {
        when(repository.findByCode("ADMIN")).thenReturn(Optional.of(role("ADMIN", "Administrator")));

        assertTrue(service.create(new CreateRoleRequest("ADMIN", "Administrator")).isEmpty());
        verify(repository, never()).save(any());
    }

    @Test
    public void delete_refusesSystemRoleAndDeletesCustomRole() {
        RoleEntity anonymous = role(RoleService.ANONYMOUS, "Anonymous");
        RoleEntity admin = role(RoleService.ADMIN, "Administrator");
        RoleEntity manager = role("MANAGER", "Manager");
        when(repository.findById(0L)).thenReturn(Optional.of(anonymous));
        when(repository.findById(1L)).thenReturn(Optional.of(admin));
        when(repository.findById(2L)).thenReturn(Optional.of(manager));

        assertFalse(service.delete(0L));
        assertFalse(service.delete(1L));
        assertTrue(service.delete(2L));
        verify(repository, never()).delete(anonymous);
        verify(repository, never()).delete(admin);
        verify(repository).delete(manager);
    }

    private static RoleEntity role(String code, String label) {
        Instant createdDate = Instant.parse("2026-08-01T00:00:00Z");
        return new RoleEntity(code, label, createdDate, createdDate);
    }

    private void stubSave() {
        when(repository.save(any(RoleEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }
}