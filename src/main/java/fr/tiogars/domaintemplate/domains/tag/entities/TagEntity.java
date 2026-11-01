package fr.tiogars.domaintemplate.domains.tag.entities;

import fr.tiogars.domaintemplate.domains.taxonomy.AuditedTaxonomyEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "tags")
public class TagEntity extends AuditedTaxonomyEntity {
    protected TagEntity() {
        // Required by JPA.
    }

    public TagEntity(String code, String label, String description) {
        super(code, label, description);
    }
}
