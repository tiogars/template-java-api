package fr.tiogars.domaintemplate.domains.auth.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import fr.tiogars.domaintemplate.domains.auth.models.LoginRequest;
import fr.tiogars.domaintemplate.domains.auth.models.LoginResponse;
import fr.tiogars.domaintemplate.domains.auth.user.entities.User;
import fr.tiogars.domaintemplate.domains.auth.user.services.UserService;

/** Authenticates credentials and issues JWT access tokens. */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final UserService userService;

    AuthService(AuthenticationManager authenticationManager, JwtTokenService jwtTokenService, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.userService = userService;
    }

    public LoginResponse login(LoginRequest request) {
        UserDetails principal = (UserDetails) authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())).getPrincipal();
        User user = userService.findByUsername(principal.getUsername()).orElseThrow();
        return new LoginResponse(
                jwtTokenService.generateToken(principal),
                "Bearer",
                jwtTokenService.expiresAt(),
                user);
    }
}