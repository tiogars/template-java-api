package fr.tiogars.domaintemplate.domains.task.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.tiogars.domaintemplate.domains.task.entities.TaskEntity;
import fr.tiogars.domaintemplate.domains.task.models.CreateTaskRequest;
import fr.tiogars.domaintemplate.domains.task.models.Task;
import fr.tiogars.domaintemplate.domains.task.repositories.TaskRepository;

/** Creates or updates Tasks from seed data. */
@Service
public class TaskSeedService {

    private final TaskRepository repository;

    @Autowired
    private TaskReferences references;

    @Autowired
    public TaskSeedService(TaskRepository repository) {
        this.repository = repository;
    }

    /**
     * Creates or updates templates by immutable business code.
     *
     * @param requests business data to seed
     * @return stored templates in input order
     */
    @Transactional
    public List<Task> seed(List<CreateTaskRequest> requests) {
        List<Task> storedTemplates = new ArrayList<>();
        for (CreateTaskRequest request : requests) {
            TaskEntity entity = repository.findByCode(request.code())
                    .map(existing -> {
                        existing.update(request.label(), request.description());
                        return existing;
                    })
                    .orElseGet(() -> new TaskEntity(
                            request.code(), request.label(), request.description()));
            entity.attributes(request.status(), request.priority(), request.dueDate(), request.assignedTo());
            if (references != null) {
                references.apply(entity, request.tagIds(), request.nomenclatureId());
            }
            storedTemplates.add(toRecord(repository.saveAndFlush(entity)));
        }
        return storedTemplates;
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
