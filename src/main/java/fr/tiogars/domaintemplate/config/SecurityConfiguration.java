package fr.tiogars.domaintemplate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import fr.tiogars.domaintemplate.domains.auth.services.JwtAuthenticationFilter;

/** Configures stateless JWT security for the API. */
@Configuration
@EnableMethodSecurity
class SecurityConfiguration {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers("/actuator/health", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/task/export.*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/task/*/print.pdf").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/task/print-selected.pdf").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/task", "/api/v1/task/*").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/api/v1/task", "/api/v1/task/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/tag", "/api/v1/tag/*",
                                "/api/v1/nomenclature", "/api/v1/nomenclature/*").hasAnyRole("ADMIN", "USER")
                        .requestMatchers("/api/v1/tag", "/api/v1/tag/**",
                                "/api/v1/nomenclature", "/api/v1/nomenclature/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/auth/users", "/api/v1/auth/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/auth/roles", "/api/v1/auth/roles/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/auth/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/settings/user/anonymous").permitAll()
                        .requestMatchers("/api/v1/settings/user/anonymous", "/api/v1/settings/user/users/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/settings/user/me").hasAnyRole("ADMIN", "USER")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}