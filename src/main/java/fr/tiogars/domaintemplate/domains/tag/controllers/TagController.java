package fr.tiogars.domaintemplate.domains.tag.controllers;

import java.net.URI;
import java.util.List;
import fr.tiogars.domaintemplate.domains.tag.models.Tag;
import fr.tiogars.domaintemplate.domains.tag.models.CreateTagRequest;
import fr.tiogars.domaintemplate.domains.tag.models.UpdateTagRequest;
import fr.tiogars.domaintemplate.domains.taxonomy.TaxonomyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/v1/tag")
public class TagController {
    private final TaxonomyService service;

    public TagController(TaxonomyService service) {
        this.service = service;
    }

    @GetMapping
    public List<Tag> findAll() {
        return service.tags();
    }

    @GetMapping("/{id}")
    public Tag findById(@PathVariable Long id) {
        return service.tag(id);
    }

    @PostMapping
    public ResponseEntity<Tag> create(@Valid @RequestBody CreateTagRequest request) {
        Tag created = service.createTag(request);
        return ResponseEntity.created(URI.create("/api/v1/tag/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public Tag update(@PathVariable Long id, @Valid @RequestBody UpdateTagRequest request) {
        return service.updateTag(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
}
