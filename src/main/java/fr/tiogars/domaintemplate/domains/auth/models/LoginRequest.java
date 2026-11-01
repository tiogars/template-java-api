package fr.tiogars.domaintemplate.domains.auth.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** Credentials used to request a JWT. */
public record LoginRequest(
        @Schema(description = "Login name.", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank String username,
        @Schema(description = "Plain password.", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank String password) {
}