package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.FactureVente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface FactureVenteRepository extends JpaRepository<FactureVente, Long> {
    Optional<FactureVente> findFirstByCommandeVenteId(Long commandeVenteId);
    List<FactureVente> findAllByCommandeVenteId(Long commandeVenteId);
    List<FactureVente> findByEntrepriseId(Long entrepriseId);
    List<FactureVente> findByClientId(Long clientId);
    List<FactureVente> findByStatut(String statut);
    List<FactureVente> findByEntrepriseIdAndStatut(Long entrepriseId, String statut);

    @Query("SELECT COALESCE(SUM(f.montantTTC), 0) FROM FactureVente f WHERE f.entreprise.id = :entrepriseId AND f.statut = 'PAYEE'")
    Double getTotalRevenuePaid(@Param("entrepriseId") Long entrepriseId);

    @Query("SELECT COALESCE(SUM(f.montantTTC), 0) FROM FactureVente f WHERE f.entreprise.id = :entrepriseId AND f.statut != 'PAYEE'")
    Double getAccountsReceivable(@Param("entrepriseId") Long entrepriseId);

    @Transactional
    void deleteByEntrepriseId(Long entrepriseId);
}
