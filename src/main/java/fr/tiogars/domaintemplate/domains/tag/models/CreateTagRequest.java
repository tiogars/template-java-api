package fr.tiogars.domaintemplate.domains.tag.models;

import jakarta.validation.constraints.NotBlank;

public record CreateTagRequest(@NotBlank String code, @NotBlank String label, String description) {
}
