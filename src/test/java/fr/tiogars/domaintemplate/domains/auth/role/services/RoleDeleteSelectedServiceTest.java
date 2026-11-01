package fr.tiogars.domaintemplate.domains.auth.role.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.tiogars.domaintemplate.common.models.DeleteSelectedResponse;

@ExtendWith(MockitoExtension.class)
class RoleDeleteSelectedServiceTest {

    @Mock
    private RoleService roleService;

    @InjectMocks
    private RoleDeleteSelectedService service;

    @Test
    void deleteSelected_reportsProtectedRolesAsSkipped() {
        when(roleService.delete(5L)).thenReturn(true);
        when(roleService.delete(1L)).thenReturn(false);

        DeleteSelectedResponse response = service.deleteSelected(List.of(5L, 1L));

        assertThat(response.deletedIds()).containsExactly(5L);
        assertThat(response.skippedIds()).containsExactly(1L);
    }
}
