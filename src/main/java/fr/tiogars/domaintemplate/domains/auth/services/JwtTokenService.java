package fr.tiogars.domaintemplate.domains.auth.services;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import fr.tiogars.domaintemplate.config.JwtProperties;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/** Generates and validates signed JWT access tokens. */
@Service
public class JwtTokenService {

    private final Clock clock;
    private final JwtProperties properties;

    @Autowired
    public JwtTokenService(JwtProperties properties) {
        this(properties, Clock.systemUTC());
    }

    JwtTokenService(JwtProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    public String generateToken(UserDetails userDetails) {
        Instant now = Instant.now(clock);
        List<String> authorities = userDetails.getAuthorities().stream()
            .map(authority -> authority.getAuthority())
                .toList();
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt(now)))
                .claim("authorities", authorities)
                .signWith(signingKey())
                .compact();
    }

    public Optional<String> extractUsername(String token) {
        try {
            return Optional.ofNullable(Jwts.parser()
                    .verifyWith(signingKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject());
        } catch (JwtException | IllegalArgumentException _) {
            return Optional.empty();
        }
    }

    public boolean isValid(String token, UserDetails userDetails) {
        return extractUsername(token)
                .map(username -> username.equals(userDetails.getUsername()))
                .orElse(false);
    }

    public Instant expiresAt() {
        return expiresAt(Instant.now(clock));
    }

    private Instant expiresAt(Instant now) {
        return now.plusSeconds(properties.expirationSeconds());
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }
}