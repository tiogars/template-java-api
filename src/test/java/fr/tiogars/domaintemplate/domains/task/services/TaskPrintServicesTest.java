package fr.tiogars.domaintemplate.domains.task.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import fr.tiogars.domaintemplate.domains.task.entities.TaskEntity;
import fr.tiogars.domaintemplate.domains.task.repositories.TaskRepository;

class TaskPrintServicesTest {

    private final TaskPdfRenderer renderer = new TaskPdfRenderer();

    @Test
    void print_should_renderOnlyTheRequestedRecord() throws Exception {
        TaskFindByIdService finder = mock(TaskFindByIdService.class);
        when(finder.findById(42L)).thenReturn(Optional.of(
                TaskPdfRendererTest.template(42L, "CURRENT", "Current", null)));

        byte[] bytes = new TaskPrintService(finder, renderer).print(42L).orElseThrow();

        assertThat(text(bytes)).contains("ID: 42", "Code: CURRENT");
        verify(finder).findById(42L);
    }

    @Test
    void print_should_reportMissingOrArchivedRecord() throws Exception {
        TaskFindByIdService finder = mock(TaskFindByIdService.class);
        when(finder.findById(99L)).thenReturn(Optional.empty());

        assertThat(new TaskPrintService(finder, renderer).print(99L)).isEmpty();
    }

    @Test
    void printAll_should_renderAllRecordsInFinderOrder() throws Exception {
        TaskFindAllService finder = mock(TaskFindAllService.class);
        when(finder.findAll()).thenReturn(List.of(
                TaskPdfRendererTest.template(1L, "FIRST", "First", null),
                TaskPdfRendererTest.template(2L, "SECOND", "Second", null)));

        String text = text(new TasksPrintAllService(finder, renderer).printAll());

        assertThat(text).contains("Code: FIRST", "Code: SECOND");
        assertThat(text.indexOf("Code: FIRST")).isLessThan(text.indexOf("Code: SECOND"));
    }

    @Test
    void printSelected_should_queryOnlyDistinctSelectedIdsAndPrintInCodeOrder() throws Exception {
        TaskRepository repository = mock(TaskRepository.class);
        List<TaskEntity> entities = List.of(entity(1L, "FIRST"), entity(2L, "SECOND"));
        when(repository.findAllByIdInOrderByCodeAsc(List.of(2L, 1L)))
                .thenReturn(entities);

        byte[] bytes = new TasksPrintSelectedService(repository, renderer)
                .printSelected(List.of(2L, 1L, 2L)).orElseThrow();

        String text = text(bytes);
        assertThat(text).contains("Selected Tasks", "Code: FIRST", "Code: SECOND");
        assertThat(text.indexOf("Code: FIRST")).isLessThan(text.indexOf("Code: SECOND"));
        try (PDDocument pdf = Loader.loadPDF(bytes)) {
            assertThat(pdf.getNumberOfPages()).isEqualTo(2);
        }
        verify(repository).findAllByIdInOrderByCodeAsc(List.of(2L, 1L));
    }

    @Test
    void printSelected_should_rejectPartialSelectionWhenARecordIsMissingOrArchived() throws Exception {
        TaskRepository repository = mock(TaskRepository.class);
        List<TaskEntity> entities = List.of(entity(1L, "FIRST"));
        when(repository.findAllByIdInOrderByCodeAsc(List.of(1L, 99L))).thenReturn(entities);

        assertThat(new TasksPrintSelectedService(repository, renderer)
                .printSelected(List.of(1L, 99L))).isEmpty();
    }

    @Test
    void printSelected_should_neverTreatEmptySelectionAsPrintAll() {
        TaskRepository repository = mock(TaskRepository.class);
        assertThatThrownBy(() -> new TasksPrintSelectedService(repository, renderer).printSelected(List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static TaskEntity entity(Long id, String code) {
        TaskEntity entity = mock(TaskEntity.class);
        when(entity.getId()).thenReturn(id);
        when(entity.getCode()).thenReturn(code);
        when(entity.getLabel()).thenReturn(code);
        return entity;
    }

    private static String text(byte[] bytes) throws Exception {
        try (PDDocument pdf = Loader.loadPDF(bytes)) {
            return new PDFTextStripper().getText(pdf);
        }
    }
}
