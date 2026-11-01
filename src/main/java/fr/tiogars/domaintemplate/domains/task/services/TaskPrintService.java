package fr.tiogars.domaintemplate.domains.task.services;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.tiogars.domaintemplate.domains.task.models.Task;

/** Generates a printable PDF for one active Task. */
@Service
public class TaskPrintService {

    private final TaskFindByIdService findByIdService;
    private final TaskPdfRenderer renderer;

    public TaskPrintService(TaskFindByIdService findByIdService, TaskPdfRenderer renderer) {
        this.findByIdService = findByIdService;
        this.renderer = renderer;
    }

    /**
     * Prints an active record.
     *
     * @param id persistent identifier
     * @return PDF bytes, or empty if the record was not found
     * @throws IOException when the PDF cannot be generated
     */
    @Transactional(readOnly = true)
    public Optional<byte[]> print(Long id) throws IOException {
        Optional<Task> template = findByIdService.findById(id);
        if (template.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(renderer.render("Task", List.of(template.get())));
    }
}
