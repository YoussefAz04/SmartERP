package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.dto.DashboardHomeSummaryDto;
import BuissnesManager.BuissnesManager.entity.Stock;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    List<Stock> findByArticleAchatId(Long articleAchatId);
    Optional<Stock> findFirstByArticleAchatId(Long articleAchatId);
    Optional<Stock> findFirstByArticleAchatIdAndEntrepriseId(Long articleAchatId, Long entrepriseId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Stock s WHERE s.articleAchat.id = :articleId AND s.entreprise.id = :entrepriseId")
    Optional<Stock> findByArticleAndEntrepriseForUpdate(@Param("articleId") Long articleId,
                                                        @Param("entrepriseId") Long entrepriseId);
    long countByArticleAchatId(Long articleAchatId);

    @Query("SELECT s FROM Stock s WHERE s.articleAchat.entreprise.id = :entrepriseId")
    List<Stock> findByEntrepriseId(@Param("entrepriseId") Long entrepriseId);

    @Query("SELECT COUNT(s) FROM Stock s WHERE s.articleAchat.entreprise.id = :entrepriseId")
    long countByEntrepriseId(@Param("entrepriseId") Long entrepriseId);

    /**
     * Slim projection for the home dashboard "articles en stock" panel.
     * Casts {@code quantiteDisponible} (BigDecimal) to Double so the DTO can
     * stay JSON-friendly without dragging the article entity graph along.
     */
    @Query("SELECT new BuissnesManager.BuissnesManager.dto.DashboardHomeSummaryDto$RecentStockDto(" +
           "s.id, s.articleAchat.id, s.articleAchat.nom, " +
           "CAST(COALESCE(s.quantiteDisponible, 0) AS double)) " +
           "FROM Stock s WHERE s.articleAchat.entreprise.id = :entrepriseId ORDER BY s.id DESC")
    List<DashboardHomeSummaryDto.RecentStockDto> findRecentByEntreprise(
            @Param("entrepriseId") Long entrepriseId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(s.quantiteDisponible * s.articleAchat.prixAchat), 0) FROM Stock s WHERE s.articleAchat.entreprise.id = :entrepriseId")
    Double getTotalStockValue(@Param("entrepriseId") Long entrepriseId);

    @Query("SELECT COUNT(s) FROM Stock s WHERE s.articleAchat.entreprise.id = :entrepriseId AND s.seuilAlerte IS NOT NULL AND s.seuilAlerte > 0 AND s.quantiteDisponible < s.seuilAlerte")
    Long getLowStockItemsCount(@Param("entrepriseId") Long entrepriseId);

    @Query("SELECT COUNT(s) FROM Stock s WHERE s.articleAchat.entreprise.id = :entrepriseId AND (s.quantiteDisponible IS NULL OR s.quantiteDisponible = 0)")
    Long getOutOfStockItemsCount(@Param("entrepriseId") Long entrepriseId);

    @Query("SELECT COUNT(DISTINCT s.articleAchat.id) FROM Stock s WHERE s.articleAchat.entreprise.id = :entrepriseId")
    Long getTotalSKUs(@Param("entrepriseId") Long entrepriseId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Stock s WHERE s.articleAchat.entreprise.id = :entrepriseId")
    void deleteByEntrepriseId(@Param("entrepriseId") Long entrepriseId);
}
