package fr.tiogars.domaintemplate.domains.auth.user.models;

import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

/** Request body used to create a user account. */
public record CreateUserRequest(
        @Schema(
                description = "Unique login name.",
                example = "operator",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String username,
        @Schema(
                description = "Plain password, stored only as a BCrypt hash.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String password,
        @Schema(description = "Whether this account can authenticate.", example = "true")
        Boolean enabled,
        @Schema(
                description = "Role codes assigned to this account.",
                example = "[\"USER\"]",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty
        Set<@NotBlank String> roles) {
}