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

    /**
     * Top-selling products aggregated by ArticleVente.
     *
     * The seed data only populates the underlying Article (articleAchat) with a name –
     * ArticleVente.nom can legitimately be NULL for records created before the entity
     * gained a `nom` column. We therefore COALESCE over articleVente.nom, the linked
     * articleAchat.nom, and finally a synthetic fallback so every product row gets a
     * distinguishable, human-readable label in the AI dashboard.
     */
    @Query("SELECT COALESCE(NULLIF(lc.articleVente.nom, ''), NULLIF(lc.articleVente.articleAchat.nom, ''), CONCAT('Produit #', lc.articleVente.id)), " +
           "COALESCE(SUM(lc.quantite), 0), " +
           "COALESCE(SUM(lc.montantTTC), 0) " +
           "FROM LigneCommandeVente lc " +
           "WHERE lc.commandeVente.entreprise.id = :entrepriseId " +
           "AND lc.articleVente IS NOT NULL " +
           "GROUP BY lc.articleVente.id, lc.articleVente.nom, lc.articleVente.articleAchat.nom " +
           "ORDER BY SUM(lc.quantite) DESC")
    List<Object[]> getTopProducts(@Param("entrepriseId") Long entrepriseId);

    @Query("SELECT COUNT(c) FROM CommandeVente c WHERE c.entreprise.id = :entrepriseId AND c.dateCommande BETWEEN :debut AND :fin")
    Long getSalesOrderCountInPeriod(@Param("entrepriseId") Long entrepriseId, @Param("debut") LocalDate debut, @Param("fin") LocalDate fin);

    /**
     * Cost Of Goods Sold (COGS) for a given enterprise and period.
     * Sums quantity * purchase-price of every sold line, so the resulting "gross margin"
     * (sales - COGS) reflects the real margin on items that were actually delivered,
     * not the naive "sales - all purchases" (which treats stock build-up as a loss).
     */
    @Query("SELECT COALESCE(SUM(lc.quantite * COALESCE(lc.articleVente.articleAchat.prixAchat, 0)), 0) " +
           "FROM LigneCommandeVente lc " +
           "WHERE lc.commandeVente.entreprise.id = :entrepriseId " +
           "AND lc.commandeVente.dateCommande BETWEEN :debut AND :fin")
    Double getCOGSInPeriod(@Param("entrepriseId") Long entrepriseId,
                           @Param("debut") LocalDate debut,
                           @Param("fin") LocalDate fin);

    /**
     * Revenue, quantity sold and line count grouped by real article category
     * (joined through articleVente -> articleAchat -> categorieArticle).
     *
     * The seed inserted categorie_article rows against a legacy `nom` column so
     * `libelle` may be NULL – fall back to `code` or a default label.
     */
    @Query("SELECT COALESCE(NULLIF(c.libelle, ''), NULLIF(c.code, ''), 'Non catégorisé'), " +
           "COALESCE(SUM(lc.montantTTC), 0), " +
           "COALESCE(SUM(lc.quantite), 0) " +
           "FROM LigneCommandeVente lc " +
           "LEFT JOIN lc.articleVente av " +
           "LEFT JOIN av.articleAchat aa " +
           "LEFT JOIN aa.categorieArticle c " +
           "WHERE lc.commandeVente.entreprise.id = :entrepriseId " +
           "GROUP BY c.id, c.libelle, c.code " +
           "ORDER BY SUM(lc.montantTTC) DESC")
    List<Object[]> getCategoryPerformance(@Param("entrepriseId") Long entrepriseId);
}
