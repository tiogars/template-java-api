package fr.tiogars.domaintemplate.domains.task.services;

import java.util.List;
import fr.tiogars.domaintemplate.domains.task.entities.TaskEntity;
import fr.tiogars.domaintemplate.domains.tag.repositories.TagRepository;
import fr.tiogars.domaintemplate.domains.nomenclature.repositories.NomenclatureRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class TaskReferences {
    private final TagRepository tags;
    private final NomenclatureRepository nomenclatures;

    public TaskReferences(TagRepository tags, NomenclatureRepository nomenclatures) {
        this.tags = tags;
        this.nomenclatures = nomenclatures;
    }

    public void apply(TaskEntity entity, List<Long> tagIds, Long nomenclatureId) {
        // Lock in a consistent order with taxonomy mutations to prevent reference/deletion races.
        nomenclatures.lockForest();
        var category = nomenclatureId == null ? null : nomenclatures.findById(nomenclatureId)
                .orElseThrow(() -> invalid("Nomenclature does not exist."));
        var selectedTags = tagIds.stream().distinct().sorted()
                .map(id -> tags.lockById(id).orElseThrow(() -> invalid("Tag does not exist: " + id))).toList();
        entity.references(selectedTags, category);
    }

    private static ResponseStatusException invalid(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
