package fr.tiogars.domaintemplate.common.models;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Identifies the records selected for a bulk deletion.
 *
 * @param ids persistent identifiers of the selected records
 * @since 0.1.0
 */
@Schema(description = "Identifiers of the records selected for deletion.")
public record DeleteSelectedRequest(
        @Schema(description = "Persistent identifiers of the selected records.", example = "[1, 2, 3]", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty @Size(max = 1000) List<@NotNull Long> ids) {
}
