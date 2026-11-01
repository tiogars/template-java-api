package fr.tiogars.domaintemplate.domains.auth.role.entities;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/** Public role representation. */
public record Role(
        @Schema(description = "Generated persistent identifier.", example = "1") @NotNull Long id,
        @Schema(description = "Stable role code.", example = "ADMIN") @NotNull String code,
        @Schema(description = "Display label.", example = "Administrator") @NotNull String label,
        @Schema(description = "Creation timestamp.") @NotNull Instant createdDate,
        @Schema(description = "Last modification timestamp.") @NotNull Instant lastModifiedDate) {
}