package fr.tiogars.domaintemplate.domains.task.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import fr.tiogars.domaintemplate.domains.task.models.Task;

/** Renders printable Task documents with wrapping and A4 pagination. */
@Component
public class TaskPdfRenderer {

    private static final String FONT_RESOURCE = "/org/apache/pdfbox/resources/ttf/LiberationSans-Regular.ttf";
    private static final float MARGIN = 48;
    private static final float FONT_SIZE = 11;
    private static final float LINE_HEIGHT = 16;
    private static final float TEXT_WIDTH = PDRectangle.A4.getWidth() - 2 * MARGIN;

    /**
     * Renders one record per page, continuing long records on additional pages.
     *
     * @param title document heading
     * @param templates records to print, in display order
     * @return PDF bytes
     * @throws IOException when the PDF cannot be generated
     */
    public byte[] render(String title, List<Task> templates) throws IOException {
        try (PDDocument document = new PDDocument();
                InputStream fontStream = TaskPdfRenderer.class.getResourceAsStream(FONT_RESOURCE);
                ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            if (fontStream == null) {
                throw new IOException("The bundled PDF font is unavailable.");
            }
            PDType0Font font = PDType0Font.load(document, fontStream);
            document.getDocumentInformation().setTitle(title);
            try (PageWriter writer = new PageWriter(document, font, title)) {
                if (templates.isEmpty()) {
                    writer.newPage();
                    writer.field("Records", "No Tasks.");
                }
                for (Task template : templates) {
                    writer.newPage();
                    writer.field("ID", template.id());
                    writer.field("Code", template.code());
                    writer.field("Label", template.label());
                    writer.field("Description", template.description());
                    writer.field("Status", template.status());
                    writer.field("Priority", template.priority());
                    writer.field("Due date", template.dueDate());
                    writer.field("Assigned to", template.assignedTo());
                    writer.field("Tag IDs", template.tagIds());
                    writer.field("Nomenclature ID", template.nomenclatureId());
                    writer.field("Created", template.createdDate());
                    writer.field("Created by", template.createdBy());
                    writer.field("Last modified", template.lastModifiedDate());
                    writer.field("Last modified by", template.lastModifiedBy());
                }
            }
            document.save(output);
            return output.toByteArray();
        }
    }

    private static final class PageWriter implements AutoCloseable {

        private final PDDocument document;
        private final PDType0Font font;
        private final String title;
        private PDPageContentStream stream;
        private float y;

        private PageWriter(PDDocument document, PDType0Font font, String title) {
            this.document = document;
            this.font = font;
            this.title = title;
        }

        private void newPage() throws IOException {
            close();
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            stream = new PDPageContentStream(document, page);
            text(title, 18, page.getMediaBox().getHeight() - MARGIN);
            text("Page " + document.getNumberOfPages(), 9, MARGIN / 2);
            y = page.getMediaBox().getHeight() - MARGIN - 36;
        }

        private void field(String label, Object value) throws IOException {
            String content = label + ": " + (value == null ? "Not available" : value);
            for (String paragraph : content.replace("\t", "    ").split("\\R", -1)) {
                StringBuilder line = new StringBuilder();
                float width = 0;
                for (int codePoint : paragraph.codePoints().toArray()) {
                    String character = new String(Character.toChars(codePoint));
                    float characterWidth = characterWidth(character);
                    if (width + characterWidth > TEXT_WIDTH && !line.isEmpty()) {
                        String currentLine = line.toString();
                        int wordBreak = currentLine.lastIndexOf(' ');
                        line(wordBreak > 0 ? currentLine.substring(0, wordBreak) : currentLine);
                        line.setLength(0);
                        if (wordBreak > 0) {
                            line.append(currentLine.substring(wordBreak + 1));
                        }
                        width = font.getStringWidth(line.toString()) * FONT_SIZE / 1000;
                    }
                    line.append(character);
                    width += characterWidth;
                }
                line(line.toString());
            }
            y -= LINE_HEIGHT / 2;
        }

        private float characterWidth(String character) throws IOException {
            try {
                return font.getStringWidth(character) * FONT_SIZE / 1000;
            } catch (IllegalArgumentException exception) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT,
                        "A Task contains characters unsupported by the PDF font.", exception);
            }
        }

        private void line(String value) throws IOException {
            if (y < MARGIN) {
                newPage();
            }
            text(value, FONT_SIZE, y);
            y -= LINE_HEIGHT;
        }

        private void text(String value, float size, float baseline) throws IOException {
            stream.beginText();
            stream.setFont(font, size);
            stream.newLineAtOffset(MARGIN, baseline);
            stream.showText(value);
            stream.endText();
        }

        @Override
        public void close() throws IOException {
            if (stream != null) {
                stream.close();
                stream = null;
            }
        }
    }
}
