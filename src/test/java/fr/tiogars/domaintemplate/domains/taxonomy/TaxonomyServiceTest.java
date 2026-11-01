package fr.tiogars.domaintemplate.domains.taxonomy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import fr.tiogars.domaintemplate.domains.tag.entities.TagEntity;
import fr.tiogars.domaintemplate.domains.tag.repositories.TagRepository;
import fr.tiogars.domaintemplate.domains.nomenclature.entities.NomenclatureEntity;
import fr.tiogars.domaintemplate.domains.nomenclature.models.UpdateNomenclatureRequest;
import fr.tiogars.domaintemplate.domains.nomenclature.repositories.NomenclatureRepository;
import fr.tiogars.domaintemplate.domains.task.entities.TaskEntity;
import fr.tiogars.domaintemplate.domains.task.services.TaskReferences;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

class TaxonomyServiceTest {
    private final TagRepository tags = mock(TagRepository.class);
    private final NomenclatureRepository categories = mock(NomenclatureRepository.class);
    private final TaxonomyService service = new TaxonomyService(tags, categories);

    @Test
    void rejectsSelfAndIndirectCycles() {
        NomenclatureEntity root = category(1L);
        NomenclatureEntity child = category(2L);
        child.setParent(root);
        when(categories.findById(1L)).thenReturn(Optional.of(root));
        when(categories.findById(2L)).thenReturn(Optional.of(child));
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> service.updateNomenclature(1L, new UpdateNomenclatureRequest("Root", null, 1L))).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> service.updateNomenclature(1L, new UpdateNomenclatureRequest("Root", null, 2L))).getStatusCode());
        verify(categories, never()).saveAndFlush(root);
    }

    @Test
    void rejectsReferencedTagsCategoriesAndParents() {
        TagEntity tag = new TagEntity("TAG", "Tag", null);
        when(tags.lockById(1L)).thenReturn(Optional.of(tag));
        when(tags.countTaskReferences(1L)).thenReturn(1L);
        assertEquals(HttpStatus.CONFLICT, assertThrows(ResponseStatusException.class,
                () -> service.deleteTag(1L)).getStatusCode());
        when(categories.findById(2L)).thenReturn(Optional.of(category(2L)));
        when(categories.existsByParentId(2L)).thenReturn(true);
        assertEquals(HttpStatus.CONFLICT, assertThrows(ResponseStatusException.class,
                () -> service.deleteNomenclature(2L)).getStatusCode());
        when(categories.findById(3L)).thenReturn(Optional.of(category(3L)));
        when(categories.countTaskReferences(3L)).thenReturn(1L);
        assertEquals(HttpStatus.CONFLICT, assertThrows(ResponseStatusException.class,
                () -> service.deleteNomenclature(3L)).getStatusCode());
    }

    @Test
    void resolvesUniqueTaskTagsAndRejectsMissingReferences() {
        TaskReferences references = new TaskReferences(tags, categories);
        TaskEntity task = new TaskEntity("TASK", "Task", null);
        TagEntity tag = new TagEntity("TAG", "Tag", null);
        ReflectionTestUtils.setField(tag, "id", 7L);
        when(tags.lockById(7L)).thenReturn(Optional.of(tag));
        when(categories.findById(2L)).thenReturn(Optional.of(category(2L)));
        references.apply(task, List.of(7L, 7L), 2L);
        assertEquals(List.of(7L), task.getTagIds());
        assertEquals(2L, task.getNomenclatureId());
        when(tags.lockById(99L)).thenReturn(Optional.empty());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> references.apply(task, List.of(99L), null)).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
                () -> references.apply(task, List.of(), 99L)).getStatusCode());
    }

    private static NomenclatureEntity category(Long id) {
        NomenclatureEntity category = new NomenclatureEntity("CAT" + id, "Category", null);
        ReflectionTestUtils.setField(category, "id", id);
        return category;
    }
}
