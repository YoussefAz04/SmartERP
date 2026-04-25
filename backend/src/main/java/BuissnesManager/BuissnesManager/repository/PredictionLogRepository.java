package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.PredictionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PredictionLogRepository extends JpaRepository<PredictionLog, Long> {

    Optional<PredictionLog> findByEntrepriseIdAndPredictionMonthAndTargetMonthAndHorizonMonths(
            Long entrepriseId, LocalDate predictionMonth, LocalDate targetMonth, Integer horizonMonths);

    List<PredictionLog> findByEntrepriseIdOrderByTargetMonthDescHorizonMonthsAsc(Long entrepriseId);

    /** Predictions whose target month is &lt;= given cutoff and that have not been
     * settled yet (actual still null). Used by the reconciliation job. */
    @Query("SELECT p FROM PredictionLog p " +
           "WHERE p.entreprise.id = :entrepriseId " +
           "AND p.actualValue IS NULL " +
           "AND p.targetMonth <= :cutoff")
    List<PredictionLog> findPendingReconciliation(
            @Param("entrepriseId") Long entrepriseId,
            @Param("cutoff") LocalDate cutoff);

    @Query("SELECT p FROM PredictionLog p " +
           "WHERE p.actualValue IS NULL AND p.targetMonth <= :cutoff")
    List<PredictionLog> findAllPendingReconciliation(@Param("cutoff") LocalDate cutoff);
}
