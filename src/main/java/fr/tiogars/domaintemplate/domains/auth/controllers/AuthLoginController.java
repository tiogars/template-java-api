package fr.tiogars.domaintemplate.domains.auth.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import fr.tiogars.domaintemplate.domains.auth.models.LoginRequest;
import fr.tiogars.domaintemplate.domains.auth.models.LoginResponse;
import fr.tiogars.domaintemplate.domains.auth.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/** Exposes the endpoint that authenticates users. */
@Tag(name = "Authentication", description = "JWT authentication endpoints.")
@RestController
public class AuthLoginController {

    private final AuthService service;

    AuthLoginController(AuthService service) {
        this.service = service;
    }

    @Operation(summary = "Login", description = "Authenticates credentials and returns a JWT access token.")
    @ApiResponse(responseCode = "200", description = "Credentials are valid.")
    @ApiResponse(responseCode = "401", description = "Credentials are invalid.")
    @PostMapping("/api/v1/auth/login")
    ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(service.login(request));
    }
}