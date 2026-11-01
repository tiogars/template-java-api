package fr.tiogars.domaintemplate.domains.tag.repositories;

import java.util.List;
import java.util.Optional;
import fr.tiogars.domaintemplate.domains.tag.entities.TagEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface TagRepository extends JpaRepository<TagEntity, Long> {
    List<TagEntity> findAllByOrderByCodeAsc();

    boolean existsByCode(String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TagEntity t where t.id = :id")
    Optional<TagEntity> lockById(Long id);

    @Query(value = "select count(*) from task_tags where tag_id = :id", nativeQuery = true)
    long countTaskReferences(Long id);
}
