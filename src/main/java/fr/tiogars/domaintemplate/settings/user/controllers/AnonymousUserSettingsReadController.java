package fr.tiogars.domaintemplate.settings.user.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.tiogars.domaintemplate.settings.user.entities.UserSettings;
import fr.tiogars.domaintemplate.settings.user.services.UserSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/** Exposes the public endpoint that reads the ANONYMOUS role settings. */
@Tag(name = "User settings", description = "User and ANONYMOUS role settings operations.")
@RestController
public class AnonymousUserSettingsReadController {

    private final UserSettingsService service;

    AnonymousUserSettingsReadController(UserSettingsService service) {
        this.service = service;
    }

    @Operation(summary = "Read the settings applied to anonymous visitors")
    @GetMapping("/api/v1/settings/user/anonymous")
    UserSettings readAnonymousUserSettings() {
        return service.findAnonymous();
    }
}