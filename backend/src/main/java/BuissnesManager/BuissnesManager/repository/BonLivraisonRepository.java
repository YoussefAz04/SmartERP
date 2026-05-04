package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.BonLivraison;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface BonLivraisonRepository extends JpaRepository<BonLivraison, Long> {
    List<BonLivraison> findByEntrepriseId(Long entrepriseId);
    List<BonLivraison> findByCommandeVenteId(Long commandeVenteId);
    List<BonLivraison> findByStatut(String statut);

    @Modifying
    @Transactional
    @Query("DELETE FROM BonLivraison b WHERE b.entreprise.id = :entrepriseId")
    void deleteByEntrepriseId(@Param("entrepriseId") Long entrepriseId);
}
