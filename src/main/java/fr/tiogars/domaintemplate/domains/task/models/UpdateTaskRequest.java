package fr.tiogars.domaintemplate.domains.task.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** Defines the mutable business data of a Task. */
@Schema(description = "Mutable business data of a Task.")
public record UpdateTaskRequest(
        @Schema(example = "Customer", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String label,
        @Schema(example = "Updated customer Task")
        String description,
        TaskStatus status,
        TaskPriority priority,
        java.time.LocalDate dueDate,
        String assignedTo,
        java.util.List<@jakarta.validation.constraints.NotNull @jakarta.validation.constraints.Positive Long> tagIds,
        @jakarta.validation.constraints.Positive Long nomenclatureId
) {
    public UpdateTaskRequest {
        status = status == null ? TaskStatus.TODO : status;
        priority = priority == null ? TaskPriority.NORMAL : priority;
        tagIds = tagIds == null ? java.util.List.of() : java.util.List.copyOf(tagIds);
    }

    public UpdateTaskRequest(String label, String description) {
        this(label, description, TaskStatus.TODO, TaskPriority.NORMAL, null, null, java.util.List.of(), null);
    }
}