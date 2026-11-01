package fr.tiogars.domaintemplate.domains.task.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import fr.tiogars.domaintemplate.domains.task.entities.TaskEntity;
import fr.tiogars.domaintemplate.domains.task.models.Task;
import fr.tiogars.domaintemplate.domains.task.repositories.TaskRepository;

@ExtendWith(MockitoExtension.class)
public class TaskFindAllServiceTest {

    @Mock
    private TaskRepository repository;

    @Test
    public void findAll_returnsTemplatesInRepositoryOrder() {
        TaskEntity customer = new TaskEntity("CUSTOMER", "Customer", null);
        ReflectionTestUtils.setField(customer, "createdBy", "creator");
        ReflectionTestUtils.setField(customer, "lastModifiedBy", "editor");
        when(repository.findAllByOrderByCodeAsc()).thenReturn(List.of(
                customer,
                new TaskEntity("ORDER", "Order", null)));

        List<Task> templates = new TaskFindAllService(repository).findAll();

        assertEquals(List.of("CUSTOMER", "ORDER"), templates.stream().map(Task::code).toList());
        assertEquals("creator", templates.getFirst().createdBy());
        assertEquals("editor", templates.getFirst().lastModifiedBy());
    }
}
