package fr.tiogars.domaintemplate.domains.task.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import fr.tiogars.domaintemplate.domains.task.entities.TaskEntity;
import fr.tiogars.domaintemplate.domains.task.models.CreateTaskRequest;
import fr.tiogars.domaintemplate.domains.task.models.Task;
import fr.tiogars.domaintemplate.domains.task.repositories.TaskRepository;

@ExtendWith(MockitoExtension.class)
public class TaskCreateServiceTest {

    @Mock
    private TaskRepository repository;

    private TaskCreateService service;

    @BeforeEach
    void setUp() {
        service = new TaskCreateService(repository);
    }

    @Test
    public void create_flushesEntityBeforeMapping_whenCodeIsAvailable() {
        when(repository.saveAndFlush(any(TaskEntity.class))).thenAnswer(invocation -> {
            TaskEntity entity = invocation.getArgument(0);
            ReflectionTestUtils.setField(entity, "createdBy", "creator");
            ReflectionTestUtils.setField(entity, "lastModifiedBy", "creator");
            return entity;
        });
        when(repository.findByCode("CUSTOMER")).thenReturn(Optional.empty());

        Task created = service.create(
                new CreateTaskRequest("CUSTOMER", "Customer", "Customer template")).orElseThrow();

        assertEquals("CUSTOMER", created.code());
        assertEquals("Customer", created.label());
        assertEquals("creator", created.createdBy());
        assertEquals("creator", created.lastModifiedBy());
        verify(repository).saveAndFlush(any(TaskEntity.class));
    }

    @Test
    public void create_returnsEmpty_whenCodeAlreadyExists() {
        when(repository.findByCode("CUSTOMER")).thenReturn(Optional.of(entity("CUSTOMER", "Existing")));

        assertTrue(service.create(new CreateTaskRequest("CUSTOMER", "Customer", null)).isEmpty());
        verify(repository, never()).saveAndFlush(any());
    }

    private static TaskEntity entity(String code, String label) {
        return new TaskEntity(code, label, null);
    }
}
