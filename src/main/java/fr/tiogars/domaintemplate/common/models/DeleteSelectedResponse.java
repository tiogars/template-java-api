package fr.tiogars.domaintemplate.common.models;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Reports the outcome of a bulk deletion.
 *
 * @param deletedIds identifiers that were deleted
 * @param skippedIds identifiers that were not found or are protected from deletion
 * @since 0.1.0
 */
@Schema(description = "Outcome of a bulk deletion.")
public record DeleteSelectedResponse(
        @Schema(description = "Identifiers that were deleted.", requiredMode = Schema.RequiredMode.REQUIRED) List<Long> deletedIds,
        @Schema(description = "Identifiers that were not found or are protected from deletion.", requiredMode = Schema.RequiredMode.REQUIRED) List<Long> skippedIds) {
}
