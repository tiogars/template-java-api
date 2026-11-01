package fr.tiogars.domaintemplate.domains.task.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import fr.tiogars.domaintemplate.domains.task.entities.TaskEntity;
import fr.tiogars.domaintemplate.domains.task.models.Task;
import fr.tiogars.domaintemplate.domains.task.models.UpdateTaskRequest;
import fr.tiogars.domaintemplate.domains.task.repositories.TaskRepository;

@ExtendWith(MockitoExtension.class)
public class TaskUpdateServiceTest {

    @Mock
    private TaskRepository repository;

    private TaskUpdateService service;

    @BeforeEach
    void setUp() {
        service = new TaskUpdateService(repository);
    }

    @Test
    public void update_preservesCodeAndCreationDate() {
        TaskEntity entity = new TaskEntity(
                "CUSTOMER", "Old label", null);
        ReflectionTestUtils.setField(entity, "createdDate", Instant.parse("2026-08-01T00:00:00Z"));
        ReflectionTestUtils.setField(entity, "createdBy", "creator");
        ReflectionTestUtils.setField(entity, "lastModifiedBy", "creator");
        when(repository.findById(42L)).thenReturn(Optional.of(entity));
        when(repository.saveAndFlush(any(TaskEntity.class))).thenAnswer(invocation -> {
            TaskEntity saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "lastModifiedBy", "editor");
            return saved;
        });

        Task updated = service.update(
                42L, new UpdateTaskRequest("New label", "Updated")).orElseThrow();

        assertEquals("CUSTOMER", updated.code());
        assertEquals(Instant.parse("2026-08-01T00:00:00Z"), updated.createdDate());
        assertEquals("New label", updated.label());
        assertEquals("creator", updated.createdBy());
        assertEquals("editor", updated.lastModifiedBy());
    }
}
