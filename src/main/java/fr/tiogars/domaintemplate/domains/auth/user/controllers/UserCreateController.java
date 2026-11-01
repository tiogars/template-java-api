package fr.tiogars.domaintemplate.domains.auth.user.controllers;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import fr.tiogars.domaintemplate.domains.auth.user.entities.User;
import fr.tiogars.domaintemplate.domains.auth.user.models.CreateUserRequest;
import fr.tiogars.domaintemplate.domains.auth.user.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/** Exposes the endpoint that creates user accounts. */
@Tag(name = "Users", description = "User account management operations.")
@RestController
public class UserCreateController {

    private final UserService service;

    UserCreateController(UserService service) {
        this.service = service;
    }

    @Operation(summary = "Create a user account")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/api/v1/auth/users")
    ResponseEntity<User> createUser(@Valid @RequestBody CreateUserRequest request) {
        return service.create(request)
                .map(created -> ResponseEntity.created(URI.create("/api/v1/auth/users/" + created.id())).body(created))
                .orElseGet(() -> ResponseEntity.status(409).build());
    }
}