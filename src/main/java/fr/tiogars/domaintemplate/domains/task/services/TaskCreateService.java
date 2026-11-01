package fr.tiogars.domaintemplate.domains.task.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.tiogars.domaintemplate.domains.task.entities.TaskEntity;
import fr.tiogars.domaintemplate.domains.task.models.CreateTaskRequest;
import fr.tiogars.domaintemplate.domains.task.models.Task;
import fr.tiogars.domaintemplate.domains.task.repositories.TaskRepository;

/**
 * Creates Tasks when their business code is available.
 *
 * @since 0.1.0
 */
@Service
public class TaskCreateService {

    private final TaskRepository repository;

    @Autowired
    private TaskReferences references;

    /**
     * Creates a service using Spring Data auditing.
     *
     * @param repository domain record repository
     */
    @Autowired
    public TaskCreateService(TaskRepository repository) {
        this.repository = repository;
    }

    /**
     * Creates a Task when its code does not already exist.
     *
     * @param request business data to create
     * @return created template, or an empty optional when the code exists
     */
    @Transactional
    public Optional<Task> create(CreateTaskRequest request) {
        if (repository.findByCode(request.code()).isPresent()) {
            return Optional.empty();
        }
        TaskEntity entity = new TaskEntity(
                request.code(), request.label(), request.description());
        entity.attributes(request.status(), request.priority(), request.dueDate(), request.assignedTo());
        if (references != null) {
            references.apply(entity, request.tagIds(), request.nomenclatureId());
        }
        return Optional.of(toRecord(repository.saveAndFlush(entity)));
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
