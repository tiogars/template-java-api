package fr.tiogars.domaintemplate.domains.task.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import fr.tiogars.domaintemplate.domains.task.models.Task;

class TaskPdfRendererTest {

    private final TaskPdfRenderer renderer = new TaskPdfRenderer();

    @Test
    void render_should_includeBusinessAndAuditFieldsWithAccentedText() throws Exception {
        Task template = template(42L, "CUSTOMER", "Caf\u00e9", "First line\nSecond line");

        try (PDDocument pdf = Loader.loadPDF(renderer.render("Task", List.of(template)))) {
            String text = new PDFTextStripper().getText(pdf);
            assertThat(pdf.getNumberOfPages()).isEqualTo(1);
            assertThat(text).contains("Task", "ID: 42", "Code: CUSTOMER", "Label: Caf\u00e9",
                    "Description: First line", "Second line", "Created: 2026-08-01T10:00:00Z",
                    "Created by: creator", "Last modified: 2026-08-02T10:00:00Z", "Last modified by: editor", "Page 1");
            assertThat(pdf.getPage(0).getResources().getFont(
                    pdf.getPage(0).getResources().getFontNames().iterator().next()).isEmbedded()).isTrue();
        }
    }

    @Test
    void render_should_preserveGreekAndCyrillicText() throws Exception {
        String label = "\u0391\u03b8\u03ae\u03bd\u03b1 \u041c\u043e\u0441\u043a\u0432\u0430";
        try (PDDocument pdf = Loader.loadPDF(renderer.render("Task",
                List.of(template(1L, "UNICODE", label, null))))) {
            assertThat(new PDFTextStripper().getText(pdf)).contains(label, "Description: Not available");
        }
    }

    @Test
    void render_should_wrapLongUnbrokenFieldsAndPaginateWithoutLosingContent() throws Exception {
        String description = "x".repeat(12000) + "\nEND OF DESCRIPTION";

        try (PDDocument pdf = Loader.loadPDF(renderer.render("Task",
                List.of(template(1L, "LONG", "Long template", description))))) {
            String text = new PDFTextStripper().getText(pdf);
            assertThat(pdf.getNumberOfPages()).isGreaterThan(1);
            assertThat(text.chars().filter(value -> value == 'x').count()).isEqualTo(12000);
            assertThat(text).contains("END OF DESCRIPTION", "Last modified by: editor",
                    "Page " + pdf.getNumberOfPages());
        }
    }

    @Test
    void render_should_startEachRecordOnANewPage() throws Exception {
        try (PDDocument pdf = Loader.loadPDF(renderer.render("Tasks",
                List.of(template(1L, "FIRST", "First", null), template(2L, "SECOND", "Second", null))))) {
            assertThat(pdf.getNumberOfPages()).isEqualTo(2);
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(1);
            stripper.setEndPage(1);
            assertThat(stripper.getText(pdf)).contains("Code: FIRST").doesNotContain("Code: SECOND");
        }
    }

    @Test
    void render_should_createPrintableDocumentForEmptyCollection() throws Exception {
        try (PDDocument pdf = Loader.loadPDF(renderer.render("Tasks", List.of()))) {
            assertThat(pdf.getNumberOfPages()).isEqualTo(1);
            assertThat(new PDFTextStripper().getText(pdf)).contains("No Tasks.");
        }
    }

    @Test
    void render_should_reportUnsupportedGlyphsInsteadOfReplacingText() {
        assertThatThrownBy(() -> renderer.render("Task",
                List.of(template(1L, "EMOJI", "\ud83d\ude00", null))))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("422")
                .hasMessageContaining("unsupported");
    }

    static Task template(Long id, String code, String label, String description) {
        return new Task(id, code, label, description,
                Instant.parse("2026-08-01T10:00:00Z"), Instant.parse("2026-08-02T10:00:00Z"), "creator", "editor");
    }
}
