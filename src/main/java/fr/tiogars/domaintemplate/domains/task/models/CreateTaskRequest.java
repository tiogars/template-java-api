package fr.tiogars.domaintemplate.domains.task.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** Defines the business data required to create a Task. */
@Schema(description = "Business data required to create a Task.")
public record CreateTaskRequest(
        @Schema(example = "CUSTOMER", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String code,
        @Schema(example = "Customer", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String label,
        @Schema(example = "Customer Task")
        String description,
        TaskStatus status,
        TaskPriority priority,
        java.time.LocalDate dueDate,
        String assignedTo,
        java.util.List<@jakarta.validation.constraints.NotNull @jakarta.validation.constraints.Positive Long> tagIds,
        @jakarta.validation.constraints.Positive Long nomenclatureId
) {
    public CreateTaskRequest {
        status = status == null ? TaskStatus.TODO : status;
        priority = priority == null ? TaskPriority.NORMAL : priority;
        tagIds = tagIds == null ? java.util.List.of() : java.util.List.copyOf(tagIds);
    }

    public CreateTaskRequest(String code, String label, String description) {
        this(code, label, description, TaskStatus.TODO, TaskPriority.NORMAL, null, null, java.util.List.of(), null);
    }
}