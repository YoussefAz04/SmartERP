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
    List<Stock> findByArticleAchatId(Long articleAchatId);
    Optional<Stock> findFirstByArticleAchatId(Long articleAchatId);

    @Query("SELECT s FROM Stock s WHERE s.articleAchat.entreprise.id = :entrepriseId")
    List<Stock> findByEntrepriseId(@Param("entrepriseId") Long entrepriseId);
}
