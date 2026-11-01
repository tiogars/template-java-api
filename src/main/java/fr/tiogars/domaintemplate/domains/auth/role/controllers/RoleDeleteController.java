package fr.tiogars.domaintemplate.domains.auth.role.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import fr.tiogars.domaintemplate.domains.auth.role.services.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/** Exposes the endpoint that deletes roles. */
@Tag(name = "Roles", description = "Role management operations.")
@RestController
public class RoleDeleteController {

    private final RoleService service;

    RoleDeleteController(RoleService service) {
        this.service = service;
    }

    @Operation(summary = "Delete a role")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/api/v1/auth/roles/{id}")
    ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        return service.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}