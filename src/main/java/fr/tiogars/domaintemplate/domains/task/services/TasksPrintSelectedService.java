package fr.tiogars.domaintemplate.domains.task.services;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.tiogars.domaintemplate.domains.task.models.Task;
import fr.tiogars.domaintemplate.domains.task.repositories.TaskRepository;

/** Generates a printable PDF for selected active Tasks. */
@Service
public class TasksPrintSelectedService {

    private final TaskRepository repository;
    private final TaskPdfRenderer renderer;

    public TasksPrintSelectedService(TaskRepository repository, TaskPdfRenderer renderer) {
        this.repository = repository;
        this.renderer = renderer;
    }

    /**
     * Prints a selection ordered by code without duplicating records.
     *
     * @param ids selected persistent identifiers
     * @return PDF bytes, or empty if any selected record was not found
     * @throws IOException when the PDF cannot be generated
     */
    @Transactional(readOnly = true)
    public Optional<byte[]> printSelected(List<Long> ids) throws IOException {
        if (ids.isEmpty()) {
            throw new IllegalArgumentException("Select at least one Task to print.");
        }
        List<Long> uniqueIds = ids.stream().distinct().toList();
        List<Task> templates = repository.findAllByIdInOrderByCodeAsc(uniqueIds).stream()
                .map(TaskFindAllService::toRecord)
                .toList();
        if (templates.size() != uniqueIds.size()) {
            return Optional.empty();
        }
        return Optional.of(renderer.render("Selected Tasks", templates));
    }
}
