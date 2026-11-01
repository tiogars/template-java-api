package fr.tiogars.domaintemplate.config;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import fr.tiogars.domaintemplate.domains.auth.services.JwtTokenService;
import fr.tiogars.domaintemplate.domains.auth.services.JwtAuthenticationFilter;
import fr.tiogars.domaintemplate.domains.task.controllers.TaskPrintController;
import fr.tiogars.domaintemplate.domains.task.services.TaskPrintService;
import fr.tiogars.domaintemplate.domains.task.services.TasksPrintAllService;
import fr.tiogars.domaintemplate.domains.task.services.TasksPrintSelectedService;

@WebMvcTest({TaskPrintController.class,
        fr.tiogars.domaintemplate.domains.tag.controllers.TagController.class,
        fr.tiogars.domaintemplate.domains.nomenclature.controllers.NomenclatureController.class})
@Import({SecurityConfiguration.class, JwtAuthenticationFilter.class,
        TaskPrintSecurityTest.SecurityTestConfiguration.class})
class TaskPrintSecurityTest {

    @TestConfiguration
    @EnableWebSecurity
    static class SecurityTestConfiguration {
    }

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    private MockMvc mvc;

    @MockitoBean
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private TaskPrintService printService;

    @MockitoBean
    private TasksPrintAllService printAllService;

    @MockitoBean
    private TasksPrintSelectedService printSelectedService;

    @MockitoBean
    private fr.tiogars.domaintemplate.domains.taxonomy.TaxonomyService taxonomyService;

    @ParameterizedTest
    @ValueSource(strings = {"/api/v1/tag", "/api/v1/nomenclature"})
    void taxonomyReadsAllowUsersButWritesRequireAdmin(String path) throws Exception {
        authenticate("USER");
        mvc.perform(get(path).header("Authorization", "Bearer " + "print-test")).andExpect(status().isOk());
        mvc.perform(post(path).header("Authorization", "Bearer " + "print-test")
                .contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"CODE\",\"label\":\"Label\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(path + "/1")
                .header("Authorization", "Bearer " + "print-test")).andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/v1/tag", "/api/v1/nomenclature"})
    void taxonomyWritesAllowAdminAndAnonymousReadsAreDenied(String path) throws Exception {
        mvc.perform(get(path)).andExpect(status().isForbidden());
        authenticate("ADMIN");
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(path + "/1")
                .header("Authorization", "Bearer " + "print-test")).andExpect(status().isNoContent());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/v1/tag", "/api/v1/nomenclature"})
    void taxonomyRejectsAuthenticatedUnrelatedRolesForEveryCrudOperation(String path) throws Exception {
        authenticate("ANONYMOUS");
        for (String readPath : List.of(path, path + "/1")) {
            mvc.perform(get(readPath).header("Authorization", "Bearer " + "print-test"))
                    .andExpect(status().isForbidden());
        }
        mvc.perform(post(path).header("Authorization", "Bearer " + "print-test")
                .contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"CODE\",\"label\":\"Label\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(path + "/1")
                .header("Authorization", "Bearer " + "print-test")
                .contentType(MediaType.APPLICATION_JSON).content("{\"label\":\"Label\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(path + "/1")
                .header("Authorization", "Bearer " + "print-test")).andExpect(status().isForbidden());
        verifyNoInteractions(taxonomyService);
    }

    @BeforeEach
    void configureSecurityFilters() {
        // Register JWT only inside its security chain, not twice through MockMvc's bean filter discovery.
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(springSecurityFilterChain).build();
    }

    @ParameterizedTest
    @ValueSource(strings = {"USER", "ADMIN"})
    void printEndpoints_should_allowTheSameRolesAsReading(String role) throws Exception {
        authenticate(role);
        byte[] pdf = "%PDF-1.7".getBytes(StandardCharsets.UTF_8);
        when(printService.print(42L)).thenReturn(Optional.of(pdf));
        when(printAllService.printAll()).thenReturn(pdf);
        when(printSelectedService.printSelected(List.of(42L))).thenReturn(Optional.of(pdf));

        mvc.perform(get("/api/v1/task/42/print.pdf")
                .header("Authorization", "Bearer print-test")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/task/print.pdf")
                .header("Authorization", "Bearer print-test")).andExpect(status().isOk());
        mvc.perform(post("/api/v1/task/print-selected.pdf")
                .header("Authorization", "Bearer print-test")
                .contentType(MediaType.APPLICATION_JSON).content("{\"ids\":[42]}")).andExpect(status().isOk());
    }

    @Test
    void printEndpoints_should_denyUnauthenticatedRequests() throws Exception {
        mvc.perform(get("/api/v1/task/42/print.pdf")).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/task/print.pdf")).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/task/print-selected.pdf")
                .contentType(MediaType.APPLICATION_JSON).content("{\"ids\":[42]}")).andExpect(status().isForbidden());

        verifyNoInteractions(printService, printAllService, printSelectedService);
    }

    @Test
    void printEndpoints_should_denyAuthenticatedUsersWithoutReadingRoles() throws Exception {
        authenticate("ANONYMOUS");
        mvc.perform(get("/api/v1/task/42/print.pdf")
                .header("Authorization", "Bearer print-test")).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/task/print.pdf")
                .header("Authorization", "Bearer print-test")).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/task/print-selected.pdf")
                .header("Authorization", "Bearer print-test")
                .contentType(MediaType.APPLICATION_JSON).content("{\"ids\":[42]}")).andExpect(status().isForbidden());

        verifyNoInteractions(printService, printAllService, printSelectedService);
    }

    private void authenticate(String role) {
        UserDetails user = User.withUsername("printer").password("unused").roles(role).build();
        when(jwtTokenService.extractUsername("print-test")).thenReturn(Optional.of("printer"));
        when(userDetailsService.loadUserByUsername("printer")).thenReturn(user);
        when(jwtTokenService.isValid("print-test", user)).thenReturn(true);
    }
}
