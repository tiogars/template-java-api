package fr.tiogars.domaintemplate.domains.auth.role.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.tiogars.domaintemplate.domains.auth.role.entities.Role;
import fr.tiogars.domaintemplate.domains.auth.role.services.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/** Exposes the endpoint that lists roles. */
@Tag(name = "Roles", description = "Role management operations.")
@RestController
public class RolesListController {

    private final RoleService service;

    RolesListController(RoleService service) {
        this.service = service;
    }

    @Operation(summary = "List roles")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/api/v1/auth/roles")
    List<Role> listRoles() {
        return service.findAll();
    }
}