package fr.tiogars.domaintemplate.domains.task.controllers;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Reports active-code and concurrent-write conflicts without hiding other database errors. */
@RestControllerAdvice(basePackageClasses = TaskCreateController.class)
public class TaskConflictHandler {

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<Void> concurrentWrite(OptimisticLockingFailureException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Void> duplicateActiveCode(DataIntegrityViolationException exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException constraint
                    && "uk_tasks_active_code".equals(constraint.getConstraintName())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        }
        throw exception;
    }
}
