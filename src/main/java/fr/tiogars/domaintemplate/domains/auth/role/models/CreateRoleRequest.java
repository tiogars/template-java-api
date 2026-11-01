package fr.tiogars.domaintemplate.domains.auth.role.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** Request body used to create a role. */
public record CreateRoleRequest(
        @Schema(description = "Stable role code.", example = "MANAGER", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank String code,
        @Schema(description = "Display label.", example = "Manager", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank String label) {
}