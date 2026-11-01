package fr.tiogars.domaintemplate.domains.task.models;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Represents an business object managed by the template API.
 *
 * @param id               generated persistent identifier
 * @param code             unique business code
 * @param label            display label
 * @param description      optional description
 * @param createdDate      creation timestamp
 * @param lastModifiedDate last modification timestamp
 * @param createdBy        username at creation
 * @param lastModifiedBy   username at the last modification
 * @since 0.1.0
 */
@Schema(description = "An business object persisted by the template API.")
public record Task(
        @Schema(description = "Generated persistent identifier.", example = "42", accessMode = Schema.AccessMode.READ_ONLY)
        @NotNull Long id,
        @Schema(description = "Unique immutable business code.", example = "CUSTOMER", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String code,
        @Schema(description = "Display label.", example = "Customer", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank String label,
        @Schema(description = "Optional business description.", example = "Customer Task") String description,
        @Schema(description = "UTC creation instant.", format = "date-time", accessMode = Schema.AccessMode.READ_ONLY)
        @NotNull Instant createdDate,
        @Schema(description = "UTC instant of the last modification.", format = "date-time", accessMode = Schema.AccessMode.READ_ONLY)
        @NotNull Instant lastModifiedDate,
        @Schema(description = "Username at creation.", accessMode = Schema.AccessMode.READ_ONLY) @NotNull String createdBy,
        @Schema(description = "Username at the last modification.", accessMode = Schema.AccessMode.READ_ONLY)
        @NotNull String lastModifiedBy,
        TaskStatus status, TaskPriority priority, java.time.LocalDate dueDate, String assignedTo,
        java.util.List<Long> tagIds, Long nomenclatureId) {
    public Task(Long id, String code, String label, String description, Instant createdDate,
            Instant lastModifiedDate, String createdBy, String lastModifiedBy) {
        this(id, code, label, description, createdDate, lastModifiedDate, createdBy, lastModifiedBy,
                TaskStatus.TODO, TaskPriority.NORMAL, null, null, java.util.List.of(), null);
    }
}