package fr.tiogars.domaintemplate.config;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** Configures authenticated actors and PostgreSQL-compatible audit timestamps. */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "securityAuditorAware", dateTimeProviderRef = "auditDateTimeProvider")
public class JpaAuditingConfiguration {

    @Bean
    public Clock auditClock() {
        return Clock.systemUTC();
    }

    @Bean
    public DateTimeProvider auditDateTimeProvider(Clock auditClock) {
        return () -> Optional.of(Instant.now(auditClock).truncatedTo(ChronoUnit.MICROS));
    }
}
