package fr.tiogars.domaintemplate.domains.auth.user.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import fr.tiogars.domaintemplate.domains.auth.user.entities.User;
import fr.tiogars.domaintemplate.domains.auth.user.models.UpdateUserRequest;
import fr.tiogars.domaintemplate.domains.auth.user.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/** Exposes the endpoint that updates user accounts. */
@Tag(name = "Users", description = "User account management operations.")
@RestController
public class UserUpdateController {

    private final UserService service;

    UserUpdateController(UserService service) {
        this.service = service;
    }

    @Operation(summary = "Update a user account")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/api/v1/auth/users/{id}")
    ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return service.update(id, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}