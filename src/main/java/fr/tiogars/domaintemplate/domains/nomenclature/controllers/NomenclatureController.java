package fr.tiogars.domaintemplate.domains.nomenclature.controllers;

import java.net.URI;
import java.util.List;
import fr.tiogars.domaintemplate.domains.nomenclature.models.Nomenclature;
import fr.tiogars.domaintemplate.domains.nomenclature.models.CreateNomenclatureRequest;
import fr.tiogars.domaintemplate.domains.nomenclature.models.UpdateNomenclatureRequest;
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
@RequestMapping("/api/v1/nomenclature")
public class NomenclatureController {
    private final TaxonomyService service;

    public NomenclatureController(TaxonomyService service) {
        this.service = service;
    }

    @GetMapping
    public List<Nomenclature> findAll() {
        return service.nomenclatures();
    }

    @GetMapping("/{id}")
    public Nomenclature findById(@PathVariable Long id) {
        return service.nomenclature(id);
    }

    @PostMapping
    public ResponseEntity<Nomenclature> create(@Valid @RequestBody CreateNomenclatureRequest request) {
        Nomenclature created = service.createNomenclature(request);
        return ResponseEntity.created(URI.create("/api/v1/nomenclature/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public Nomenclature update(@PathVariable Long id, @Valid @RequestBody UpdateNomenclatureRequest request) {
        return service.updateNomenclature(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteNomenclature(id);
        return ResponseEntity.noContent().build();
    }
}
