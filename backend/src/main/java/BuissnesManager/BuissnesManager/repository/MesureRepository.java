package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.Mesure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface MesureRepository extends JpaRepository<Mesure, Long> {
    List<Mesure> findByEntrepriseId(Long entrepriseId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Mesure m WHERE m.entreprise.id = :entrepriseId")
    void deleteByEntrepriseId(@Param("entrepriseId") Long entrepriseId);
}
