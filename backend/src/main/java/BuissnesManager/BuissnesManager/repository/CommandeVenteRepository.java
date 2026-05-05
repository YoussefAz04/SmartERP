package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.CommandeStatut;
import BuissnesManager.BuissnesManager.entity.CommandeVente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CommandeVenteRepository extends JpaRepository<CommandeVente, Long> {

    /**
     * Convenience accessor mirroring {@link CommandeStatut#revenueStatutNames()}
     * so callers can write {@code repo.getMonthlySales(id, year, REVENUE_STATUTS)}
     * without re-importing the enum.
     */
    List<String> REVENUE_STATUTS = CommandeStatut.revenueStatutNames();
    List<String> RETURN_STATUTS = CommandeStatut.returnStatutNames();
    List<CommandeVente> findByEntrepriseId(Long entrepriseId);
    List<CommandeVente> findByStatut(String statut);
    List<CommandeVente> findByEntrepriseIdAndStatut(Long entrepriseId, String statut);
    List<CommandeVente> findByClientId(Long clientId);

    @Modifying
    @Query("UPDATE CommandeVente c SET c.client = null WHERE c.client.id = :clientId")
    void detachClient(@Param("clientId") Long clientId);

    @Query("SELECT c FROM CommandeVente c WHERE c.entreprise.id = :entrepriseId AND c.dateCommande BETWEEN :debut AND :fin")
    List<CommandeVente> findByEntrepriseIdAndDateRange(@Param("entrepriseId") Long entrepriseId, @Param("debut") LocalDate debut, @Param("fin") LocalDate fin);

    @Query("SELECT COALESCE(SUM(c.montantTTC), 0) FROM CommandeVente c " +
           "WHERE c.entreprise.id = :entrepriseId AND c.statut IN :validStatuts")
    Double getTotalSalesRevenue(@Param("entrepriseId") Long entrepriseId,
                                @Param("validStatuts") List<String> validStatuts);

    /** Backwards-compatible default: count only revenue-qualifying rows. */
    default Double getTotalSalesRevenue(Long entrepriseId) {
        return getTotalSalesRevenue(entrepriseId, REVENUE_STATUTS);
    }

    @Query("SELECT COUNT(c) FROM CommandeVente c " +
           "WHERE c.entreprise.id = :entrepriseId AND c.statut IN :validStatuts")
    Long getTotalSalesOrders(@Param("entrepriseId") Long entrepriseId,
                             @Param("validStatuts") List<String> validStatuts);

    default Long getTotalSalesOrders(Long entrepriseId) {
        return getTotalSalesOrders(entrepriseId, REVENUE_STATUTS);
    }

    @Query("SELECT c.statut, COUNT(c) FROM CommandeVente c WHERE c.entreprise.id = :entrepriseId GROUP BY c.statut")
    List<Object[]> getOrderStatusBreakdown(@Param("entrepriseId") Long entrepriseId);

    @Query("SELECT COALESCE(SUM(c.montantTTC), 0) FROM CommandeVente c " +
           "WHERE c.entreprise.id = :entrepriseId AND c.statut IN :validStatuts " +
           "AND c.dateCommande BETWEEN :debut AND :fin")
    Double getSalesInPeriod(@Param("entrepriseId") Long entrepriseId,
                            @Param("debut") LocalDate debut,
                            @Param("fin") LocalDate fin,
                            @Param("validStatuts") List<String> validStatuts);

    default Double getSalesInPeriod(Long entrepriseId, LocalDate debut, LocalDate fin) {
        return getSalesInPeriod(entrepriseId, debut, fin, REVENUE_STATUTS);
    }

    /**
     * Monthly aggregated revenue, in the entreprise's reporting currency,
     * <b>excluding</b> drafts / cancellations / returns. Uses
     * {@code montantHT} so historical changes in the VAT rate do not bias
     * the trend (see {@code CurrencyNormalizationService} docstring).
     *
     * <p>The legacy callers used {@code montantTTC}; the switch is safe
     * because the relative shape of the series is what the forecaster
     * consumes — only the absolute level differs by 1 + VAT.
     */
    @Query("SELECT FUNCTION('MONTH', c.dateCommande), " +
           "COALESCE(SUM(COALESCE(c.montantHT, c.montantTTC)), 0) " +
           "FROM CommandeVente c " +
           "WHERE c.entreprise.id = :entrepriseId " +
           "AND FUNCTION('YEAR', c.dateCommande) = :year " +
           "AND c.statut IN :validStatuts " +
           "GROUP BY FUNCTION('MONTH', c.dateCommande) " +
           "ORDER BY FUNCTION('MONTH', c.dateCommande)")
    List<Object[]> getMonthlySales(@Param("entrepriseId") Long entrepriseId,
                                   @Param("year") int year,
                                   @Param("validStatuts") List<String> validStatuts);

    default List<Object[]> getMonthlySales(Long entrepriseId, int year) {
        return getMonthlySales(entrepriseId, year, REVENUE_STATUTS);
    }

    /**
     * Monthly returns (RETOUR statut). Used to compute net revenue =
     * sales − returns when callers need a net-of-refunds view.
     */
    @Query("SELECT FUNCTION('MONTH', c.dateCommande), " +
           "COALESCE(SUM(COALESCE(c.montantHT, c.montantTTC)), 0) " +
           "FROM CommandeVente c " +
           "WHERE c.entreprise.id = :entrepriseId " +
           "AND FUNCTION('YEAR', c.dateCommande) = :year " +
           "AND c.statut IN :returnStatuts " +
           "GROUP BY FUNCTION('MONTH', c.dateCommande) " +
           "ORDER BY FUNCTION('MONTH', c.dateCommande)")
    List<Object[]> getMonthlyReturns(@Param("entrepriseId") Long entrepriseId,
                                     @Param("year") int year,
                                     @Param("returnStatuts") List<String> returnStatuts);

    default List<Object[]> getMonthlyReturns(Long entrepriseId, int year) {
        return getMonthlyReturns(entrepriseId, year, RETURN_STATUTS);
    }

    @Query("SELECT c.client.nom, COALESCE(SUM(c.montantTTC), 0), COUNT(c) " +
           "FROM CommandeVente c " +
           "WHERE c.entreprise.id = :entrepriseId AND c.client IS NOT NULL " +
           "AND c.statut IN :validStatuts " +
           "GROUP BY c.client.id, c.client.nom " +
           "ORDER BY SUM(c.montantTTC) DESC")
    List<Object[]> getTopCustomers(@Param("entrepriseId") Long entrepriseId,
                                   @Param("validStatuts") List<String> validStatuts);

    default List<Object[]> getTopCustomers(Long entrepriseId) {
        return getTopCustomers(entrepriseId, REVENUE_STATUTS);
    }

    /**
     * Top-selling products aggregated by ArticleVente.
     *
     * Uses an explicit INNER JOIN on articleVente (replaces the IS NOT NULL filter) and
     * a LEFT JOIN on articleAchat so that ArticleVente rows whose article_achat_id is NULL
     * (e.g. records created via the original SQL seed script that used the old `article_id`
     * column) are still included instead of being silently dropped by an implicit INNER JOIN.
     * COALESCE resolves the display name: articleVente.nom → articleAchat.nom → synthetic id.
     */
    @Query("SELECT COALESCE(NULLIF(av.nom, ''), NULLIF(aa.nom, ''), CONCAT('Produit #', av.id)), " +
           "COALESCE(SUM(lc.quantite), 0), " +
           "COALESCE(SUM(lc.montantTTC), 0) " +
           "FROM LigneCommandeVente lc " +
           "JOIN lc.articleVente av " +
           "LEFT JOIN av.articleAchat aa " +
           "WHERE lc.commandeVente.entreprise.id = :entrepriseId " +
           "GROUP BY av.id, av.nom, aa.nom " +
           "ORDER BY SUM(lc.quantite) DESC")
    List<Object[]> getTopProducts(@Param("entrepriseId") Long entrepriseId);

    @Query("SELECT COUNT(c) FROM CommandeVente c " +
           "WHERE c.entreprise.id = :entrepriseId " +
           "AND c.statut IN :validStatuts " +
           "AND c.dateCommande BETWEEN :debut AND :fin")
    Long getSalesOrderCountInPeriod(@Param("entrepriseId") Long entrepriseId,
                                    @Param("debut") LocalDate debut,
                                    @Param("fin") LocalDate fin,
                                    @Param("validStatuts") List<String> validStatuts);

    default Long getSalesOrderCountInPeriod(Long entrepriseId, LocalDate debut, LocalDate fin) {
        return getSalesOrderCountInPeriod(entrepriseId, debut, fin, REVENUE_STATUTS);
    }

    /**
     * Cost Of Goods Sold (COGS) for a given enterprise and period.
     * Sums quantity * purchase-price of every sold line, so the resulting "gross margin"
     * (sales - COGS) reflects the real margin on items that were actually delivered,
     * not the naive "sales - all purchases" (which treats stock build-up as a loss).
     */
    @Query("SELECT COALESCE(SUM(lc.quantite * COALESCE(lc.articleVente.articleAchat.prixAchat, 0)), 0) " +
           "FROM LigneCommandeVente lc " +
           "WHERE lc.commandeVente.entreprise.id = :entrepriseId " +
           "AND lc.commandeVente.statut IN :validStatuts " +
           "AND lc.commandeVente.dateCommande BETWEEN :debut AND :fin")
    Double getCOGSInPeriod(@Param("entrepriseId") Long entrepriseId,
                           @Param("debut") LocalDate debut,
                           @Param("fin") LocalDate fin,
                           @Param("validStatuts") List<String> validStatuts);

    default Double getCOGSInPeriod(Long entrepriseId, LocalDate debut, LocalDate fin) {
        return getCOGSInPeriod(entrepriseId, debut, fin, REVENUE_STATUTS);
    }

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

    @Modifying
    @Transactional
    @Query("DELETE FROM CommandeVente c WHERE c.entreprise.id = :entrepriseId")
    void deleteByEntrepriseId(@Param("entrepriseId") Long entrepriseId);
}