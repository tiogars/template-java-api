package fr.tiogars.domaintemplate.domains.taxonomy;

import fr.tiogars.domaintemplate.domains.tag.controllers.TagController;
import fr.tiogars.domaintemplate.domains.nomenclature.controllers.NomenclatureController;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = {TagController.class, NomenclatureController.class})
public class TaxonomyConflictHandler {
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Void> conflict(DataIntegrityViolationException exception) {
        return ResponseEntity.status(409).build();
    }
}
