package fr.tiogars.domaintemplate.domains.auth.user.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import fr.tiogars.domaintemplate.domains.auth.user.entities.User;
import fr.tiogars.domaintemplate.domains.auth.user.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/** Exposes the endpoint that reads user accounts. */
@Tag(name = "Users", description = "User account management operations.")
@RestController
public class UserReadController {

    private final UserService service;

    UserReadController(UserService service) {
        this.service = service;
    }

    @Operation(summary = "Read a user account")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/api/v1/auth/users/{id}")
    ResponseEntity<User> readUser(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}