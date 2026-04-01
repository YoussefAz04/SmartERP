package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    List<Stock> findByArticleAchatId(Long articleAchatId);
    List<Stock> findByArticleVenteId(Long articleVenteId);
    Optional<Stock> findByArticleAchatIdAndArticleVenteId(Long articleAchatId, Long articleVenteId);
}
