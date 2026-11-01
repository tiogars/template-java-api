package fr.tiogars.domaintemplate.domains.nomenclature.models;

import java.time.Instant;
import fr.tiogars.domaintemplate.domains.nomenclature.entities.NomenclatureEntity;

public record Nomenclature(Long id, String code, String label, String description, Instant createdDate,
        Instant lastModifiedDate, String createdBy, String lastModifiedBy, Long parentId) {
    public static Nomenclature from(NomenclatureEntity entity) {
        return new Nomenclature(entity.getId(), entity.getCode(), entity.getLabel(), entity.getDescription(),
                entity.getCreatedDate(), entity.getLastModifiedDate(), entity.getCreatedBy(), entity.getLastModifiedBy(),
                entity.getParentId());
    }
}
