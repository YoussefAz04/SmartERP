package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.dto.DashboardHomeSummaryDto;
import BuissnesManager.BuissnesManager.entity.Fournisseur;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface FournisseurRepo extends JpaRepository<Fournisseur, Long> {
    List<Fournisseur> findByNomContainingIgnoreCase(String nom);
    List<Fournisseur> findByEntrepriseId(Long entrepriseId);
    Optional<Fournisseur> findByIdAndEntrepriseId(Long id, Long entrepriseId);

    @Query("SELECT COUNT(f) FROM Fournisseur f WHERE f.entreprise.id = :entrepriseId")
    long countByEntrepriseId(@Param("entrepriseId") Long entrepriseId);

    /**
     * Slim projection for the home dashboard "fournisseurs récents" panel.
     */
    @Query("SELECT new BuissnesManager.BuissnesManager.dto.DashboardHomeSummaryDto$RecentFournisseurDto(" +
           "f.id, f.nom, f.telephone, f.ville) " +
           "FROM Fournisseur f WHERE f.entreprise.id = :entrepriseId ORDER BY f.id DESC")
    List<DashboardHomeSummaryDto.RecentFournisseurDto> findRecentByEntreprise(
            @Param("entrepriseId") Long entrepriseId, Pageable pageable);

    @Modifying
    @Transactional
    @Query("DELETE FROM Fournisseur f WHERE f.entreprise.id = :entrepriseId")
    void deleteByEntrepriseId(@Param("entrepriseId") Long entrepriseId);
}
