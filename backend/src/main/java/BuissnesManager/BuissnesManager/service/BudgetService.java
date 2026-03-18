package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.entity.Budget;
import BuissnesManager.BuissnesManager.entity.Projet;
import BuissnesManager.BuissnesManager.repository.BudgetRepo;
import BuissnesManager.BuissnesManager.repository.ProjetRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BudgetService {

    @Autowired
    private BudgetRepo budgetRepo;


    public List<Budget> getAllBudgets() {
        return budgetRepo.findAll();
    }

    public Budget getBudgetById(Long id) {
        return budgetRepo.findById(id).get();
    }

    public Budget addBudget(Budget budget) {
        return budgetRepo.save(budget);
    }

    public Budget updateBudget(Long id, Budget budgetDetails) {
        Budget budget = budgetRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget non trouvé avec l'id : " + id));
        budget.setNom(budgetDetails.getNom());
        budget.setMontant(budgetDetails.getMontant());
        budget.setDateFin(budgetDetails.getDateFin());
        budget.setDateDebut(budgetDetails.getDateDebut());
        budget.setIdProjet(budgetDetails.getIdProjet());
        return budgetRepo.save(budget);
    }

    public void deleteBudget(Long id) {
        budgetRepo.deleteById(id);
    }


    public Budget getBudgetsByProjet(Long idProjet){
        return budgetRepo.findByIdProjet(idProjet);
    }

}
