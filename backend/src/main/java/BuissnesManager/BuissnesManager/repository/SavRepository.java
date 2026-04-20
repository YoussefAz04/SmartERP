package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.Sav;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavRepository extends JpaRepository<Sav, Long> {

    List<Sav> findByEntrepriseIdOrderByCreatedAtDesc(Long entrepriseId);

    List<Sav> findByClientIdOrderByCreatedAtDesc(Long clientId);

    List<Sav> findByStatutOrderByCreatedAtDesc(String statut);

    List<Sav> findByEntrepriseIdAndStatutOrderByCreatedAtDesc(Long entrepriseId, String statut);

    @Query("SELECT COUNT(s) FROM Sav s WHERE s.entreprise.id = :entrepriseId")
    long countByEntreprise(@Param("entrepriseId") Long entrepriseId);

    @Query("SELECT s.statut, COUNT(s) FROM Sav s WHERE s.entreprise.id = :entrepriseId GROUP BY s.statut")
    List<Object[]> getStatutBreakdown(@Param("entrepriseId") Long entrepriseId);

    @Query("SELECT s.type, COUNT(s) FROM Sav s WHERE s.entreprise.id = :entrepriseId GROUP BY s.type")
    List<Object[]> getTypeBreakdown(@Param("entrepriseId") Long entrepriseId);

    @Query("SELECT COUNT(s) FROM Sav s WHERE s.reference = :reference")
    long countByReference(@Param("reference") String reference);
}
