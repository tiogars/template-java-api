package fr.tiogars.domaintemplate.domains.auth.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import fr.tiogars.domaintemplate.config.JwtProperties;

public class JwtTokenServiceTest {

    @Test
    public void generateToken_canBeValidatedForItsSubject() {
        JwtTokenService service = new JwtTokenService(
                new JwtProperties("test-jwt-secret-change-before-production-32chars", 3600),
                Clock.fixed(Instant.parse("2099-08-30T12:00:00Z"), ZoneOffset.UTC));
        UserDetails userDetails = User.withUsername("admin")
                .password("hash")
                .authorities("ROLE_ADMIN")
                .build();

        String token = service.generateToken(userDetails);

        assertEquals("admin", service.extractUsername(token).orElseThrow());
        assertTrue(service.isValid(token, userDetails));
        assertFalse(
                service.isValid(token, User.withUsername("other").password("hash").authorities("ROLE_USER").build()));
    }
}