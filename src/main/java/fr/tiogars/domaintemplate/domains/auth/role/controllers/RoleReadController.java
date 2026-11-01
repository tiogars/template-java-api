package fr.tiogars.domaintemplate.domains.auth.role.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import fr.tiogars.domaintemplate.domains.auth.role.entities.Role;
import fr.tiogars.domaintemplate.domains.auth.role.services.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/** Exposes the endpoint that reads roles. */
@Tag(name = "Roles", description = "Role management operations.")
@RestController
public class RoleReadController {

    private final RoleService service;

    RoleReadController(RoleService service) {
        this.service = service;
    }

    @Operation(summary = "Read a role")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/api/v1/auth/roles/{id}")
    ResponseEntity<Role> readRole(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}