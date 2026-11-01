package fr.tiogars.domaintemplate.domains.task.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import fr.tiogars.domaintemplate.common.models.DeleteSelectedResponse;
import fr.tiogars.domaintemplate.config.ApiCorsConfiguration;
import fr.tiogars.domaintemplate.domains.auth.services.JwtTokenService;
import fr.tiogars.domaintemplate.domains.task.models.CreateTaskRequest;
import fr.tiogars.domaintemplate.domains.task.models.Task;
import fr.tiogars.domaintemplate.domains.task.models.UpdateTaskRequest;
import fr.tiogars.domaintemplate.domains.task.services.TaskCsvCodec;
import fr.tiogars.domaintemplate.domains.task.services.TaskCreateService;
import fr.tiogars.domaintemplate.domains.task.services.TaskDeleteSelectedService;
import fr.tiogars.domaintemplate.domains.task.services.TaskDeleteService;
import fr.tiogars.domaintemplate.domains.task.services.TaskFindAllService;
import fr.tiogars.domaintemplate.domains.task.services.TaskFindByIdService;
import fr.tiogars.domaintemplate.domains.task.services.TaskSeedService;
import fr.tiogars.domaintemplate.domains.task.services.TaskUpdateService;

@WebMvcTest({
                TaskCreateController.class,
                TaskUpdateController.class,
                TasksListController.class,
                TaskReadController.class,
                TaskDeleteController.class,
                TaskDeleteSelectedController.class,
                TaskSeedController.class,
                TaskImportExportController.class
})
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiCorsConfiguration.class)
public class TaskControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private TaskCreateService createService;

        @MockitoBean
        private TaskUpdateService updateService;

        @MockitoBean
        private TaskDeleteService deleteService;

        @MockitoBean
        private TaskFindAllService findAllService;

        @MockitoBean
        private TaskFindByIdService findByIdService;

        @MockitoBean
        private TaskSeedService seedService;

        @MockitoBean
        private TaskCsvCodec csvCodec;

        @MockitoBean
        private TaskDeleteSelectedService deleteSelectedService;

        @MockitoBean
        private JwtTokenService jwtTokenService;

        @MockitoBean
        private UserDetailsService userDetailsService;

        @Test
        public void deleteSelected_returnsDeletedAndSkippedIds() throws Exception {
                when(deleteSelectedService.deleteSelected(List.of(1L, 2L)))
                                .thenReturn(new DeleteSelectedResponse(List.of(1L), List.of(2L)));

                mockMvc.perform(post("/api/v1/task/delete-selected")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"ids\":[1,2]}"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.deletedIds[0]").value(1))
                                .andExpect(jsonPath("$.skippedIds[0]").value(2));
        }

        @Test
        public void deleteSelected_rejectsEmptySelection() throws Exception {
                mockMvc.perform(post("/api/v1/task/delete-selected")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"ids\":[]}"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        public void create_returnsConflict_whenConcurrentWriterClaimsCode() throws Exception {
                when(createService.create(any(CreateTaskRequest.class)))
                                .thenThrow(new DataIntegrityViolationException("Duplicate active code",
                                                new ConstraintViolationException("Duplicate active code",
                                                                new SQLException(), "uk_tasks_active_code")));

                mockMvc.perform(post("/api/v1/task")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"code\":\"CUSTOMER\",\"label\":\"Customer\"}"))
                                .andExpect(status().isConflict());
        }

        @Test
        public void update_returnsConflict_whenConcurrentWriterChangesRow() throws Exception {
                when(updateService.update(eq(42L), any(UpdateTaskRequest.class)))
                                .thenThrow(new OptimisticLockingFailureException("Concurrent modification"));

                mockMvc.perform(put("/api/v1/task/42")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"label\":\"Customer\"}"))
                                .andExpect(status().isConflict());
        }

        @Test
        public void create_returnsCreatedTemplateAndLocation() throws Exception {
                when(createService.create(any(CreateTaskRequest.class))).thenReturn(Optional.of(template()));

                mockMvc.perform(post("/api/v1/task")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"code\":\"CUSTOMER\",\"label\":\"Customer\",\"description\":\"Example\"}"))
                                .andExpect(status().isCreated())
                                .andExpect(header().string("Location", "/api/v1/task/42"))
                                .andExpect(jsonPath("$.code").value("CUSTOMER"));
        }

        @Test
        public void create_rejectsBlankCode() throws Exception {
                mockMvc.perform(post("/api/v1/task")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"code\":\" \",\"label\":\"Customer\"}"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        public void findAllAndFindById_returnTemplates() throws Exception {
                when(findAllService.findAll()).thenReturn(List.of(template()));
                when(findByIdService.findById(42L)).thenReturn(Optional.of(template()));

                mockMvc.perform(get("/api/v1/task"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id").value(42))
                                .andExpect(jsonPath("$[0].createdBy").value("creator"))
                                .andExpect(jsonPath("$[0].lastModifiedBy").value("editor"));
                mockMvc.perform(get("/api/v1/task/42"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.label").value("Customer"))
                                .andExpect(jsonPath("$.createdBy").value("creator"))
                                .andExpect(jsonPath("$.lastModifiedBy").value("editor"));
        }

        @Test
        public void findAll_allowsBackendConsoleOrigin() throws Exception {
                when(findAllService.findAll()).thenReturn(List.of(template()));

                mockMvc.perform(get("/api/v1/task")
                                .header(HttpHeaders.ORIGIN, "http://localhost:5173"))
                                .andExpect(status().isOk())
                                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                                                "http://localhost:5173"));
        }

        @Test
        public void updateAndDelete_usePersistentIdentifier() throws Exception {
                when(updateService.update(eq(42L), any(UpdateTaskRequest.class)))
                                .thenReturn(Optional.of(template()));
                when(deleteService.delete(42L)).thenReturn(true);

                mockMvc.perform(put("/api/v1/task/42")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"label\":\"Customer\",\"description\":\"Updated\"}"))
                                .andExpect(status().isOk());
                mockMvc.perform(delete("/api/v1/task/42"))
                                .andExpect(status().isNoContent());
        }

        @Test
        public void seedAndImportExport_useNewRoutes() throws Exception {
                when(seedService.seed(any())).thenReturn(List.of(template()));
                when(findAllService.findAll()).thenReturn(List.of(template()));
                when(csvCodec.serialize(any())).thenReturn("id,code,label,description,createdDate,lastModifiedDate\n");
                when(csvCodec.parse(any())).thenReturn(List.of(
                                new CreateTaskRequest("CUSTOMER", "Customer", null)));

                mockMvc.perform(post("/api/v1/task/seed")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("[{\"code\":\"CUSTOMER\",\"label\":\"Customer\"}]"))
                                .andExpect(status().isCreated());
                mockMvc.perform(get("/api/v1/task/export.csv"))
                                .andExpect(status().isOk())
                                .andExpect(content().contentTypeCompatibleWith("text/csv"));
                mockMvc.perform(post("/api/v1/task/import.csv")
                                .contentType("text/csv")
                                .content("id,code,label,description,createdDate,lastModifiedDate\n"))
                                .andExpect(status().isCreated());
        }

        private static Task template() {
                Instant timestamp = Instant.parse("2026-08-30T12:00:00Z");
                return new Task(42L, "CUSTOMER", "Customer", "Example", timestamp, timestamp,
                                "creator", "editor");
        }
}