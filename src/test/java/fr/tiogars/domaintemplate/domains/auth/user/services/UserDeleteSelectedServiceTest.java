package fr.tiogars.domaintemplate.domains.auth.user.services;

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
class UserDeleteSelectedServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserDeleteSelectedService service;

    @Test
    void deleteSelected_reportsUnknownUsersAsSkipped() {
        when(userService.delete(10L)).thenReturn(true);
        when(userService.delete(11L)).thenReturn(false);

        DeleteSelectedResponse response = service.deleteSelected(List.of(10L, 11L));

        assertThat(response.deletedIds()).containsExactly(10L);
        assertThat(response.skippedIds()).containsExactly(11L);
    }
}
