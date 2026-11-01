package fr.tiogars.domaintemplate.settings.user.entities;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/** Public user settings representation. */
public record UserSettings(
        @Schema(description = "Owner user identifier. Null for role settings such as ANONYMOUS.", example = "2")
        Long userId,
        @Schema(description = "Owner role code. Null for user settings.", example = "ANONYMOUS")
        String roleCode,
        @Schema(
                description = "Preferred view mode.",
                example = "system",
                allowableValues = {"light", "dark", "system"},
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        String viewMode,
        @Schema(description = "Creation timestamp. Null while default settings have never been saved.")
        Instant createdDate,
        @Schema(description = "Last modification timestamp. Null while default settings have never been saved.")
        Instant lastModifiedDate) {
}
