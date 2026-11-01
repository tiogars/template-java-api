package fr.tiogars.domaintemplate.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class SecurityAuditorAwareTest {

    private final SecurityAuditorAware auditor = new SecurityAuditorAware();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentAuditor_usesAuthenticatedUsername() {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("operator", null, List.of()));

        assertEquals("operator", auditor.getCurrentAuditor().orElseThrow());
    }

    @Test
    void getCurrentAuditor_rejectsMissingAuthentication() {
        assertThrows(AuthenticationCredentialsNotFoundException.class, auditor::getCurrentAuditor);
    }

    @Test
    void getCurrentAuditor_rejectsUnauthenticatedPrincipal() {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.unauthenticated("operator", null));

        assertThrows(AuthenticationCredentialsNotFoundException.class, auditor::getCurrentAuditor);
    }

    @Test
    void getCurrentAuditor_rejectsAnonymousAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(
                new AnonymousAuthenticationToken("test", "anonymousUser",
                        List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));

        assertThrows(AuthenticationCredentialsNotFoundException.class, auditor::getCurrentAuditor);
    }
}
