package fr.tiogars.domaintemplate.domains.task.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
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
public class TaskSeedServiceTest {

    @Mock
    private TaskRepository repository;

    private TaskSeedService service;

    @BeforeEach
    void setUp() {
        service = new TaskSeedService(repository);
    }

    @Test
    public void seed_upsertsByCode() {
        TaskEntity existing = new TaskEntity(
                "CUSTOMER", "Old label", null);
        ReflectionTestUtils.setField(existing, "createdBy", "creator");
        ReflectionTestUtils.setField(existing, "lastModifiedBy", "creator");
        when(repository.findByCode("CUSTOMER")).thenReturn(Optional.of(existing));
        when(repository.saveAndFlush(any(TaskEntity.class))).thenAnswer(invocation -> {
            TaskEntity saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "lastModifiedBy", "importer");
            return saved;
        });

        List<Task> stored = service.seed(List.of(
                new CreateTaskRequest("CUSTOMER", "Customer", null)));

        assertEquals("Customer", stored.getFirst().label());
        assertEquals("CUSTOMER", stored.getFirst().code());
        assertEquals("creator", stored.getFirst().createdBy());
        assertEquals("importer", stored.getFirst().lastModifiedBy());
    }
}
