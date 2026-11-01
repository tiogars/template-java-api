package fr.tiogars.domaintemplate.domains.auth.controllers;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.tiogars.domaintemplate.domains.auth.user.entities.User;
import fr.tiogars.domaintemplate.domains.auth.user.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/** Exposes the current authenticated user. */
@Tag(name = "Authentication", description = "JWT authentication endpoints.")
@RestController
public class AuthMeController {

    private final UserService service;

    AuthMeController(UserService service) {
        this.service = service;
    }

    @Operation(summary = "Current user", description = "Returns the authenticated user.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/api/v1/auth/me")
    ResponseEntity<User> me(Principal principal) {
        return service.findByUsername(principal.getName())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}