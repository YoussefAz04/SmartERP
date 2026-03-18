package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.CategorieDepense;
import BuissnesManager.BuissnesManager.entity.Depense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DepenseRepo extends JpaRepository<Depense,Long> {

    List<Depense> findAllByCategorieDepenseId(Long idCatgorieِِِ);


    @Query("SELECT COALESCE(SUM(d.montant), 0) FROM Depense d WHERE d.budget.id = :budgetId")
    Double sumByBudgetId(Long budgetId);
}
