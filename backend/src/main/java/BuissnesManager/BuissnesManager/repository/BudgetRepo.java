package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BudgetRepo extends JpaRepository<Budget,Long> {
    Budget findByIdProjet(Long idProjet)  ;
}
