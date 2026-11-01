package fr.tiogars.domaintemplate.settings.user.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import fr.tiogars.domaintemplate.settings.user.entities.UserSettings;
import fr.tiogars.domaintemplate.settings.user.models.UpdateUserSettingsRequest;
import fr.tiogars.domaintemplate.settings.user.services.UserSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/** Exposes the administrator endpoint that updates the settings of a USER account. */
@Tag(name = "User settings", description = "User and ANONYMOUS role settings operations.")
@RestController
public class UserSettingsUpdateController {

    private final UserSettingsService service;

    UserSettingsUpdateController(UserSettingsService service) {
        this.service = service;
    }

    @Operation(summary = "Update the settings of a user account holding the USER role")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/api/v1/settings/user/users/{userId}")
    ResponseEntity<UserSettings> updateUserSettings(
            @PathVariable Long userId, @Valid @RequestBody UpdateUserSettingsRequest request) {
        return service.updateByUserId(userId, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}