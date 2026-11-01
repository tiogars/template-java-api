package fr.tiogars.domaintemplate.domains.tag.models;

import jakarta.validation.constraints.NotBlank;

public record UpdateTagRequest(@NotBlank String label, String description) {
}
