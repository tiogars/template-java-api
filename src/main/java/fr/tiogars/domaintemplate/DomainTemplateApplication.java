package fr.tiogars.domaintemplate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Bootstraps the DomainTemplate Spring application.
 *
 * @since 0.1.0
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class DomainTemplateApplication {

    /**
     * Starts the application.
     *
     * @param args command-line arguments passed to Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(DomainTemplateApplication.class, args);
    }
}