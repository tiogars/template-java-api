package fr.tiogars.domaintemplate.settings.user.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.tiogars.domaintemplate.settings.user.entities.UserSettingsEntity;

/** Accesses persisted user and role settings. */
public interface UserSettingsRepository extends JpaRepository<UserSettingsEntity, Long> {

    Optional<UserSettingsEntity> findByUserId(Long userId);

    Optional<UserSettingsEntity> findByRoleCode(String roleCode);
}
