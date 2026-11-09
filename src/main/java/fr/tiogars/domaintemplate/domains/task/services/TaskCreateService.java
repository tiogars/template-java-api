package fr.tiogars.domaintemplate.domains.task.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.tiogars.core.creation.services.CreationService;
import fr.tiogars.domaintemplate.domains.task.entities.TaskEntity;
import fr.tiogars.domaintemplate.domains.task.models.CreateTaskRequest;
import fr.tiogars.domaintemplate.domains.task.models.Task;
import fr.tiogars.domaintemplate.domains.task.repositories.TaskRepository;

/**
 * Creates Tasks when their business code is available.
 *
 * <p>Builds on the generic {@link CreationService} pipeline from the
 * {@code fr.tiogars:core} library: the duplicate lookup against the database
 * is delegated to {@link #findExisting(TaskCreation)} and the persistence
 * (reference resolution then save) to {@link #persist(TaskCreation)}. The
 * request/model mapping stays local to this service.
 *
 * @since 0.1.0
 */
@Service
public class TaskCreateService
        extends CreationService<TaskCreateService.TaskCreation> {

    /**
     * Pending creation unit carrying the entity and the reference identifiers
     * it still needs, so references are resolved only after the duplicate
     * check and immediately before the save.
     *
     * @param entity task entity to persist
     * @param tagIds identifiers of the tags to link, never null
     * @param nomenclatureId identifier of the nomenclature to link, or null
     */
    public record TaskCreation(
            TaskEntity entity, List<Long> tagIds, Long nomenclatureId) {
    }

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
        return createIfAbsent(toCreation(request))
                .map(creation -> toRecord(creation.entity()));
    }

    /**
     * Looks up a task already stored with the same business code.
     *
     * @param creation pending creation carrying the requested code
     * @return the stored task wrapped without pending references, if any
     */
    @Override
    protected Optional<TaskCreation> findExisting(TaskCreation creation) {
        return repository.findByCode(creation.entity().getCode())
                .map(existing -> new TaskCreation(existing, List.of(), null));
    }

    /**
     * Resolves the pending references, then saves and flushes the entity so
     * Spring Data auditing populates the audit fields.
     *
     * @param creation pending creation validated and absent from the storage
     * @return the creation unit carrying the persisted entity
     */
    @Override
    protected TaskCreation persist(TaskCreation creation) {
        TaskEntity entity = creation.entity();
        if (references != null) {
            references.apply(
                    entity, creation.tagIds(), creation.nomenclatureId());
        }
        return new TaskCreation(repository.saveAndFlush(entity),
                creation.tagIds(), creation.nomenclatureId());
    }

    private static TaskCreation toCreation(CreateTaskRequest request) {
        TaskEntity entity = new TaskEntity(
                request.code(), request.label(), request.description());
        entity.attributes(request.status(), request.priority(),
                request.dueDate(), request.assignedTo());
        return new TaskCreation(
                entity, request.tagIds(), request.nomenclatureId());
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
