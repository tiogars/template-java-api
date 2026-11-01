package fr.tiogars.domaintemplate.domains.taxonomy;

import java.util.HashSet;
import java.util.List;

import fr.tiogars.domaintemplate.domains.tag.entities.TagEntity;
import fr.tiogars.domaintemplate.domains.tag.models.CreateTagRequest;
import fr.tiogars.domaintemplate.domains.tag.models.UpdateTagRequest;
import fr.tiogars.domaintemplate.domains.tag.models.Tag;
import fr.tiogars.domaintemplate.domains.tag.repositories.TagRepository;
import fr.tiogars.domaintemplate.domains.nomenclature.entities.NomenclatureEntity;
import fr.tiogars.domaintemplate.domains.nomenclature.models.CreateNomenclatureRequest;
import fr.tiogars.domaintemplate.domains.nomenclature.models.UpdateNomenclatureRequest;
import fr.tiogars.domaintemplate.domains.nomenclature.models.Nomenclature;
import fr.tiogars.domaintemplate.domains.nomenclature.repositories.NomenclatureRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class TaxonomyService {
    private final TagRepository tags;
    private final NomenclatureRepository nomenclatures;

    public TaxonomyService(TagRepository tags, NomenclatureRepository nomenclatures) {
        this.tags = tags;
        this.nomenclatures = nomenclatures;
    }

    public List<Tag> tags() {
        return tags.findAllByOrderByCodeAsc().stream().map(Tag::from).toList();
    }

    public Tag tag(Long id) {
        return Tag.from(tags.findById(id).orElseThrow(() -> missing("Tag")));
    }

    public Tag createTag(CreateTagRequest request) {
        if (tags.existsByCode(request.code())) {
            throw conflict("Tag code already exists.");
        }
        return Tag.from(tags.saveAndFlush(new TagEntity(request.code(), request.label(), request.description())));
    }

    public Tag updateTag(Long id, UpdateTagRequest request) {
        TagEntity entity = tags.lockById(id).orElseThrow(() -> missing("Tag"));
        entity.update(request.label(), request.description());
        return Tag.from(tags.saveAndFlush(entity));
    }

    public void deleteTag(Long id) {
        TagEntity entity = tags.lockById(id).orElseThrow(() -> missing("Tag"));
        if (tags.countTaskReferences(id) > 0) {
            throw conflict("Tag is referenced by a task.");
        }
        tags.delete(entity);
        tags.flush();
    }

    public List<Nomenclature> nomenclatures() {
        return nomenclatures.findAllByOrderByCodeAsc().stream().map(Nomenclature::from).toList();
    }

    public Nomenclature nomenclature(Long id) {
        return Nomenclature.from(nomenclatures.findById(id).orElseThrow(() -> missing("Nomenclature")));
    }

    public Nomenclature createNomenclature(CreateNomenclatureRequest request) {
        nomenclatures.lockForest();
        if (nomenclatures.existsByCode(request.code())) {
            throw conflict("Nomenclature code already exists.");
        }
        NomenclatureEntity entity = new NomenclatureEntity(request.code(), request.label(), request.description());
        entity.setParent(parent(null, request.parentId()));
        return Nomenclature.from(nomenclatures.saveAndFlush(entity));
    }

    public Nomenclature updateNomenclature(Long id, UpdateNomenclatureRequest request) {
        nomenclatures.lockForest();
        NomenclatureEntity entity = nomenclatures.findById(id).orElseThrow(() -> missing("Nomenclature"));
        entity.setParent(parent(id, request.parentId()));
        entity.update(request.label(), request.description());
        return Nomenclature.from(nomenclatures.saveAndFlush(entity));
    }

    public void deleteNomenclature(Long id) {
        nomenclatures.lockForest();
        NomenclatureEntity entity = nomenclatures.findById(id).orElseThrow(() -> missing("Nomenclature"));
        if (nomenclatures.existsByParentId(id) || nomenclatures.countTaskReferences(id) > 0) {
            throw conflict("Nomenclature is referenced by a task or child.");
        }
        nomenclatures.delete(entity);
        nomenclatures.flush();
    }

    private NomenclatureEntity parent(Long id, Long parentId) {
        if (parentId == null) {
            return null;
        }
        NomenclatureEntity parent = nomenclatures.findById(parentId)
                .orElseThrow(() -> invalid("Parent nomenclature does not exist."));
        HashSet<Long> visited = new HashSet<>();
        for (NomenclatureEntity ancestor = parent; ancestor != null; ancestor = ancestor.getParent()) {
            if (ancestor.getId().equals(id) || !visited.add(ancestor.getId())) {
                throw invalid("Nomenclature parent would create a cycle.");
            }
        }
        return parent;
    }

    private static ResponseStatusException missing(String type) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, type + " not found.");
    }

    private static ResponseStatusException invalid(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private static ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }
}
