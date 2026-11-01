package fr.tiogars.domaintemplate.domains.auth.user.entities;

import java.time.Instant;
import java.util.List;

import fr.tiogars.domaintemplate.domains.auth.role.entities.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/** Public user account representation. */
public record User(
        @Schema(description = "Generated persistent identifier.", example = "1")
        @NotNull
        Long id,
        @Schema(description = "Unique login name.", example = "admin")
        @NotNull
        String username,
        @Schema(
                description = "Whether this account can authenticate.",
                example = "true",
                requiredMode = Schema.RequiredMode.REQUIRED)
        boolean enabled,
        @Schema(description = "Roles assigned to this account.")
        @NotNull
        List<Role> roles,
        @Schema(description = "Creation timestamp.")
        @NotNull
        Instant createdDate,
        @Schema(description = "Last modification timestamp.")
        @NotNull
        Instant lastModifiedDate) {
}