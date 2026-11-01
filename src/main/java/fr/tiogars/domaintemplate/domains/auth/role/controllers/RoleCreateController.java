package fr.tiogars.domaintemplate.domains.auth.role.controllers;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import fr.tiogars.domaintemplate.domains.auth.role.entities.Role;
import fr.tiogars.domaintemplate.domains.auth.role.models.CreateRoleRequest;
import fr.tiogars.domaintemplate.domains.auth.role.services.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/** Exposes the endpoint that creates roles. */
@Tag(name = "Roles", description = "Role management operations.")
@RestController
public class RoleCreateController {

    private final RoleService service;

    RoleCreateController(RoleService service) {
        this.service = service;
    }

    @Operation(summary = "Create a role")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/api/v1/auth/roles")
    ResponseEntity<Role> createRole(@Valid @RequestBody CreateRoleRequest request) {
        return service.create(request)
                .map(created -> ResponseEntity.created(URI.create("/api/v1/auth/roles/" + created.id())).body(created))
                .orElseGet(() -> ResponseEntity.status(409).build());
    }
}