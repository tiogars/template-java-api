package fr.tiogars.domaintemplate.domains.nomenclature.repositories;

import java.util.List;
import fr.tiogars.domaintemplate.domains.nomenclature.entities.NomenclatureEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface NomenclatureRepository extends JpaRepository<NomenclatureEntity, Long> {
    List<NomenclatureEntity> findAllByOrderByCodeAsc();

    boolean existsByCode(String code);

    @Query("select count(n) > 0 from NomenclatureEntity n where n.parent.id = :parentId")
    boolean existsByParentId(Long parentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select n from NomenclatureEntity n order by n.id")
    List<NomenclatureEntity> lockForest();

    @Query(value = "select count(*) from tasks where nomenclature_id = :id", nativeQuery = true)
    long countTaskReferences(Long id);
}
