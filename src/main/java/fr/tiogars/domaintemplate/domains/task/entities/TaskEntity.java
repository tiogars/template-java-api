package fr.tiogars.domaintemplate.domains.task.entities;

import java.time.Instant;

import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/** Persists a Task business object. */
@Entity
@Table(name = "tasks")
@EntityListeners(AuditingEntityListener.class)
@SQLRestriction("deleted_date IS NULL")
public class TaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private String code;

    @Column(nullable = false)
    private String label;

    private String description;

    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(nullable = false)
    private fr.tiogars.domaintemplate.domains.task.models.TaskStatus status =
            fr.tiogars.domaintemplate.domains.task.models.TaskStatus.TODO;

    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(nullable = false)
    private fr.tiogars.domaintemplate.domains.task.models.TaskPriority priority =
            fr.tiogars.domaintemplate.domains.task.models.TaskPriority.NORMAL;

    private java.time.LocalDate dueDate;

    private String assignedTo;

    @jakarta.persistence.ManyToMany
    @jakarta.persistence.JoinTable(name = "task_tags",
            joinColumns = @jakarta.persistence.JoinColumn(name = "task_id"),
            inverseJoinColumns = @jakarta.persistence.JoinColumn(name = "tag_id"))
    private java.util.List<fr.tiogars.domaintemplate.domains.tag.entities.TagEntity> tags = new java.util.ArrayList<>();

    @jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    private fr.tiogars.domaintemplate.domains.nomenclature.entities.NomenclatureEntity nomenclature;

    public void attributes(fr.tiogars.domaintemplate.domains.task.models.TaskStatus status,
            fr.tiogars.domaintemplate.domains.task.models.TaskPriority priority,
            java.time.LocalDate dueDate, String assignedTo) {
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
        this.assignedTo = assignedTo;
    }

    public void references(java.util.List<fr.tiogars.domaintemplate.domains.tag.entities.TagEntity> tags,
            fr.tiogars.domaintemplate.domains.nomenclature.entities.NomenclatureEntity nomenclature) {
        this.tags = new java.util.ArrayList<>(tags);
        this.nomenclature = nomenclature;
    }

    public fr.tiogars.domaintemplate.domains.task.models.TaskStatus getStatus() {
        return status;
    }

    public fr.tiogars.domaintemplate.domains.task.models.TaskPriority getPriority() {
        return priority;
    }

    public java.time.LocalDate getDueDate() {
        return dueDate;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public java.util.List<Long> getTagIds() {
        return tags.stream().map(fr.tiogars.domaintemplate.domains.tag.entities.TagEntity::getId).sorted().toList();
    }

    public Long getNomenclatureId() {
        return nomenclature == null ? null : nomenclature.getId();
    }

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdDate;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant lastModifiedDate;

    @CreatedBy
    @Column(nullable = false, updatable = false)
    private String createdBy;

    @LastModifiedBy
    @Column(nullable = false)
    private String lastModifiedBy;

    private Instant deletedDate;

    private String deletedBy;

    @Version
    @Column(nullable = false)
    private long version;

    protected TaskEntity() {
        // required by JPA
    }

    public TaskEntity(String code, String label, String description) {
        this.code = code;
        this.label = label;
        this.description = description;
    }

    public void update(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public void softDelete(Instant deletedDate, String deletedBy) {
        this.deletedDate = java.util.Objects.requireNonNull(deletedDate);
        this.deletedBy = java.util.Objects.requireNonNull(deletedBy);
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public Instant getDeletedDate() {
        return deletedDate;
    }

    public String getDeletedBy() {
        return deletedBy;
    }
}