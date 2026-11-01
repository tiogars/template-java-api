package fr.tiogars.domaintemplate.domains.auth.role.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import fr.tiogars.domaintemplate.domains.auth.role.entities.Role;
import fr.tiogars.domaintemplate.domains.auth.role.models.UpdateRoleRequest;
import fr.tiogars.domaintemplate.domains.auth.role.services.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/** Exposes the endpoint that updates roles. */
@Tag(name = "Roles", description = "Role management operations.")
@RestController
public class RoleUpdateController {

    private final RoleService service;

    RoleUpdateController(RoleService service) {
        this.service = service;
    }

    @Operation(summary = "Update a role")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/api/v1/auth/roles/{id}")
    ResponseEntity<Role> updateRole(@PathVariable Long id, @Valid @RequestBody UpdateRoleRequest request) {
        return service.update(id, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}