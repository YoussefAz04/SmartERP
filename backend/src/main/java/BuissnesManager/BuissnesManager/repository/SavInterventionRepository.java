package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.SavIntervention;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavInterventionRepository extends JpaRepository<SavIntervention, Long> {
    List<SavIntervention> findByTicketIdOrderByPerformedAtDesc(Long ticketId);

    Optional<SavIntervention> findByIdAndTicketId(Long id, Long ticketId);

    long countByTicketId(Long ticketId);

    void deleteByTicketId(Long ticketId);
}
