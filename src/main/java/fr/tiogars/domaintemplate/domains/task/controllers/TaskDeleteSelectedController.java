package fr.tiogars.domaintemplate.domains.task.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import fr.tiogars.domaintemplate.common.models.DeleteSelectedRequest;
import fr.tiogars.domaintemplate.common.models.DeleteSelectedResponse;
import fr.tiogars.domaintemplate.domains.task.services.TaskDeleteSelectedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Exposes the endpoint that archives a selection of domain records.
 *
 * @since 0.1.0
 */
@Tag(name = "Tasks", description = "CRUD operations for the Task business object.")
@RestController
public class TaskDeleteSelectedController {

    private final TaskDeleteSelectedService service;

    /**
     * Creates the bulk delete endpoint.
     *
     * @param service bulk deletion service
     */
    public TaskDeleteSelectedController(TaskDeleteSelectedService service) {
        this.service = service;
    }

    /**
     * Archives every selected active record.
     *
     * @param request selected identifiers
     * @return deleted and skipped identifiers
     */
    @Operation(
            summary = "Soft-delete selected Tasks",
            description = "Archives every selected active record; unknown identifiers are reported as skipped.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The selection was processed."),
            @ApiResponse(responseCode = "400", description = "The selection is empty or invalid.")
    })
    @PostMapping("/api/v1/task/delete-selected")
    public DeleteSelectedResponse deleteSelectedTask(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Identifiers of the records to delete.", required = true)
            @Valid @RequestBody DeleteSelectedRequest request) {
        return service.deleteSelected(request.ids());
    }
}
