package fr.tiogars.domaintemplate.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

/** Initial administrator account settings. */
@Validated
@ConfigurationProperties(prefix = "app.admin")
public record AdminAccountProperties(
        @NotBlank String username,
        @NotBlank String password) {
}