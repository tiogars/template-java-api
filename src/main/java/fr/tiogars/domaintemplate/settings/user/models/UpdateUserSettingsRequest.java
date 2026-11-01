package fr.tiogars.domaintemplate.settings.user.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/** Request body used to update user settings. */
public record UpdateUserSettingsRequest(
        @Schema(
                description = "Preferred view mode.",
                example = "dark",
                allowableValues = {"light", "dark", "system"},
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @Pattern(regexp = "light|dark|system")
        String viewMode) {
}
