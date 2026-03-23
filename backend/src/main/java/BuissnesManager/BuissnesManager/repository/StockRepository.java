package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    List<Stock> findByEntrepriseId(Long entrepriseId);
    List<Stock> findByArticleId(Long articleId);

    @Query("SELECT s FROM Stock s WHERE s.entreprise.id = :entrepriseId AND s.quantiteDisponible <= s.quantiteMinimale")
    List<Stock> findLowStockByEntreprise(@Param("entrepriseId") Long entrepriseId);

    Optional<Stock> findByArticleIdAndEntrepriseId(Long articleId, Long entrepriseId);
}
