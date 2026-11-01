package fr.tiogars.domaintemplate.settings.user.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.security.Principal;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import fr.tiogars.domaintemplate.config.ApiCorsConfiguration;
import fr.tiogars.domaintemplate.domains.auth.services.JwtTokenService;
import fr.tiogars.domaintemplate.settings.user.entities.UserSettings;
import fr.tiogars.domaintemplate.settings.user.models.UpdateUserSettingsRequest;
import fr.tiogars.domaintemplate.settings.user.services.UserSettingsService;

@WebMvcTest({
        MyUserSettingsReadController.class,
        MyUserSettingsUpdateController.class,
        UserSettingsReadController.class,
        UserSettingsUpdateController.class,
        AnonymousUserSettingsReadController.class,
        AnonymousUserSettingsUpdateController.class
})
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiCorsConfiguration.class)
class UserSettingsControllerTest {

    private static final Instant TIMESTAMP = Instant.parse("2026-01-02T03:04:05Z");
    private static final Principal ALICE = () -> "alice";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserSettingsService service;

    @MockitoBean
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void readMine_returnsSettingsOfAuthenticatedAccount() throws Exception {
        when(service.findByUsername("alice")).thenReturn(Optional.of(settings(7L, null, "dark")));

        mockMvc.perform(get("/api/v1/settings/user/me").principal(ALICE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(7))
                .andExpect(jsonPath("$.viewMode").value("dark"));
    }

    @Test
    void updateMine_updatesSettingsOfAuthenticatedAccount() throws Exception {
        when(service.updateByUsername(eq("alice"), any(UpdateUserSettingsRequest.class)))
                .thenReturn(Optional.of(settings(7L, null, "light")));

        mockMvc.perform(put("/api/v1/settings/user/me")
                .principal(ALICE)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"viewMode\":\"light\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.viewMode").value("light"));
    }

    @Test
    void updateMine_rejectsUnknownViewMode() throws Exception {
        mockMvc.perform(put("/api/v1/settings/user/me")
                .principal(ALICE)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"viewMode\":\"neon\"}"))
                .andExpect(status().isBadRequest());

        verify(service, never()).updateByUsername(any(), any());
    }

    @Test
    void readUser_returnsNotFoundWhenAccountIsNotManaged() throws Exception {
        when(service.findByUserId(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/settings/user/users/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_updatesSettingsOfUserRoleAccount() throws Exception {
        when(service.updateByUserId(eq(9L), any(UpdateUserSettingsRequest.class)))
                .thenReturn(Optional.of(settings(9L, null, "dark")));

        mockMvc.perform(put("/api/v1/settings/user/users/9")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"viewMode\":\"dark\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(9))
                .andExpect(jsonPath("$.viewMode").value("dark"));
    }

    @Test
    void readAnonymous_returnsRoleSettings() throws Exception {
        when(service.findAnonymous()).thenReturn(settings(null, "ANONYMOUS", "system"));

        mockMvc.perform(get("/api/v1/settings/user/anonymous"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleCode").value("ANONYMOUS"))
                .andExpect(jsonPath("$.viewMode").value("system"));
    }

    @Test
    void updateAnonymous_updatesRoleSettings() throws Exception {
        when(service.updateAnonymous(any(UpdateUserSettingsRequest.class)))
                .thenReturn(settings(null, "ANONYMOUS", "dark"));

        mockMvc.perform(put("/api/v1/settings/user/anonymous")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"viewMode\":\"dark\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.viewMode").value("dark"));
    }

    private static UserSettings settings(Long userId, String roleCode, String viewMode) {
        return new UserSettings(userId, roleCode, viewMode, TIMESTAMP, TIMESTAMP);
    }
}
