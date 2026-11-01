package fr.tiogars.domaintemplate.domains.auth.role.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import fr.tiogars.domaintemplate.common.models.DeleteSelectedRequest;
import fr.tiogars.domaintemplate.common.models.DeleteSelectedResponse;
import fr.tiogars.domaintemplate.domains.auth.role.services.RoleDeleteSelectedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/** Exposes the endpoint that deletes a selection of roles. */
@Tag(name = "Roles", description = "Role management operations.")
@RestController
public class RoleDeleteSelectedController {

    private final RoleDeleteSelectedService service;

    RoleDeleteSelectedController(RoleDeleteSelectedService service) {
        this.service = service;
    }

    @Operation(summary = "Delete selected roles", description = "System roles and unknown identifiers are reported as skipped.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/api/v1/auth/roles/delete-selected")
    DeleteSelectedResponse deleteSelectedRoles(@Valid @RequestBody DeleteSelectedRequest request) {
        return service.deleteSelected(request.ids());
    }
}
