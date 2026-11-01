package fr.tiogars.domaintemplate.domains.task.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.tiogars.domaintemplate.domains.task.entities.TaskEntity;
import fr.tiogars.domaintemplate.domains.task.models.Task;
import fr.tiogars.domaintemplate.domains.task.models.UpdateTaskRequest;
import fr.tiogars.domaintemplate.domains.task.repositories.TaskRepository;

/** Updates existing Tasks. */
@Service
public class TaskUpdateService {

    private final TaskRepository repository;

    @Autowired
    private TaskReferences references;

    @Autowired
    public TaskUpdateService(TaskRepository repository) {
        this.repository = repository;
    }

    /**
     * Updates the mutable fields of a Task.
     *
     * @param id      persistent identifier
     * @param request replacement business data
     * @return updated template, or an empty optional when it does not exist
     */
    @Transactional
    public Optional<Task> update(Long id, UpdateTaskRequest request) {
        return repository.findById(id)
                .map(existing -> {
                    existing.update(request.label(), request.description());
                    existing.attributes(request.status(), request.priority(), request.dueDate(), request.assignedTo());
                    if (references != null) {
                        references.apply(existing, request.tagIds(), request.nomenclatureId());
                    }
                    return toRecord(repository.saveAndFlush(existing));
                });
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
