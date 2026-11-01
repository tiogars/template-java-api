package fr.tiogars.domaintemplate.domains.tag.models;

import java.time.Instant;
import fr.tiogars.domaintemplate.domains.tag.entities.TagEntity;

public record Tag(Long id, String code, String label, String description, Instant createdDate,
        Instant lastModifiedDate, String createdBy, String lastModifiedBy) {
    public static Tag from(TagEntity entity) {
        return new Tag(entity.getId(), entity.getCode(), entity.getLabel(), entity.getDescription(),
                entity.getCreatedDate(), entity.getLastModifiedDate(), entity.getCreatedBy(), entity.getLastModifiedBy());
    }
}
