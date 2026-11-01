package fr.tiogars.domaintemplate.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the public OpenAPI description for the service.
 */
@Configuration
class OpenApiConfiguration {

    /**
     * Creates the service OpenAPI metadata.
     *
     * @return OpenAPI description for Stored Data Domains
     */
    @Bean
    OpenAPI domainTemplateOpenApi() {
        return new OpenAPI()
            .components(new Components()
                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")))
                .info(new Info()
                        .title("Task API")
                        .version("v1")
                .description("CRUD API for the Task business object with JWT authentication."));
    }
}