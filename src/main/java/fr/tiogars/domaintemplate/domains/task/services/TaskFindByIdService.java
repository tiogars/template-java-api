package fr.tiogars.domaintemplate.domains.task.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.tiogars.domaintemplate.domains.task.entities.TaskEntity;
import fr.tiogars.domaintemplate.domains.task.models.Task;
import fr.tiogars.domaintemplate.domains.task.repositories.TaskRepository;

/** Retrieves a Task by its persistent identifier. */
@Service
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public class TaskFindByIdService {

    private final TaskRepository repository;

    @Autowired
    public TaskFindByIdService(TaskRepository repository) {
        this.repository = repository;
    }

    /**
     * Finds a Task by identifier.
     *
     * @param id persistent identifier
     * @return matching template, or an empty optional when none exists
     */
    public Optional<Task> findById(Long id) {
        return repository.findById(id)
                .map(TaskFindByIdService::toRecord);
    }

    private static Task toRecord(TaskEntity entity) {
        return new Task(
                entity.getId(),
                entity.getCode(),
                entity.getLabel(),
                entity.getDescription(),
                entity.getCreatedDate(),
                entity.getLastModifiedDate(),
                entity.getCreatedBy(),
                entity.getLastModifiedBy(),
                entity.getStatus(), entity.getPriority(), entity.getDueDate(), entity.getAssignedTo(),
                entity.getTagIds(), entity.getNomenclatureId());
    }
}
