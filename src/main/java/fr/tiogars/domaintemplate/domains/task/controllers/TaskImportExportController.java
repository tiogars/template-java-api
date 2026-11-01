package fr.tiogars.domaintemplate.domains.task.controllers;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import fr.tiogars.domaintemplate.domains.task.models.CreateTaskRequest;
import fr.tiogars.domaintemplate.domains.task.models.Task;
import fr.tiogars.domaintemplate.domains.task.services.TaskCsvCodec;
import fr.tiogars.domaintemplate.domains.task.services.TaskFindAllService;
import fr.tiogars.domaintemplate.domains.task.services.TaskSeedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Exposes JSON and CSV import/export endpoints for Tasks.
 *
 * @since 0.1.0
 */
@Tag(name = "Tasks", description = "CRUD operations for the Task business object.")
@RestController
@RequestMapping("/api/v1/task")
public class TaskImportExportController {

    private final TaskFindAllService findAllService;
    private final TaskSeedService seedService;
    private final TaskCsvCodec csvCodec;

    /**
     * Creates the import/export controller.
     *
     * @param findAllService domain record listing service
     * @param seedService    domain record seed service
     * @param csvCodec       CSV codec
     */
    public TaskImportExportController(
            TaskFindAllService findAllService,
            TaskSeedService seedService,
            TaskCsvCodec csvCodec) {
        this.findAllService = findAllService;
        this.seedService = seedService;
        this.csvCodec = csvCodec;
    }

    /**
     * Exports records as JSON.
     *
     * @return domain records
     */
    @Operation(summary = "Export domain records as JSON")
    @ApiResponse(responseCode = "200", description = "Domain records were exported.")
    @GetMapping(value = "/export.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Task> exportJsonTask() {
        return findAllService.findAll();
    }

    /**
     * Exports records as CSV.
     *
     * @return CSV response
     */
    @Operation(summary = "Export domain records as CSV")
    @ApiResponse(responseCode = "200", description = "Domain records were exported.")
    @GetMapping(value = "/export.csv", produces = "text/csv")
    public ResponseEntity<String> exportCsvTask() {
        String filename = "tasks.csv";
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename).build().toString())
                .body(csvCodec.serialize(findAllService.findAll()));
    }

    /**
     * Imports JSON records.
     *
     * @param records records to import
     * @return stored records
     */
    @Operation(summary = "Import domain records from JSON")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Records were imported."),
            @ApiResponse(responseCode = "400", description = "The import document is invalid.")
    })
    @PostMapping(value = "/import.json", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public List<Task> importJsonTask(
            @RequestBody List<@Valid CreateTaskRequest> records) {
        return seedService.seed(records);
    }

    /**
     * Imports CSV records.
     *
     * @param csv CSV document
     * @return stored records
     */
    @Operation(summary = "Import domain records from CSV")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Records were imported."),
            @ApiResponse(responseCode = "400", description = "The import document is invalid.")
    })
    @PostMapping(value = "/import.csv", consumes = "text/csv")
    @ResponseStatus(HttpStatus.CREATED)
    public List<Task> importCsvTask(@RequestBody String csv) {
        try {
            return seedService.seed(csvCodec.parse(csv));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }
}