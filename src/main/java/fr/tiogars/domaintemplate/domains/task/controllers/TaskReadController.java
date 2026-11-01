package fr.tiogars.domaintemplate.domains.task.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import fr.tiogars.domaintemplate.domains.task.models.Task;
import fr.tiogars.domaintemplate.domains.task.services.TaskFindByIdService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/** Exposes lookup of one Task by identifier. */
@Tag(name = "Tasks", description = "CRUD operations for the Task business object.")
@RestController
public class TaskReadController {

    private final TaskFindByIdService service;

    public TaskReadController(TaskFindByIdService service) {
        this.service = service;
    }

    @Operation(summary = "Get a Task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The template was returned."),
            @ApiResponse(responseCode = "404", description = "No template exists for the supplied identifier.")
    })
    @GetMapping("/api/v1/task/{id}")
    public ResponseEntity<Task> readTask(
            @Parameter(description = "Persistent template identifier.", required = true, example = "42") @PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}