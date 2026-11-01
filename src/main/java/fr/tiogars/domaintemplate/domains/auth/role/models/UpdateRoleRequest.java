package fr.tiogars.domaintemplate.domains.auth.role.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** Request body used to update a role. */
public record UpdateRoleRequest(
        @Schema(
                description = "Display label.",
                example = "Administrator",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String label) {
}