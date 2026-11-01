package fr.tiogars.domaintemplate.settings.user.controllers;

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

/** Exposes the administrator endpoint that updates the ANONYMOUS role settings. */
@Tag(name = "User settings", description = "User and ANONYMOUS role settings operations.")
@RestController
public class AnonymousUserSettingsUpdateController {

    private final UserSettingsService service;

    AnonymousUserSettingsUpdateController(UserSettingsService service) {
        this.service = service;
    }

    @Operation(summary = "Update the settings applied to anonymous visitors")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/api/v1/settings/user/anonymous")
    UserSettings updateAnonymousUserSettings(@Valid @RequestBody UpdateUserSettingsRequest request) {
        return service.updateAnonymous(request);
    }
}