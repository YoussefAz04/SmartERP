package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.SavStatus;
import BuissnesManager.BuissnesManager.entity.SavTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface SavTicketRepository extends JpaRepository<SavTicket, Long> {
    List<SavTicket> findByEntrepriseIdOrderByCreatedAtDesc(Long entrepriseId);

    Optional<SavTicket> findByIdAndEntrepriseId(Long id, Long entrepriseId);

    long countByEntrepriseId(Long entrepriseId);

    long countByEntrepriseIdAndStatus(Long entrepriseId, SavStatus status);

    long countByEntrepriseIdAndStatusIn(Long entrepriseId, List<SavStatus> statuses);

    long countByEntrepriseIdAndDueDateBeforeAndStatusNotIn(
            Long entrepriseId, LocalDate date, List<SavStatus> statuses);

    long countByEntrepriseIdAndAssignedToIsNullAndStatusNotIn(
            Long entrepriseId, List<SavStatus> statuses);

    @Query("select coalesce(sum(t.actualCost), 0) from SavTicket t where t.entreprise.id = :entrepriseId")
    BigDecimal sumActualCostByEntrepriseId(@Param("entrepriseId") Long entrepriseId);
}
