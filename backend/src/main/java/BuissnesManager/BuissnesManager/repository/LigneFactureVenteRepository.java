package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.LigneFactureVente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface LigneFactureVenteRepository extends JpaRepository<LigneFactureVente, Long> {
    List<LigneFactureVente> findByFactureVenteId(Long factureVenteId);

    @Transactional
    void deleteByFactureVenteId(Long factureVenteId);

    @Modifying
    @Transactional
    @Query("DELETE FROM LigneFactureVente l WHERE l.factureVente.entreprise.id = :entrepriseId")
    void deleteByFactureVenteEntrepriseId(@Param("entrepriseId") Long entrepriseId);
}
