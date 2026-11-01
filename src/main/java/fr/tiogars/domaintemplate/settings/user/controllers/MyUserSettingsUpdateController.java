package fr.tiogars.domaintemplate.settings.user.controllers;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
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

/** Exposes the endpoint that updates the settings of the authenticated account. */
@Tag(name = "User settings", description = "User and ANONYMOUS role settings operations.")
@RestController
public class MyUserSettingsUpdateController {

    private final UserSettingsService service;

    MyUserSettingsUpdateController(UserSettingsService service) {
        this.service = service;
    }

    @Operation(summary = "Update the settings of the authenticated account")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/api/v1/settings/user/me")
    ResponseEntity<UserSettings> updateMyUserSettings(
            Principal principal, @Valid @RequestBody UpdateUserSettingsRequest request) {
        return service.updateByUsername(principal.getName(), request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}