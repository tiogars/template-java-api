package fr.tiogars.domaintemplate.domains.task.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import fr.tiogars.domaintemplate.domains.task.entities.TaskEntity;
import fr.tiogars.domaintemplate.domains.task.models.Task;
import fr.tiogars.domaintemplate.domains.task.repositories.TaskRepository;

@ExtendWith(MockitoExtension.class)
public class TaskFindByIdServiceTest {

    @Mock
    private TaskRepository repository;

    @Test
    public void findById_returnsEmptyWhenMissing() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertTrue(new TaskFindByIdService(repository).findById(42L).isEmpty());
    }

    @Test
    public void findById_mapsFoundTemplate() {
        TaskEntity entity = new TaskEntity("CUSTOMER", "Customer", null);
        ReflectionTestUtils.setField(entity, "createdBy", "creator");
        ReflectionTestUtils.setField(entity, "lastModifiedBy", "editor");
        when(repository.findById(42L)).thenReturn(Optional.of(entity));

        Task found = new TaskFindByIdService(repository).findById(42L).orElseThrow();

        assertEquals("CUSTOMER", found.code());
        assertEquals("Customer", found.label());
        assertEquals("creator", found.createdBy());
        assertEquals("editor", found.lastModifiedBy());
    }
}
