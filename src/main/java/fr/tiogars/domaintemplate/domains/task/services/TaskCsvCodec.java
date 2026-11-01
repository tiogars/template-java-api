package fr.tiogars.domaintemplate.domains.task.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import fr.tiogars.domaintemplate.domains.task.models.CreateTaskRequest;
import fr.tiogars.domaintemplate.domains.task.models.Task;

/**
 * Serializes and parses Tasks in the CSV import/export format.
 *
 * @since 0.1.0
 */
@Component
public class TaskCsvCodec {

    private static final String LEGACY_HEADER = "id,code,label,description,createdDate,lastModifiedDate";
    private static final String HEADER = LEGACY_HEADER + ",status,priority,dueDate,assignedTo,tagIds,nomenclatureId";

    /**
     * Serializes records to CSV.
     *
     * @param records records to serialize
     * @return CSV document
     */
    public String serialize(List<Task> records) {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        for (Task template : records) {
            lines.add(String.join(",",
                    escape(String.valueOf(template.id())),
                    escape(template.code()),
                    escape(template.label()),
                    escape(template.description() == null ? "" : template.description()),
                    escape(template.createdDate().toString()),
                    escape(template.lastModifiedDate().toString()),
                    escape(template.status().name()),
                    escape(template.priority().name()),
                    escape(template.dueDate() == null ? "" : template.dueDate().toString()),
                    escape(template.assignedTo() == null ? "" : template.assignedTo()),
                    escape(template.tagIds().stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(";"))),
                    escape(template.nomenclatureId() == null ? "" : template.nomenclatureId().toString())));
        }
        return String.join("\n", lines) + "\n";
    }

    /**
     * Parses records from CSV.
     *
     * @param csv CSV document
     * @return parsed records
     */
    public List<CreateTaskRequest> parse(String csv) {
        List<List<String>> rows = parseRows(csv);
        if (rows.isEmpty()) {
            return List.of();
        }
        String header = String.join(",", rows.getFirst());
        boolean legacy = LEGACY_HEADER.equals(header);
        if (!HEADER.equals(header) && !legacy) {
            throw new IllegalArgumentException("CSV header must be " + HEADER + ".");
        }

        List<CreateTaskRequest> records = new ArrayList<>();
        for (int index = 1; index < rows.size(); index++) {
            List<String> row = rows.get(index);
            if (row.size() == 1 && row.getFirst().isBlank()) {
                continue;
            }
            if (row.size() != (legacy ? 6 : 12)) {
                throw new IllegalArgumentException("CSV row " + (index + 1) + " has an invalid number of columns.");
            }
            try {
                records.add(parseRecord(row, index + 1));
            } catch (java.time.DateTimeException exception) {
                throw new IllegalArgumentException("CSV row " + (index + 1) + " has an invalid dueDate.", exception);
            }
        }
        return records;
    }

    private CreateTaskRequest parseRecord(List<String> row, int lineNumber) {
        String code = row.get(1).trim();
        String label = row.get(2).trim();
        if (code.isBlank() || label.isBlank()) {
            throw new IllegalArgumentException("CSV row " + lineNumber + " must contain code and label.");
        }
        String description = row.get(3).isBlank() ? null : row.get(3);
        if (row.size() == 6) {
            return new CreateTaskRequest(code, label, description);
        }
        List<Long> tagIds = row.get(10).isBlank() ? List.of()
                : java.util.Arrays.stream(row.get(10).split(";")).map(String::trim).map(Long::valueOf).toList();
        Long nomenclatureId = row.get(11).isBlank() ? null : Long.valueOf(row.get(11));
        if (tagIds.stream().anyMatch(id -> id <= 0) || (nomenclatureId != null && nomenclatureId <= 0)) {
            throw new IllegalArgumentException("Reference identifiers must be positive.");
        }
        return new CreateTaskRequest(code, label, description,
                row.get(6).isBlank() ? null
                        : fr.tiogars.domaintemplate.domains.task.models.TaskStatus.valueOf(row.get(6)),
                row.get(7).isBlank() ? null
                        : fr.tiogars.domaintemplate.domains.task.models.TaskPriority.valueOf(row.get(7)),
                row.get(8).isBlank() ? null : java.time.LocalDate.parse(row.get(8)),
                row.get(9).isBlank() ? null : row.get(9), tagIds, nomenclatureId);
    }

    private static List<List<String>> parseRows(String csv) {
        List<List<String>> rows = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;

        for (int index = 0; index < csv.length(); index++) {
            char character = csv.charAt(index);
            if (quoted) {
                if (character == '"') {
                    if (index + 1 < csv.length() && csv.charAt(index + 1) == '"') {
                        field.append('"');
                        index++;
                    } else {
                        quoted = false;
                    }
                } else {
                    field.append(character);
                }
            } else if (character == '"') {
                quoted = true;
            } else if (character == ',') {
                row.add(field.toString());
                field.setLength(0);
            } else if (character == '\n') {
                row.add(field.toString());
                rows.add(row);
                row = new ArrayList<>();
                field.setLength(0);
            } else if (character != '\r') {
                field.append(character);
            }
        }

        if (quoted) {
            throw new IllegalArgumentException("CSV contains an unterminated quoted field.");
        }
        if (!field.isEmpty() || !row.isEmpty()) {
            row.add(field.toString());
            rows.add(row);
        }
        return rows;
    }

    private static String escape(String value) {
        if (value.contains(",") || value.contains("\n") || value.contains("\r") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}