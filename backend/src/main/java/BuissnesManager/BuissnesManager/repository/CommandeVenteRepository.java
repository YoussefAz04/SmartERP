package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.CommandeVente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CommandeVenteRepository extends JpaRepository<CommandeVente, Long> {
    List<CommandeVente> findByEntrepriseId(Long entrepriseId);
    List<CommandeVente> findByStatut(String statut);
    List<CommandeVente> findByEntrepriseIdAndStatut(Long entrepriseId, String statut);
    List<CommandeVente> findByClientId(Long clientId);

    @Query("SELECT c FROM CommandeVente c WHERE c.entreprise.id = :entrepriseId AND c.dateCommande BETWEEN :debut AND :fin")
    List<CommandeVente> findByEntrepriseIdAndDateRange(@Param("entrepriseId") Long entrepriseId, @Param("debut") LocalDate debut, @Param("fin") LocalDate fin);

    @Query("SELECT COALESCE(SUM(c.montantTTC), 0) FROM CommandeVente c WHERE c.entreprise.id = :entrepriseId")
    Double getTotalSalesRevenue(@Param("entrepriseId") Long entrepriseId);

    @Query("SELECT COUNT(c) FROM CommandeVente c WHERE c.entreprise.id = :entrepriseId")
    Long getTotalSalesOrders(@Param("entrepriseId") Long entrepriseId);

    @Query("SELECT c.statut, COUNT(c) FROM CommandeVente c WHERE c.entreprise.id = :entrepriseId GROUP BY c.statut")
    List<Object[]> getOrderStatusBreakdown(@Param("entrepriseId") Long entrepriseId);

    @Query("SELECT COALESCE(SUM(c.montantTTC), 0) FROM CommandeVente c WHERE c.entreprise.id = :entrepriseId AND c.dateCommande BETWEEN :debut AND :fin")
    Double getSalesInPeriod(@Param("entrepriseId") Long entrepriseId, @Param("debut") LocalDate debut, @Param("fin") LocalDate fin);

    @Query("SELECT FUNCTION('MONTH', c.dateCommande), COALESCE(SUM(c.montantTTC), 0) FROM CommandeVente c WHERE c.entreprise.id = :entrepriseId AND FUNCTION('YEAR', c.dateCommande) = :year GROUP BY FUNCTION('MONTH', c.dateCommande) ORDER BY FUNCTION('MONTH', c.dateCommande)")
    List<Object[]> getMonthlySales(@Param("entrepriseId") Long entrepriseId, @Param("year") int year);

    @Query("SELECT c.client.nom, COALESCE(SUM(c.montantTTC), 0), COUNT(c) FROM CommandeVente c WHERE c.entreprise.id = :entrepriseId AND c.client IS NOT NULL GROUP BY c.client.id, c.client.nom ORDER BY SUM(c.montantTTC) DESC")
    List<Object[]> getTopCustomers(@Param("entrepriseId") Long entrepriseId);

    @Query("SELECT lc.articleVente.nom, COALESCE(SUM(lc.quantite), 0), COALESCE(SUM(lc.montantTTC), 0) FROM LigneCommandeVente lc WHERE lc.commandeVente.entreprise.id = :entrepriseId AND lc.articleVente IS NOT NULL GROUP BY lc.articleVente.id, lc.articleVente.nom ORDER BY SUM(lc.quantite) DESC")
    List<Object[]> getTopProducts(@Param("entrepriseId") Long entrepriseId);
}
