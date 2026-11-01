package fr.tiogars.domaintemplate.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/** JWT signing and lifetime settings. */
@Validated
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        @NotBlank String secret,
        @Positive long expirationSeconds) {

    @AssertTrue(message = "JWT secret must contain at least 32 characters")
    boolean isSecretLongEnough() {
        return secret != null && secret.length() >= 32;
    }
}