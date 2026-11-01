package fr.tiogars.domaintemplate.settings.user.controllers;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.tiogars.domaintemplate.settings.user.entities.UserSettings;
import fr.tiogars.domaintemplate.settings.user.services.UserSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/** Exposes the endpoint that reads the settings of the authenticated account. */
@Tag(name = "User settings", description = "User and ANONYMOUS role settings operations.")
@RestController
public class MyUserSettingsReadController {

    private final UserSettingsService service;

    MyUserSettingsReadController(UserSettingsService service) {
        this.service = service;
    }

    @Operation(summary = "Read the settings of the authenticated account")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/api/v1/settings/user/me")
    ResponseEntity<UserSettings> readMyUserSettings(Principal principal) {
        return service.findByUsername(principal.getName())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}