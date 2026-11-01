package fr.tiogars.domaintemplate.domains.nomenclature.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreateNomenclatureRequest(@NotBlank String code, @NotBlank String label,
        String description, @Positive Long parentId) {
}
