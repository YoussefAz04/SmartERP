package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.SavTicketHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SavTicketHistoryRepository extends JpaRepository<SavTicketHistory, Long> {
    List<SavTicketHistory> findByTicketIdOrderByCreatedAtDesc(Long ticketId);

    void deleteByTicketId(Long ticketId);
}
