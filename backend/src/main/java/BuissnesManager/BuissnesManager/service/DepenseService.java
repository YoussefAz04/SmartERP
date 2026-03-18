package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.dto.BudgetPerformanceDto;
import BuissnesManager.BuissnesManager.entity.Budget;
import BuissnesManager.BuissnesManager.entity.CategorieDepense;
import BuissnesManager.BuissnesManager.entity.Depense;
import BuissnesManager.BuissnesManager.repository.BudgetRepo;
import BuissnesManager.BuissnesManager.repository.CategorieDepenseRepo;
import BuissnesManager.BuissnesManager.repository.DepenseRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class DepenseService {

    @Autowired
    private DepenseRepo depenseRepo;
    @Autowired
    private CategorieDepenseRepo categorieDepenseRepo ;
    @Autowired
    private BudgetRepo budgetRepo ;

    public List<Depense> getAllDepenses() {
        return depenseRepo.findAll();
    }


    public Depense getDepenseById(Long id) {
        return depenseRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Dépense non trouvée avec id : " + id));
    }



    @Transactional
    public Depense addDepense(Depense depense,Long idCategorie,Long idBudjet) {
        CategorieDepense categorieDepense = categorieDepenseRepo.findById(idCategorie).get();
        Budget budget = budgetRepo.findById(idBudjet).get();
        budget.setMontant(budget.getMontant()+depense.getMontant());
        budgetRepo.save(budget);
        depense.setBudget(budget);
        depense.setCategorieDepense(categorieDepense);
        return depenseRepo.save(depense);
    }

    public Depense updateDepense(Long id, Depense depenseDetails) {
        Depense depense = depenseRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Dépense non trouvée avec id : " + id));

        depense.setDescription(depenseDetails.getDescription());
        depense.setMontant(depenseDetails.getMontant());
        depense.setDateDepense(depenseDetails.getDateDepense());
        depense.setDateDepense(depenseDetails.getDateDepense());
        return depenseRepo.save(depense);
    }

    public void deleteDepense(Long id) {
        depenseRepo.deleteById(id);
    }

    public Double getTotalDepenseByProjet(Long idProjet){
        Double total = 0.0;
         Budget budget = budgetRepo.findByIdProjet(idProjet);
         for (Depense depense : budget.getDepenses()){
             total += depense.getMontant() ;
         }
       return total ;
    }

    public List<Depense> getDepenseByCategorie(Long idCategorie){
        return depenseRepo.findAllByCategorieDepenseId(idCategorie);
    }

    public Double tauxDeUtilisation(Long idProjet){
        Double total = 0.0;

        Budget budget = budgetRepo.findByIdProjet(idProjet);
        for (Depense depense : budget.getDepenses()){
            total += depense.getMontant() ;
        }
      return total / budget.getMontant() * 100 ;
    }



    public List<BudgetPerformanceDto> getPerformanceBudgets() {
        LocalDate today = LocalDate.now();
        List<Budget> budgets = budgetRepo.findAll();

        return budgets.stream().map(b -> {
            double montantTotal = b.getMontant() != null ? b.getMontant() : 0.0;
            double montantDepense = depenseRepo.sumByBudgetId(b.getId());

            double tauxConsommation = montantTotal > 0 ? (montantDepense / montantTotal) * 100 : 0;

            long dureeTotale = java.time.temporal.ChronoUnit.DAYS.between(b.getDateDebut(), b.getDateFin());
            long dureeEcoulee = java.time.temporal.ChronoUnit.DAYS.between(b.getDateDebut(), today);

            double tauxTemps = dureeTotale > 0 ? ((double) dureeEcoulee / dureeTotale) * 100 : 0;

            boolean depassement = tauxConsommation > tauxTemps;

            return new BudgetPerformanceDto(
                    b.getId(),
                    b.getNom(),
                    tauxConsommation,
                    tauxTemps,
                    depassement
            );
        }).toList();
    }

}

