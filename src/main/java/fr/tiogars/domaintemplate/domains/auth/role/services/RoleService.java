package fr.tiogars.domaintemplate.domains.auth.role.services;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.tiogars.domaintemplate.domains.auth.role.entities.Role;
import fr.tiogars.domaintemplate.domains.auth.role.entities.RoleEntity;
import fr.tiogars.domaintemplate.domains.auth.role.models.CreateRoleRequest;
import fr.tiogars.domaintemplate.domains.auth.role.models.UpdateRoleRequest;
import fr.tiogars.domaintemplate.domains.auth.role.repositories.RoleRepository;

/** Coordinates role persistence and default role seeding. */
@Service
public class RoleService {

    public static final String ANONYMOUS = "ANONYMOUS";
    public static final String ADMIN = "ADMIN";
    public static final String USER = "USER";

    private static final Set<String> SYSTEM_ROLES = Set.of(ANONYMOUS, ADMIN, USER);

    private final Clock clock;
    private final RoleRepository repository;

    @Autowired
    public RoleService(RoleRepository repository) {
        this(repository, Clock.systemUTC());
    }

    RoleService(RoleRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public Optional<Role> create(CreateRoleRequest request) {
        if (repository.findByCode(request.code()).isPresent()) {
            return Optional.empty();
        }
        Instant timestamp = currentTimestamp();
        return Optional.of(toRecord(repository.save(new RoleEntity(request.code(), request.label(), timestamp, timestamp))));
    }

    @Transactional
    public Optional<Role> update(Long id, UpdateRoleRequest request) {
        return repository.findById(id)
                .map(existing -> {
                    existing.update(request.label(), currentTimestamp());
                    return toRecord(repository.save(existing));
                });
    }

    @Transactional
    public boolean delete(Long id) {
        return repository.findById(id)
                .filter(existing -> !SYSTEM_ROLES.contains(existing.getCode()))
                .map(existing -> {
                    repository.delete(existing);
                    return true;
                })
                .orElse(false);
    }

    public List<Role> findAll() {
        return repository.findAllByOrderByCodeAsc().stream()
                .map(RoleService::toRecord)
                .toList();
    }

    public Optional<Role> findById(Long id) {
        return repository.findById(id).map(RoleService::toRecord);
    }

    public Optional<RoleEntity> findEntityByCode(String code) {
        return repository.findByCode(code);
    }

    @Transactional
    public List<Role> seedDefaultRoles() {
        return List.of(
                seedSystemRole(ANONYMOUS, "Anonymous"),
                seedSystemRole(ADMIN, "Administrator"),
                seedSystemRole(USER, "User"));
    }

    private Role seedSystemRole(String code, String label) {
        Instant timestamp = currentTimestamp();
        RoleEntity entity = repository.findByCode(code)
                .map(existing -> {
                    existing.update(label, timestamp);
                    return existing;
                })
                .orElseGet(() -> new RoleEntity(code, label, timestamp, timestamp));
        return toRecord(repository.save(entity));
    }

    private Instant currentTimestamp() {
        return Instant.now(clock).truncatedTo(ChronoUnit.MICROS);
    }

    public static Role toRecord(RoleEntity entity) {
        return new Role(entity.getId(), entity.getCode(), entity.getLabel(), entity.getCreatedDate(), entity.getLastModifiedDate());
    }
}