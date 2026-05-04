package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.BonReception;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface BonReceptionRepository extends JpaRepository<BonReception, Long> {
    List<BonReception> findByEntrepriseId(Long entrepriseId);
    List<BonReception> findByCommandeAchatId(Long commandeAchatId);
    List<BonReception> findByStatut(String statut);

    @Modifying
    @Transactional
    @Query("DELETE FROM BonReception b WHERE b.entreprise.id = :entrepriseId")
    void deleteByEntrepriseId(@Param("entrepriseId") Long entrepriseId);
}
