package fr.tiogars.domaintemplate.domains.auth.user.services;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.tiogars.domaintemplate.domains.auth.role.entities.RoleEntity;
import fr.tiogars.domaintemplate.domains.auth.role.services.RoleService;
import fr.tiogars.domaintemplate.domains.auth.user.entities.User;
import fr.tiogars.domaintemplate.domains.auth.user.entities.UserEntity;
import fr.tiogars.domaintemplate.domains.auth.user.models.CreateUserRequest;
import fr.tiogars.domaintemplate.domains.auth.user.models.UpdateUserRequest;
import fr.tiogars.domaintemplate.domains.auth.user.repositories.UserRepository;

/** Coordinates user account persistence and password hashing. */
@Service
public class UserService {

    private final Clock clock;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;
    private final UserRepository repository;

    @Autowired
    public UserService(UserRepository repository, RoleService roleService, PasswordEncoder passwordEncoder) {
        this(repository, roleService, passwordEncoder, Clock.systemUTC());
    }

    UserService(UserRepository repository, RoleService roleService, PasswordEncoder passwordEncoder, Clock clock) {
        this.repository = repository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    @Transactional
    public Optional<User> create(CreateUserRequest request) {
        if (repository.findByUsername(request.username()).isPresent()) {
            return Optional.empty();
        }
        Instant timestamp = currentTimestamp();
        UserEntity entity = new UserEntity(
                request.username(),
                passwordEncoder.encode(request.password()),
                request.enabled() == null || request.enabled(),
                resolveRoles(request.roles()),
                timestamp,
                timestamp);
        return Optional.of(toRecord(repository.save(entity)));
    }

    @Transactional
    public Optional<User> update(Long id, UpdateUserRequest request) {
        return repository.findById(id)
                .filter(existing -> repository.findByUsername(request.username())
                        .map(found -> found == existing || Objects.equals(found.getId(), existing.getId()))
                        .orElse(true))
                .map(existing -> {
                    String passwordHash = request.password() == null || request.password().isBlank()
                            ? null
                            : passwordEncoder.encode(request.password());
                    existing.update(request.username(), passwordHash, request.enabled(), resolveRoles(request.roles()),
                            currentTimestamp());
                    return toRecord(repository.save(existing));
                });
    }

    @Transactional
    public boolean delete(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }

    public List<User> findAll() {
        return repository.findAllByOrderByUsernameAsc().stream()
                .map(UserService::toRecord)
                .toList();
    }

    public Optional<User> findById(Long id) {
        return repository.findById(id).map(UserService::toRecord);
    }

    public Optional<User> findByUsername(String username) {
        return repository.findByUsername(username).map(UserService::toRecord);
    }

    @Transactional
    public User forceAdminUser(String username, String password) {
        RoleEntity adminRole = roleService.findEntityByCode(RoleService.ADMIN).orElseThrow();
        Instant timestamp = currentTimestamp();
        UserEntity entity = repository.findByUsername(username)
                .map(existing -> {
                    existing.update(username, passwordEncoder.encode(password), true, Set.of(adminRole), timestamp);
                    return existing;
                })
                .orElseGet(() -> new UserEntity(
                        username,
                        passwordEncoder.encode(password),
                        true,
                        Set.of(adminRole),
                        timestamp,
                        timestamp));
        return toRecord(repository.save(entity));
    }

    private Set<RoleEntity> resolveRoles(Set<String> roleCodes) {
        Set<RoleEntity> roles = new LinkedHashSet<>();
        for (String roleCode : roleCodes) {
            roles.add(roleService.findEntityByCode(roleCode).orElseThrow(
                    () -> new IllegalArgumentException("Unknown role: " + roleCode)));
        }
        return roles;
    }

    private Instant currentTimestamp() {
        return Instant.now(clock).truncatedTo(ChronoUnit.MICROS);
    }

    public static User toRecord(UserEntity entity) {
        return new User(
                entity.getId(),
                entity.getUsername(),
                entity.isEnabled(),
                entity.getRoles().stream()
                        .sorted(Comparator.comparing(role -> role.getCode()))
                        .map(RoleService::toRecord)
                        .toList(),
                entity.getCreatedDate(),
                entity.getLastModifiedDate());
    }
}