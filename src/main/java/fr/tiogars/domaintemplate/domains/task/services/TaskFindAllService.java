package fr.tiogars.domaintemplate.domains.task.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.tiogars.domaintemplate.domains.task.entities.TaskEntity;
import fr.tiogars.domaintemplate.domains.task.models.Task;
import fr.tiogars.domaintemplate.domains.task.repositories.TaskRepository;

/** Retrieves all Tasks. */
@Service
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public class TaskFindAllService {

    private final TaskRepository repository;

    @Autowired
    public TaskFindAllService(TaskRepository repository) {
        this.repository = repository;
    }

    /**
     * Lists all templates ordered by business code.
     *
     * @return templates ordered by code
     */
    public List<Task> findAll() {
        return repository.findAllByOrderByCodeAsc().stream()
                .map(TaskFindAllService::toRecord)
                .toList();
    }

    static Task toRecord(TaskEntity entity) {
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
