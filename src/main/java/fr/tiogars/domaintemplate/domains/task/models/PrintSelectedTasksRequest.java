package fr.tiogars.domaintemplate.domains.task.models;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Identifies Tasks selected for PDF printing.
 *
 * @param ids persistent identifiers of the selected records
 * @since 0.1.0
 */
@Schema(description = "Identifiers of the Tasks selected for printing.")
public record PrintSelectedTasksRequest(
        @Schema(description = "Selected persistent identifiers.", example = "[1, 2, 3]",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty @Size(max = 1000) List<@NotNull @Positive Long> ids) {
}
