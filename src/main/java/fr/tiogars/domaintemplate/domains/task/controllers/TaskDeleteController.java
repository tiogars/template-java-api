package fr.tiogars.domaintemplate.domains.task.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import fr.tiogars.domaintemplate.domains.task.services.TaskDeleteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Exposes the endpoint that archives a domain record.
 *
 * @since 0.1.0
 */
@Tag(name = "Tasks", description = "CRUD operations for the Task business object.")
@RestController
public class TaskDeleteController {

    private final TaskDeleteService service;

    /**
     * Creates the delete endpoint.
     *
     * @param service domain record deletion service
     */
    public TaskDeleteController(TaskDeleteService service) {
        this.service = service;
    }

    /**
     * Archives an active record, preserving its database row and business code.
     *
     * @param id persistent template identifier
     * @return a no-content response when a record was archived, or a not-found response otherwise
     */
        @Operation(
            summary = "Soft-delete a Task",
            description = "Archives the row and allows its code to be reused with a new identifier."
        )
        @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The record was archived."),
            @ApiResponse(responseCode = "404", description = "No active record exists for the supplied identifier.")
        })
        @DeleteMapping("/api/v1/task/{id}")
        public ResponseEntity<Void> deleteTask(
            @Parameter(description = "Persistent template identifier.", required = true, example = "42")
            @PathVariable Long id) {
        return service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}