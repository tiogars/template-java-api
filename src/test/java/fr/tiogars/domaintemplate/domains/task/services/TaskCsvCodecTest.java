package fr.tiogars.domaintemplate.domains.task.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import fr.tiogars.domaintemplate.domains.task.models.CreateTaskRequest;
import fr.tiogars.domaintemplate.domains.task.models.Task;

public class TaskCsvCodecTest {

        private final TaskCsvCodec codec = new TaskCsvCodec();

        @Test
        public void serializeAndParse_preservesBusinessFieldsAndIgnoresServerFields() {
                Instant timestamp = Instant.parse("2026-01-01T00:00:00Z");
                Task template = new Task(
                                42L, "CUSTOMER", "Customer", "Customers, active", timestamp, timestamp,
                                "creator", "editor");

                String csv = codec.serialize(List.of(template));
                assertEquals("id,code,label,description,createdDate,lastModifiedDate"
                                + ",status,priority,dueDate,assignedTo,tagIds,nomenclatureId",
                                csv.lines().findFirst().orElseThrow());
                List<CreateTaskRequest> parsed = codec.parse(csv);

                assertEquals(List.of(new CreateTaskRequest(
                                "CUSTOMER", "Customer", "Customers, active")), parsed);
        }

        @Test
        public void parse_rejectsInvalidHeader() {
                IllegalArgumentException exception = assertThrows(
                                IllegalArgumentException.class,
                                () -> codec.parse("code,label\nCUSTOMER,Customer\n"));

                assertEquals(
                                "CSV header must be id,code,label,description,createdDate,lastModifiedDate"
                                                + ",status,priority,dueDate,assignedTo,tagIds,nomenclatureId.",
                                exception.getMessage());
        }

        @Test
        void csvRoundtripPreservesTaskAttributesAndReferences() {
                Instant timestamp = Instant.parse("2026-01-01T00:00:00Z");
                var task = new Task(9L, "TASK", "Task", "Multiline\ntext", timestamp, timestamp, "creator", "editor",
                                fr.tiogars.domaintemplate.domains.task.models.TaskStatus.IN_PROGRESS,
                                fr.tiogars.domaintemplate.domains.task.models.TaskPriority.URGENT,
                                java.time.LocalDate.parse("2026-12-31"), "Alice, Smith", List.of(2L, 7L), 5L);
                var request = codec.parse(codec.serialize(List.of(task))).getFirst();
                assertEquals(task.status(), request.status());
                assertEquals(task.priority(), request.priority());
                assertEquals(task.dueDate(), request.dueDate());
                assertEquals(task.assignedTo(), request.assignedTo());
                assertEquals(task.tagIds(), request.tagIds());
                assertEquals(task.nomenclatureId(), request.nomenclatureId());
        }

        @Test
        public void parse_rejectsBlankBusinessFields() {
                IllegalArgumentException exception = assertThrows(
                                IllegalArgumentException.class,
                                () -> codec.parse(
                                                "id,code,label,description,createdDate,lastModifiedDate\n42,,Customer,,,\n"));

                assertEquals("CSV row 2 must contain code and label.", exception.getMessage());
        }
}