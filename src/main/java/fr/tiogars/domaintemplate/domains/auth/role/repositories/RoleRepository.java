package fr.tiogars.domaintemplate.domains.auth.role.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.tiogars.domaintemplate.domains.auth.role.entities.RoleEntity;

/** Accesses persisted authorization roles. */
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    List<RoleEntity> findAllByOrderByCodeAsc();

    Optional<RoleEntity> findByCode(String code);
}