package fr.tiogars.domaintemplate.domains.task.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.tiogars.domaintemplate.common.models.DeleteSelectedResponse;
import fr.tiogars.domaintemplate.common.services.DeleteSelectedSupport;

/**
 * Soft-deletes a selection of Tasks in a single transaction.
 *
 * @since 0.1.0
 */
@Service
public class TaskDeleteSelectedService {

    private final TaskDeleteService deleteService;

    /**
     * Creates the bulk deletion service.
     *
     * @param deleteService single Task deletion service
     */
    public TaskDeleteSelectedService(TaskDeleteService deleteService) {
        this.deleteService = deleteService;
    }

    /**
     * Soft-deletes the selected Tasks.
     *
     * @param ids selected Task identifiers
     * @return deleted and skipped identifiers
     */
    @Transactional
    public DeleteSelectedResponse deleteSelected(List<Long> ids) {
        return DeleteSelectedSupport.deleteEach(ids, deleteService::delete);
    }
}
