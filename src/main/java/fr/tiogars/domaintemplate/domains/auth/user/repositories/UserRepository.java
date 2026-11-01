package fr.tiogars.domaintemplate.domains.auth.user.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.tiogars.domaintemplate.domains.auth.user.entities.UserEntity;

/** Accesses persisted user accounts. */
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    List<UserEntity> findAllByOrderByUsernameAsc();

    Optional<UserEntity> findByUsername(String username);

    boolean existsByUsername(String username);
}