package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.SavIntervention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavInterventionRepository extends JpaRepository<SavIntervention, Long> {
    List<SavIntervention> findBySavIdOrderByCreatedAtAsc(Long savId);
}
