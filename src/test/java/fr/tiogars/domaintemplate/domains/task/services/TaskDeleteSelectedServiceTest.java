package fr.tiogars.domaintemplate.domains.task.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.tiogars.domaintemplate.common.models.DeleteSelectedResponse;

@ExtendWith(MockitoExtension.class)
class TaskDeleteSelectedServiceTest {

    @Mock
    private TaskDeleteService deleteService;

    @InjectMocks
    private TaskDeleteSelectedService service;

    @Test
    void deleteSelected_reportsDeletedAndSkippedIds_andIgnoresDuplicates() {
        when(deleteService.delete(1L)).thenReturn(true);
        when(deleteService.delete(2L)).thenReturn(false);
        when(deleteService.delete(3L)).thenReturn(true);

        DeleteSelectedResponse response = service.deleteSelected(List.of(1L, 2L, 3L, 1L));

        assertThat(response.deletedIds()).containsExactly(1L, 3L);
        assertThat(response.skippedIds()).containsExactly(2L);
        verify(deleteService, times(1)).delete(1L);
    }
}
