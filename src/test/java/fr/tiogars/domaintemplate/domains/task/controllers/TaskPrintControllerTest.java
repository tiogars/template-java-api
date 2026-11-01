package fr.tiogars.domaintemplate.domains.task.controllers;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import fr.tiogars.domaintemplate.domains.auth.services.JwtTokenService;
import fr.tiogars.domaintemplate.domains.task.services.TaskPrintService;
import fr.tiogars.domaintemplate.domains.task.services.TasksPrintAllService;
import fr.tiogars.domaintemplate.domains.task.services.TasksPrintSelectedService;

@WebMvcTest(TaskPrintController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskPrintControllerTest {

    private static final byte[] PDF = "%PDF-1.7\nTest PDF bytes".getBytes(StandardCharsets.UTF_8);

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private TaskPrintService printService;

    @MockitoBean
    private TasksPrintAllService printAllService;

    @MockitoBean
    private TasksPrintSelectedService printSelectedService;

    @MockitoBean
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void print_should_returnInlinePdfForCurrentRecord() throws Exception {
        when(printService.print(42L)).thenReturn(Optional.of(PDF));

        mvc.perform(get("/api/v1/task/42/print.pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(content().bytes(PDF))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"task-42.pdf\""))
                .andExpect(header().string("Cache-Control", "no-store"));
    }

    @Test
    void printAll_should_returnInlinePdf() throws Exception {
        when(printAllService.printAll()).thenReturn(PDF);

        mvc.perform(get("/api/v1/task/print.pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(content().bytes(PDF))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"tasks.pdf\""));
    }

    @Test
    void printSelected_should_returnInlinePdfForRequestedIds() throws Exception {
        when(printSelectedService.printSelected(List.of(1L, 2L))).thenReturn(Optional.of(PDF));

        mvc.perform(post("/api/v1/task/print-selected.pdf")
                .contentType(MediaType.APPLICATION_JSON).content("{\"ids\":[1,2]}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(content().bytes(PDF))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"tasks-selected.pdf\""));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"ids\":null}", "{\"ids\":[]}", "{\"ids\":[null]}",
            "{\"ids\":[0]}", "{\"ids\":[-1]}"})
    void printSelected_should_rejectInvalidSelections(String body) throws Exception {
        mvc.perform(post("/api/v1/task/print-selected.pdf")
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(printSelectedService);
    }

    @Test
    void printSelected_should_rejectOversizedSelection() throws Exception {
        String body = "{\"ids\":[" + String.join(",", java.util.Collections.nCopies(1001, "1")) + "]}";
        mvc.perform(post("/api/v1/task/print-selected.pdf")
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(printSelectedService);
    }

    @Test
    void print_should_returnNotFoundWhenCurrentRecordIsMissing() throws Exception {
        when(printService.print(99L)).thenReturn(Optional.empty());

        mvc.perform(get("/api/v1/task/99/print.pdf")).andExpect(status().isNotFound());
    }

    @Test
    void printSelected_should_returnNotFoundWhenAnySelectedRecordIsMissing() throws Exception {
        when(printSelectedService.printSelected(List.of(1L, 99L))).thenReturn(Optional.empty());

        mvc.perform(post("/api/v1/task/print-selected.pdf")
                .contentType(MediaType.APPLICATION_JSON).content("{\"ids\":[1,99]}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void print_should_reportUnsupportedCharactersAsUnprocessableContent() throws Exception {
        when(printService.print(42L)).thenThrow(new ResponseStatusException(
                HttpStatus.UNPROCESSABLE_CONTENT, "Unsupported PDF characters."));

        mvc.perform(get("/api/v1/task/42/print.pdf"))
                .andExpect(status().is(HttpStatus.UNPROCESSABLE_CONTENT.value()));
    }
}
