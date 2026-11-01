package fr.tiogars.domaintemplate.settings.user.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import fr.tiogars.domaintemplate.settings.user.entities.UserSettings;
import fr.tiogars.domaintemplate.settings.user.services.UserSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/** Exposes the administrator endpoint that reads the settings of a USER account. */
@Tag(name = "User settings", description = "User and ANONYMOUS role settings operations.")
@RestController
public class UserSettingsReadController {

    private final UserSettingsService service;

    UserSettingsReadController(UserSettingsService service) {
        this.service = service;
    }

    @Operation(summary = "Read the settings of a user account holding the USER role")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/api/v1/settings/user/users/{userId}")
    ResponseEntity<UserSettings> readUserSettings(@PathVariable Long userId) {
        return service.findByUserId(userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}