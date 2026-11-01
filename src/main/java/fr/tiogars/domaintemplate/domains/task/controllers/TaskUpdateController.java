package fr.tiogars.domaintemplate.domains.task.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import fr.tiogars.domaintemplate.domains.task.models.Task;
import fr.tiogars.domaintemplate.domains.task.models.UpdateTaskRequest;
import fr.tiogars.domaintemplate.domains.task.services.TaskUpdateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Exposes the endpoint that updates domain records.
 *
 * @since 0.1.0
 */
@Tag(name = "Tasks", description = "CRUD operations for the Task business object.")
@RestController
public class TaskUpdateController {

        private final TaskUpdateService service;

        /**
         * Creates the record update endpoint.
         *
         * @param service domain record update service
         */
        public TaskUpdateController(TaskUpdateService service) {
                this.service = service;
        }

        /**
         * Replaces the payload of an existing record version.
         *
         * @param id      persistent template identifier
         * @param request validated version and replacement payload
         * @return an OK response containing the record, or a not-found response when
         *         the version is absent
         */
        @Operation(summary = "Update a task", description = "Replaces mutable task attributes without changing its code.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "The record version was updated."),
                        @ApiResponse(responseCode = "400", description = "The record version or payload is invalid."),
                        @ApiResponse(responseCode = "404", description = "No record exists for the supplied domain, key, and version.")
        })
        @PutMapping("/api/v1/task/{id}")
        public ResponseEntity<Task> updateTask(
                        @Parameter(description = "Persistent template identifier.", required = true, example = "42") @PathVariable Long id,
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                description = "Replacement task attributes.", required = true)
                        @Valid @RequestBody UpdateTaskRequest request) {
                return service.update(id, request)
                                .map(ResponseEntity::ok)
                                .orElseGet(() -> ResponseEntity.notFound().build());
        }
}