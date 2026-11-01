package fr.tiogars.domaintemplate.domains.task.services;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Generates a printable PDF for all active Tasks. */
@Service
public class TasksPrintAllService {

    private final TaskFindAllService findAllService;
    private final TaskPdfRenderer renderer;

    public TasksPrintAllService(TaskFindAllService findAllService, TaskPdfRenderer renderer) {
        this.findAllService = findAllService;
        this.renderer = renderer;
    }

    /**
     * Prints all active records ordered by code.
     *
     * @return PDF bytes, including an empty-list message when there are no records
     * @throws IOException when the PDF cannot be generated
     */
    @Transactional(readOnly = true)
    public byte[] printAll() throws IOException {
        return renderer.render("Tasks", findAllService.findAll());
    }
}
