package fr.tiogars.domaintemplate.domains.task.controllers;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import fr.tiogars.domaintemplate.domains.task.models.CreateTaskRequest;
import fr.tiogars.domaintemplate.domains.task.models.Task;
import fr.tiogars.domaintemplate.domains.task.services.TaskCreateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Exposes the endpoint that creates domain records.
 *
 * @since 0.1.0
 */
@Tag(name = "Tasks", description = "CRUD operations for the Task business object.")
@RestController
public class TaskCreateController {

        private final TaskCreateService service;

        /**
         * Creates the record creation endpoint.
         *
         * @param service domain record creation service
         */
        public TaskCreateController(TaskCreateService service) {
                this.service = service;
        }

        /**
         * Creates a new version of a domain record.
         *
         * @param request validated version and payload to create
         * @return a created response containing the record, or a conflict response when
         *         the version exists
         */
        @Operation(summary = "Create a task", description = "Stores a task with an immutable, active-unique code.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "The record version was created."),
                        @ApiResponse(responseCode = "400", description = "The record version or payload is invalid."),
                        @ApiResponse(responseCode = "409", description = "A record with the same domain, key, and version already exists.")
        })
        @PostMapping("/api/v1/task")
        public ResponseEntity<Task> createTask(
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                description = "Business data of the task to create.", required = true)
                        @Valid @RequestBody CreateTaskRequest request) {
                return service.create(request)
                                .map(created -> ResponseEntity
                                                .created(URI.create("/api/v1/task/" + created.id()))
                                                .body(created))
                                .orElseGet(() -> ResponseEntity.status(409).build());
        }
}