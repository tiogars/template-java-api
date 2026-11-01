package fr.tiogars.domaintemplate.domains.task.services;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.tiogars.domaintemplate.config.SecurityAuditorAware;
import fr.tiogars.domaintemplate.domains.task.repositories.TaskRepository;

/** Archives Tasks without removing their database rows. */
@Service
public class TaskDeleteService {

    private final TaskRepository repository;
    private final SecurityAuditorAware auditor;
    private final Clock clock;

    @Autowired
    public TaskDeleteService(TaskRepository repository, SecurityAuditorAware auditor, Clock clock) {
        this.repository = repository;
        this.auditor = auditor;
        this.clock = clock;
    }

    /**
     * Soft-deletes an active Task.
     *
     * @param id persistent identifier
     * @return {@code true} when the template existed
     */
    @Transactional
    public boolean delete(Long id) {
        return repository.findById(id).map(entity -> {
            entity.softDelete(Instant.now(clock).truncatedTo(ChronoUnit.MICROS),
                    auditor.getCurrentAuditor().orElseThrow());
            repository.saveAndFlush(entity);
            return true;
        }).orElse(false);
    }
}
