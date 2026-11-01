package fr.tiogars.domaintemplate.domains.auth.models;

import java.time.Instant;

import fr.tiogars.domaintemplate.domains.auth.user.entities.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/** JWT response returned after successful authentication. */
public record LoginResponse(
        @Schema(description = "JWT access token.") @NotNull String token,
        @Schema(description = "Token type.", example = "Bearer") @NotNull String tokenType,
        @Schema(description = "Token expiration timestamp.") @NotNull Instant expiresAt,
        @Schema(description = "Authenticated user.") @NotNull User user) {
}