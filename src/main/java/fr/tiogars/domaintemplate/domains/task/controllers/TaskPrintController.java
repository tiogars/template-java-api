package fr.tiogars.domaintemplate.domains.task.controllers;

import java.io.IOException;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.tiogars.domaintemplate.domains.task.models.PrintSelectedTasksRequest;
import fr.tiogars.domaintemplate.domains.task.services.TaskPrintService;
import fr.tiogars.domaintemplate.domains.task.services.TasksPrintAllService;
import fr.tiogars.domaintemplate.domains.task.services.TasksPrintSelectedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/** Exposes printable PDFs for single, all, and selected Tasks. */
@Tag(name = "Tasks", description = "CRUD operations for the Task business object.")
@RestController
@RequestMapping("/api/v1/task")
public class TaskPrintController {

    private final TaskPrintService printService;
    private final TasksPrintAllService printAllService;
    private final TasksPrintSelectedService printSelectedService;

    public TaskPrintController(TaskPrintService printService,
            TasksPrintAllService printAllService, TasksPrintSelectedService printSelectedService) {
        this.printService = printService;
        this.printAllService = printAllService;
        this.printSelectedService = printSelectedService;
    }

    @Operation(summary = "Print one Task as PDF")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Printable PDF."),
            @ApiResponse(responseCode = "404", description = "The template was not found."),
            @ApiResponse(responseCode = "422", description = "The PDF font does not support some record characters.")
    })
    @GetMapping(value = "/{id}/print.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> printTask(@PathVariable Long id) throws IOException {
        return printService.print(id)
                .map(bytes -> pdf(bytes, "task-" + id + ".pdf"))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Print all Tasks as PDF")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Printable PDF."),
            @ApiResponse(responseCode = "422", description = "The PDF font does not support some record characters.")
    })
    @GetMapping(value = "/print.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> printAllTasks() throws IOException {
        return pdf(printAllService.printAll(), "tasks.pdf");
    }

    @Operation(summary = "Print selected Tasks as PDF")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Printable PDF."),
            @ApiResponse(responseCode = "400", description = "Select between 1 and 1000 positive identifiers."),
            @ApiResponse(responseCode = "404", description = "One or more selected templates were not found."),
            @ApiResponse(responseCode = "422", description = "The PDF font does not support some record characters.")
    })
    @PostMapping(value = "/print-selected.pdf", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> printSelectedTasks(
            @Valid @RequestBody PrintSelectedTasksRequest request) throws IOException {
        return printSelectedService.printSelected(request.ids())
                .map(bytes -> pdf(bytes, "tasks-selected.pdf"))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private static ResponseEntity<byte[]> pdf(byte[] bytes, String filename) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(filename).build().toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(bytes);
    }
}
