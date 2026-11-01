package fr.tiogars.domaintemplate.domains.auth.user.models;

import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/** Request body used to update a user account. */
public record UpdateUserRequest(
        @Schema(
                description = "Unique login name.",
                example = "operator",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String username,
        @Schema(description = "Replacement plain password. Omit or leave blank to keep the current password.")
        String password,
        @Schema(
                description = "Whether this account can authenticate.",
                example = "true",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        Boolean enabled,
        @Schema(
                description = "Role codes assigned to this account.",
                example = "[\"USER\"]",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty
        Set<@NotBlank String> roles) {
}