package fr.tiogars.domaintemplate.domains.auth.user.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import fr.tiogars.domaintemplate.common.models.DeleteSelectedRequest;
import fr.tiogars.domaintemplate.common.models.DeleteSelectedResponse;
import fr.tiogars.domaintemplate.domains.auth.user.services.UserDeleteSelectedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/** Exposes the endpoint that deletes a selection of users. */
@Tag(name = "Users", description = "User account management operations.")
@RestController
public class UserDeleteSelectedController {

    private final UserDeleteSelectedService service;

    UserDeleteSelectedController(UserDeleteSelectedService service) {
        this.service = service;
    }

    @Operation(summary = "Delete selected users", description = "Unknown identifiers are reported as skipped.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/api/v1/auth/users/delete-selected")
    DeleteSelectedResponse deleteSelectedUsers(@Valid @RequestBody DeleteSelectedRequest request) {
        return service.deleteSelected(request.ids());
    }
}
