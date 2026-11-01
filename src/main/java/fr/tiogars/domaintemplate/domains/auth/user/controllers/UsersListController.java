package fr.tiogars.domaintemplate.domains.auth.user.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.tiogars.domaintemplate.domains.auth.user.entities.User;
import fr.tiogars.domaintemplate.domains.auth.user.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/** Exposes the endpoint that lists user accounts. */
@Tag(name = "Users", description = "User account management operations.")
@RestController
public class UsersListController {

    private final UserService service;

    UsersListController(UserService service) {
        this.service = service;
    }

    @Operation(summary = "List user accounts")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/api/v1/auth/users")
    List<User> listUsers() {
        return service.findAll();
    }
}