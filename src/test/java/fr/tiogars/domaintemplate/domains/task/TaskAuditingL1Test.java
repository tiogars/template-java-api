package fr.tiogars.domaintemplate.domains.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.jayway.jsonpath.JsonPath;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import fr.tiogars.domaintemplate.domains.task.entities.TaskEntity;
import fr.tiogars.domaintemplate.domains.task.repositories.TaskRepository;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Testcontainers
@Tag("Layer1")
class TaskAuditingL1Test {

    @Container
    private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17-alpine")
            .withInitScript("db/legacy-domain-templates.sql");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private TaskRepository repository;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) throws SQLException, IOException {
        try (Connection connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
                Statement statement = connection.createStatement()) {
            statement.execute(migrationSql());
            statement.execute(migrationSql());
        }
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @BeforeEach
    void authenticate() {
        authenticateAs("creator");
    }

    @AfterEach
    void cleanUp() {
        jdbc.update("DELETE FROM task_tags");
        jdbc.update("DELETE FROM tasks WHERE code <> 'LEGACY'");
        jdbc.update("DELETE FROM tags");
        jdbc.update("UPDATE nomenclatures SET parent_id = NULL");
        jdbc.update("DELETE FROM nomenclatures");
        SecurityContextHolder.clearContext();
    }

    @Test
    void taskAttributesRoundtripThroughCrudSeedAndImportExport() throws Exception {
        long tagId = createTaxonomy("/api/v1/tag", "{\"code\":\"TAG\",\"label\":\"Tag\"}");
        long categoryId = createTaxonomy("/api/v1/nomenclature", "{\"code\":\"CAT\",\"label\":\"Category\"}");
        String body = """
                {"code":"TASK","label":"Task","description":"Description","status":"IN_PROGRESS","priority":"URGENT",
                 "dueDate":"2026-12-31","assignedTo":"alice","tagIds":[%d],"nomenclatureId":%d}
                """.formatted(tagId, categoryId);
        String created = mvc.perform(post("/api/v1/task").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.priority").value("URGENT")).andExpect(jsonPath("$.dueDate").value("2026-12-31"))
                .andExpect(jsonPath("$.assignedTo").value("alice")).andExpect(jsonPath("$.tagIds[0]").value(tagId))
                .andExpect(jsonPath("$.nomenclatureId").value(categoryId)).andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(created, "$.id");
        mvc.perform(get("/api/v1/task/" + id)).andExpect(jsonPath("$.tagIds[0]").value(tagId));
        mvc.perform(post("/api/v1/task/seed").contentType(MediaType.APPLICATION_JSON).content("[" + body + "]"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$[0].priority").value("URGENT"));
        String exported = mvc.perform(get("/api/v1/task/export.json")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String taskExport = new tools.jackson.databind.ObjectMapper().writeValueAsString(
                JsonPath.read(exported, "$[?(@.code == 'TASK')]"));
        mvc.perform(post("/api/v1/task/import.json").contentType(MediaType.APPLICATION_JSON).content(taskExport))
                .andExpect(status().isCreated()).andExpect(jsonPath("$[?(@.code == 'TASK')].assignedTo").value("alice"));
        String csv = mvc.perform(get("/api/v1/task/export.csv")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String taskCsv = csv.lines().filter(line -> line.startsWith("id,") || line.contains(",TASK,"))
                .collect(java.util.stream.Collectors.joining("\n")) + "\n";
        mvc.perform(post("/api/v1/task/import.csv").contentType("text/csv").content(taskCsv))
                .andExpect(status().isCreated()).andExpect(jsonPath("$[?(@.code == 'TASK')].dueDate").value("2026-12-31"));
        mvc.perform(put("/api/v1/task/" + id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"label\":\"Reset\",\"description\":null}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value("TASK"))
                .andExpect(jsonPath("$.status").value("TODO")).andExpect(jsonPath("$.priority").value("NORMAL"))
                .andExpect(jsonPath("$.tagIds").isEmpty());
    }

    @Test
    void taxonomyValidatesReferencesCyclesAndProtectedDeletion() throws Exception {
        long tagId = createTaxonomy("/api/v1/tag", "{\"code\":\"TAG\",\"label\":\"Tag\"}");
        long rootId = createTaxonomy("/api/v1/nomenclature", "{\"code\":\"ROOT\",\"label\":\"Root\"}");
        long childId = createTaxonomy("/api/v1/nomenclature",
                "{\"code\":\"CHILD\",\"label\":\"Child\",\"parentId\":" + rootId + "}");
        mvc.perform(put("/api/v1/nomenclature/" + rootId).contentType(MediaType.APPLICATION_JSON)
                .content("{\"label\":\"Root\",\"parentId\":" + childId + "}")).andExpect(status().isBadRequest());
        mvc.perform(put("/api/v1/nomenclature/" + rootId).contentType(MediaType.APPLICATION_JSON)
                .content("{\"label\":\"Root\",\"parentId\":" + rootId + "}")).andExpect(status().isBadRequest());
        mvc.perform(delete("/api/v1/nomenclature/" + rootId)).andExpect(status().isConflict());
        mvc.perform(post("/api/v1/task").contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"BAD\",\"label\":\"Bad\",\"tagIds\":[999999]}")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/task").contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"BAD\",\"label\":\"Bad\",\"nomenclatureId\":999999}")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/nomenclature").contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"BAD\",\"label\":\"Bad\",\"parentId\":999999}")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/task").contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"TASK\",\"label\":\"Task\",\"tagIds\":[" + tagId + "],\"nomenclatureId\":" + childId + "}"))
                .andExpect(status().isCreated());
        mvc.perform(delete("/api/v1/tag/" + tagId)).andExpect(status().isConflict());
        mvc.perform(delete("/api/v1/nomenclature/" + childId)).andExpect(status().isConflict());
        mvc.perform(put("/api/v1/tag/" + tagId).contentType(MediaType.APPLICATION_JSON)
                .content("{\"label\":\"Updated\",\"description\":\"Reusable\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value("TAG"))
                .andExpect(jsonPath("$.createdBy").value("creator"));
    }

    private long createTaxonomy(String path, String body) throws Exception {
        String response = mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    @Test
    void migration_preservesLegacyRowsAndAllowsFreshDatabaseSetup() throws SQLException, IOException {
        Map<String, Object> legacy = jdbc.queryForMap("SELECT * FROM tasks WHERE code = 'LEGACY'");
        assertEquals("unknown", legacy.get("created_by"));
        assertEquals("unknown", legacy.get("last_modified_by"));
        assertEquals(Instant.parse("2026-08-01T00:00:00Z"),
                jdbc.queryForObject("SELECT created_date FROM tasks WHERE code = 'LEGACY'",
                        java.sql.Timestamp.class).toInstant());

        try (Connection connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
                Statement statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA fresh_database");
            statement.execute("SET search_path TO fresh_database");
            statement.execute(migrationSql());
            try (var result = statement.executeQuery(
                    "SELECT count(*) FROM pg_indexes WHERE schemaname = 'fresh_database' "
                            + "AND indexname = 'uk_tasks_active_code'")) {
                assertTrue(result.next());
                assertEquals(1, result.getInt(1));
            }
            statement.execute("DROP SCHEMA fresh_database CASCADE");
        }
    }

    @Test
    void migrationReplacesLegacyActiveCodeIndexWithTaskConstraintName() throws SQLException, IOException {
        try (Connection connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
                Statement statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA audited_legacy");
            statement.execute("SET search_path TO audited_legacy");
            statement.execute(migrationSql());
            statement.execute("ALTER TABLE tasks RENAME TO domain_templates");
            statement.execute("ALTER INDEX uk_tasks_active_code RENAME TO uk_domain_templates_active_code");
            statement.execute(migrationSql());
            statement.execute(migrationSql());
            try (var result = statement.executeQuery(
                    "SELECT indexname FROM pg_indexes WHERE schemaname = 'audited_legacy' "
                            + "AND indexname IN ('uk_tasks_active_code', 'uk_domain_templates_active_code')")) {
                assertTrue(result.next());
                assertEquals("uk_tasks_active_code", result.getString(1));
                assertTrue(!result.next());
            }
            statement.execute("DROP SCHEMA audited_legacy CASCADE");
        }
    }

    @Test
    void lifecycle_auditsActorsRetainsDeletedRowsAndReusesCodeWithNewId() throws Exception {
        long originalId = create("CUSTOMER");
        TaskEntity original = repository.findById(originalId).orElseThrow();
        assertEquals("creator", original.getCreatedBy());
        assertEquals("creator", original.getLastModifiedBy());
        Instant createdDate = original.getCreatedDate();
        assertEquals(createdDate, original.getLastModifiedDate());
        assertEquals(0, createdDate.getNano() % 1000);

        authenticateAs("editor");
        String response = mvc.perform(put("/api/v1/task/{id}", originalId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"label\":\"Updated\",\"description\":\"Edited\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        TaskEntity updated = repository.findById(originalId).orElseThrow();
        assertEquals("creator", updated.getCreatedBy());
        assertEquals("editor", updated.getLastModifiedBy());
        assertEquals(createdDate, updated.getCreatedDate());
        assertTrue(updated.getLastModifiedDate().isAfter(createdDate));
        assertEquals(updated.getLastModifiedDate(), Instant.parse(JsonPath.read(response, "$.lastModifiedDate")));

        authenticateAs("deleter");
        mvc.perform(delete("/api/v1/task/{id}", originalId)).andExpect(status().isNoContent());
        Map<String, Object> archived = jdbc.queryForMap("SELECT * FROM tasks WHERE id = ?", originalId);
        assertEquals("CUSTOMER", archived.get("code"));
        assertEquals("creator", archived.get("created_by"));
        assertEquals("deleter", archived.get("deleted_by"));
        assertEquals("deleter", archived.get("last_modified_by"));
        assertTrue(archived.get("deleted_date") != null);
        assertTrue(repository.findById(originalId).isEmpty());
        assertTrue(repository.findByCode("CUSTOMER").isEmpty());
        assertTrue(!repository.existsById(originalId));
        mvc.perform(get("/api/v1/task/{id}", originalId)).andExpect(status().isNotFound());
        mvc.perform(put("/api/v1/task/{id}", originalId)
                .contentType(MediaType.APPLICATION_JSON).content("{\"label\":\"Resurrect\"}"))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/v1/task/{id}", originalId)).andExpect(status().isNotFound());
        String exported = mvc.perform(get("/api/v1/task/export.json"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        List<String> exportedCodes = JsonPath.read(exported, "$[*].code");
        assertTrue(!exportedCodes.contains("CUSTOMER"));
        mvc.perform(get("/api/v1/task/export.csv"))
                .andExpect(result -> assertTrue(!result.getResponse().getContentAsString().contains("CUSTOMER")));

        authenticateAs("replacementCreator");
        long replacementId = create("CUSTOMER");
        assertNotEquals(originalId, replacementId);
        assertEquals(2, jdbc.queryForObject(
                "SELECT count(*) FROM tasks WHERE code = 'CUSTOMER'", Integer.class));
        mvc.perform(get("/api/v1/task"))
                .andExpect(jsonPath("$[0].id").value(replacementId))
                .andExpect(jsonPath("$.length()").value(2));
        mvc.perform(post("/api/v1/task").contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"CUSTOMER\",\"label\":\"Duplicate\"}")).andExpect(status().isConflict());
        assertThrows(DataIntegrityViolationException.class,
                () -> repository.saveAndFlush(new TaskEntity("CUSTOMER", "Duplicate", null)));
    }

    @Test
    void pdfPrinting_excludesArchivedRowsAndOnlyPrintsSelectedActiveRecords() throws Exception {
        long archivedId = create("ARCHIVED_PDF");
        long selectedId = create("SELECTED_PDF");
        create("UNSELECTED_PDF");
        mvc.perform(delete("/api/v1/task/{id}", archivedId)).andExpect(status().isNoContent());

        mvc.perform(get("/api/v1/task/{id}/print.pdf", archivedId)).andExpect(status().isNotFound());
        mvc.perform(post("/api/v1/task/print-selected.pdf")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ids\":[" + selectedId + "," + archivedId + "]}")).andExpect(status().isNotFound());

        byte[] selectedPdf = mvc.perform(post("/api/v1/task/print-selected.pdf")
                .contentType(MediaType.APPLICATION_JSON).content("{\"ids\":[" + selectedId + "]}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();
        try (PDDocument pdf = Loader.loadPDF(selectedPdf)) {
            String text = new PDFTextStripper().getText(pdf);
            assertTrue(text.contains("SELECTED_PDF"));
            assertTrue(!text.contains("UNSELECTED_PDF"));
            assertTrue(!text.contains("ARCHIVED_PDF"));
            assertEquals(1, pdf.getNumberOfPages());
        }

        byte[] allPdf = mvc.perform(get("/api/v1/task/print.pdf"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();
        try (PDDocument pdf = Loader.loadPDF(allPdf)) {
            String text = new PDFTextStripper().getText(pdf);
            assertTrue(text.contains("SELECTED_PDF"));
            assertTrue(text.contains("UNSELECTED_PDF"));
            assertTrue(!text.contains("ARCHIVED_PDF"));
        }
    }

    @Test
    void staleUpdate_cannotOverwriteAnArchivedRow() throws Exception {
        long id = create("STALE");
        TaskEntity stale = repository.findById(id).orElseThrow();
        mvc.perform(delete("/api/v1/task/{id}", id)).andExpect(status().isNoContent());
        stale.update("Stale update", null);

        assertThrows(ObjectOptimisticLockingFailureException.class, () -> repository.saveAndFlush(stale));
        assertEquals("Original", jdbc.queryForObject(
                "SELECT label FROM tasks WHERE id = ?", String.class, id));
    }

    @Test
    void importsAndSeed_createNewRowsForArchivedCodesAndAuditUpserts() throws Exception {
        long oldId = create("IMPORT");
        mvc.perform(delete("/api/v1/task/{id}", oldId)).andExpect(status().isNoContent());
        authenticateAs("importer");
        mvc.perform(post("/api/v1/task/import.json").contentType(MediaType.APPLICATION_JSON)
                .content("[{\"code\":\"IMPORT\",\"label\":\"Imported\",\"createdBy\":\"spoofed\"}]"))
                .andExpect(status().isCreated());
        TaskEntity imported = repository.findByCode("IMPORT").orElseThrow();
        assertNotEquals(oldId, imported.getId());
        assertEquals("importer", imported.getCreatedBy());

        authenticateAs("seeder");
        mvc.perform(post("/api/v1/task/seed").contentType(MediaType.APPLICATION_JSON)
                .content("[{\"code\":\"IMPORT\",\"label\":\"Seeded\"}]")).andExpect(status().isCreated());
        assertEquals("seeder", repository.findByCode("IMPORT").orElseThrow().getLastModifiedBy());
        mvc.perform(delete("/api/v1/task/{id}", imported.getId())).andExpect(status().isNoContent());

        authenticateAs("csvImporter");
        mvc.perform(post("/api/v1/task/import.csv").contentType("text/csv")
                .content("id,code,label,description,createdDate,lastModifiedDate\n"
                        + "999,IMPORT,CSV import,,,\n")).andExpect(status().isCreated());
        TaskEntity csvImported = repository.findByCode("IMPORT").orElseThrow();
        assertNotEquals(imported.getId(), csvImported.getId());
        assertEquals("csvImporter", csvImported.getCreatedBy());
        assertEquals(3, jdbc.queryForObject(
                "SELECT count(*) FROM tasks WHERE code = 'IMPORT'", Integer.class));
    }

    private long create(String code) throws Exception {
        String response = mvc.perform(post("/api/v1/task").contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"" + code + "\",\"label\":\"Original\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(response, "$.id");
        return id.longValue();
    }

    private static void authenticateAs(String username) {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(username, null, List.of()));
    }

    private static String migrationSql() throws IOException {
        return new ClassPathResource("db/manual/task-domains.sql")
                .getContentAsString(StandardCharsets.UTF_8);
    }
}
