package fr.tiogars.domaintemplate.domains.task.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import fr.tiogars.domaintemplate.domains.task.models.CreateTaskRequest;
import fr.tiogars.domaintemplate.domains.task.models.Task;
import fr.tiogars.domaintemplate.domains.task.services.TaskSeedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Exposes the endpoint that seeds a collection of domain records.
 *
 * @since 0.1.0
 */
@Tag(name = "Tasks", description = "CRUD operations for the Task business object.")
@RestController
public class TaskSeedController {

        private final TaskSeedService service;

        /**
         * Creates the seed endpoint.
         *
         * @param service domain record seed service
         */
        public TaskSeedController(TaskSeedService service) {
                this.service = service;
        }

        /**
         * Creates or replaces the supplied records.
         *
         * @param records validated records to seed
         * @return stored records in input order
         */
        @Operation(summary = "Seed domain records", description = "Creates or replaces every supplied record version in a single request.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "The supplied records were stored."),
                        @ApiResponse(responseCode = "400", description = "One or more supplied records are invalid.")
        })
        @PostMapping("/api/v1/task/seed")
        @ResponseStatus(HttpStatus.CREATED)
        public List<Task> seedTask(
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                description = "Tasks to create or replace by immutable code.", required = true)
                        @RequestBody List<@Valid CreateTaskRequest> records) {
                return service.seed(records);
        }
}