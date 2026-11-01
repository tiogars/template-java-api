package fr.tiogars.domaintemplate.domains.task.services;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.tiogars.domaintemplate.config.SecurityAuditorAware;
import fr.tiogars.domaintemplate.domains.task.entities.TaskEntity;
import fr.tiogars.domaintemplate.domains.task.repositories.TaskRepository;

@ExtendWith(MockitoExtension.class)
public class TaskDeleteServiceTest {

    @Mock
    private TaskRepository repository;

    @Mock
    private SecurityAuditorAware auditor;

    @Test
    public void delete_reportsWhetherTemplateExists() {
        Instant timestamp = Instant.parse("2026-10-03T12:00:00.123456Z");
        TaskDeleteService service = new TaskDeleteService(
                repository, auditor, Clock.fixed(timestamp, ZoneOffset.UTC));
        TaskEntity entity = new TaskEntity("CUSTOMER", "Customer", null);
        when(repository.findById(42L)).thenReturn(Optional.of(entity));
        when(repository.findById(99L)).thenReturn(Optional.empty());
        when(auditor.getCurrentAuditor()).thenReturn(Optional.of("operator"));

        assertTrue(service.delete(42L));
        assertFalse(service.delete(99L));
        assertEquals(timestamp, entity.getDeletedDate());
        assertEquals("operator", entity.getDeletedBy());
        verify(repository).saveAndFlush(entity);
        verify(repository, never()).deleteById(any());
        verify(repository, never()).delete(any());
    }
}
