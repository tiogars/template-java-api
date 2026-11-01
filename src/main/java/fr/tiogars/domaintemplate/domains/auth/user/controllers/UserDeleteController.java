package fr.tiogars.domaintemplate.domains.auth.user.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import fr.tiogars.domaintemplate.domains.auth.user.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/** Exposes the endpoint that deletes user accounts. */
@Tag(name = "Users", description = "User account management operations.")
@RestController
public class UserDeleteController {

    private final UserService service;

    UserDeleteController(UserService service) {
        this.service = service;
    }

    @Operation(summary = "Delete a user account")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/api/v1/auth/users/{id}")
    ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        return service.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}