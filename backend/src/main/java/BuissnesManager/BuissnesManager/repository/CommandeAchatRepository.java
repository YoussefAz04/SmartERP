package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.CommandeAchat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CommandeAchatRepository extends JpaRepository<CommandeAchat, Long> {
    List<CommandeAchat> findByEntrepriseId(Long entrepriseId);
    List<CommandeAchat> findByStatut(String statut);
    List<CommandeAchat> findByEntrepriseIdAndStatut(Long entrepriseId, String statut);
    List<CommandeAchat> findByFournisseurId(Long fournisseurId);

    @Modifying
    @Query("UPDATE CommandeAchat c SET c.fournisseur = null WHERE c.fournisseur.id = :fournisseurId")
    void detachFournisseur(@Param("fournisseurId") Long fournisseurId);

    @Query("SELECT c FROM CommandeAchat c WHERE c.entreprise.id = :entrepriseId AND c.dateCommande BETWEEN :debut AND :fin")
    List<CommandeAchat> findByEntrepriseIdAndDateRange(@Param("entrepriseId") Long entrepriseId, @Param("debut") LocalDate debut, @Param("fin") LocalDate fin);

    @Query("SELECT COALESCE(SUM(c.montantTTC), 0) FROM CommandeAchat c WHERE c.entreprise.id = :entrepriseId")
    Double getTotalPurchases(@Param("entrepriseId") Long entrepriseId);

    @Query("SELECT COUNT(c) FROM CommandeAchat c WHERE c.entreprise.id = :entrepriseId")
    Long getTotalPurchaseOrders(@Param("entrepriseId") Long entrepriseId);

    @Query("SELECT c.statut, COUNT(c) FROM CommandeAchat c WHERE c.entreprise.id = :entrepriseId GROUP BY c.statut")
    List<Object[]> getPurchaseOrderStatusBreakdown(@Param("entrepriseId") Long entrepriseId);

    @Query("SELECT COALESCE(SUM(c.montantTTC), 0) FROM CommandeAchat c WHERE c.entreprise.id = :entrepriseId AND c.dateCommande BETWEEN :debut AND :fin")
    Double getPurchasesInPeriod(@Param("entrepriseId") Long entrepriseId, @Param("debut") LocalDate debut, @Param("fin") LocalDate fin);

    @Query("SELECT FUNCTION('MONTH', c.dateCommande), COALESCE(SUM(c.montantTTC), 0) FROM CommandeAchat c WHERE c.entreprise.id = :entrepriseId AND FUNCTION('YEAR', c.dateCommande) = :year GROUP BY FUNCTION('MONTH', c.dateCommande) ORDER BY FUNCTION('MONTH', c.dateCommande)")
    List<Object[]> getMonthlyPurchases(@Param("entrepriseId") Long entrepriseId, @Param("year") int year);

    @Query("SELECT c.fournisseur.id, c.fournisseur.nom, COALESCE(c.fournisseur.prenom, ''), COUNT(c), COALESCE(SUM(c.montantTTC), 0), COALESCE(c.fournisseur.tauxLivraison, 0), COALESCE(c.fournisseur.notation, 0) FROM CommandeAchat c WHERE c.entreprise.id = :entrepriseId AND c.fournisseur IS NOT NULL GROUP BY c.fournisseur.id, c.fournisseur.nom, c.fournisseur.prenom, c.fournisseur.tauxLivraison, c.fournisseur.notation ORDER BY SUM(c.montantTTC) DESC")
    List<Object[]> getSupplierMetrics(@Param("entrepriseId") Long entrepriseId);

    @Query("SELECT c.fournisseur.id, c.statut, COUNT(c) FROM CommandeAchat c WHERE c.entreprise.id = :entrepriseId AND c.fournisseur IS NOT NULL GROUP BY c.fournisseur.id, c.statut")
    List<Object[]> getSupplierStatusBreakdown(@Param("entrepriseId") Long entrepriseId);

    @Query("SELECT COUNT(c) FROM CommandeAchat c WHERE c.entreprise.id = :entrepriseId AND c.dateCommande BETWEEN :debut AND :fin")
    Long getPurchaseOrderCountInPeriod(@Param("entrepriseId") Long entrepriseId, @Param("debut") LocalDate debut, @Param("fin") LocalDate fin);

    @Query("SELECT COUNT(c) FROM CommandeAchat c WHERE c.entreprise.id = :entrepriseId AND c.statut NOT IN ('RECUE', 'ANNULEE') AND c.dateLivraisonPrevue < :today")
    Long getOverdueOrdersCount(@Param("entrepriseId") Long entrepriseId, @Param("today") LocalDate today);

    @Query("SELECT COUNT(c) FROM CommandeAchat c WHERE c.entreprise.id = :entrepriseId AND c.statut NOT IN ('ANNULEE', 'RECUE')")
    Long getActiveOrdersCount(@Param("entrepriseId") Long entrepriseId);

    @Transactional
    void deleteByEntrepriseId(Long entrepriseId);
}
