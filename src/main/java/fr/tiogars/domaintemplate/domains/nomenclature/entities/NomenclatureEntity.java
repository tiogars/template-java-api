package fr.tiogars.domaintemplate.domains.nomenclature.entities;

import fr.tiogars.domaintemplate.domains.taxonomy.AuditedTaxonomyEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;

@Entity
@Table(name = "nomenclatures")
public class NomenclatureEntity extends AuditedTaxonomyEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    private NomenclatureEntity parent;

    protected NomenclatureEntity() {
        // Required by JPA.
    }

    public NomenclatureEntity(String code, String label, String description) {
        super(code, label, description);
    }

    public NomenclatureEntity getParent() {
        return parent;
    }

    public void setParent(NomenclatureEntity parent) {
        this.parent = parent;
    }

    public Long getParentId() {
        return parent == null ? null : parent.getId();
    }
}
