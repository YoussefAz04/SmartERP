package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.DetailsFactureDeVente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DetaisFactureDeVenteRepo extends JpaRepository<DetailsFactureDeVente,Long> {

    /**
     * Fallback "top products" aggregation used by the dashboards when no
     * {@code LigneCommandeVente} rows are available for the enterprise (e.g.
     * the user enters sales as standalone factures with a free-text article
     * label rather than as commandes with structured lines). Aggregates by the
     * {@code article} string field, summing quantities and TTC totals across
     * all factures owned by the enterprise.
     *
     * Returns rows of {name, quantity, revenue} sorted by total quantity DESC,
     * matching the shape returned by {@link
     * BuissnesManager.BuissnesManager.repository.CommandeVenteRepository#getTopProducts(Long)}
     * so callers can reuse the same parsing logic.
     */
    @Query("SELECT COALESCE(NULLIF(d.article, ''), 'Article non spécifié'), " +
           "COALESCE(SUM(d.quantite), 0), " +
           "COALESCE(SUM(d.totalTTC), 0) " +
           "FROM DetailsFactureDeVente d " +
           "WHERE d.factureDeVente.entreprise.id = :entrepriseId " +
           "AND d.article IS NOT NULL " +
           "GROUP BY d.article " +
           "ORDER BY SUM(d.quantite) DESC")
    List<Object[]> getTopProductsFromFactures(@Param("entrepriseId") Long entrepriseId);
}
