package fr.tiogars.domaintemplate.domains.task.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.tiogars.domaintemplate.domains.task.entities.TaskEntity;

/**
 * Provides persistence operations for Tasks.
 *
 * @since 0.1.0
 */
public interface TaskRepository extends JpaRepository<TaskEntity, Long> {

    /**
     * Finds all Tasks ordered by code.
     *
     * @return Tasks ordered by code in ascending order
     */
    List<TaskEntity> findAllByOrderByCodeAsc();

    /**
     * Finds selected active Tasks ordered by code.
     *
     * @param ids persistent identifiers
     * @return matching active Tasks
     */
    List<TaskEntity> findAllByIdInOrderByCodeAsc(List<Long> ids);

    /**
     * Finds a Task by its code.
     *
     * @param code Task code
     * @return matching Task, or an empty optional when none exists
     */
    Optional<TaskEntity> findByCode(String code);
}
