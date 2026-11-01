package fr.tiogars.domaintemplate.domains.task.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.tiogars.domaintemplate.domains.task.models.Task;
import fr.tiogars.domaintemplate.domains.task.services.TaskFindAllService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Exposes the endpoint that lists records in a domain.
 *
 * @since 0.1.0
 */
@Tag(name = "Tasks", description = "CRUD operations for the Task business object.")
@RestController
public class TasksListController {

    private final TaskFindAllService service;

    /**
     * Creates the domain query endpoint.
     *
     * @param service domain record listing service
     */
    public TasksListController(TaskFindAllService service) {
        this.service = service;
    }

    /**
     * Lists all records.
     *
     * @return matching records ordered by key and version
     */
    @Operation(summary = "List tasks", description = "Returns active tasks ordered by immutable code.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Records in the domain were returned.")
    })
    @GetMapping("/api/v1/task")
    public List<Task> listTask() {
        return service.findAll();
    }
}