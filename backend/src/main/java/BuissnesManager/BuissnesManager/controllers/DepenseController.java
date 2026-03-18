package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.dto.BudgetPerformanceDto;
import BuissnesManager.BuissnesManager.entity.Depense;
import BuissnesManager.BuissnesManager.service.DepenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/depenses")
public class DepenseController {

    @Autowired
    private DepenseService depenseService;

    @GetMapping("getAllDepenses")
    public List<Depense> getAllDepenses() {
        return depenseService.getAllDepenses();
    }

    @GetMapping("getDepenseById/{id}")
    public ResponseEntity<?> getDepenseById(@PathVariable Long id) {
        Depense depense = depenseService.getDepenseById(id);
        return ResponseEntity.ok(depense);
    }

    @PostMapping("addDepense/{idBudget}/{idCategorie}")
    public ResponseEntity<?> addDepense(@RequestBody Depense depense,@PathVariable Long idBudget,@PathVariable Long idCategorie) {
        Depense newDepense = depenseService.addDepense(depense, idCategorie,idBudget);
        return ResponseEntity.ok().build();
    }

    @PutMapping("updateDepense/{id}")
    public ResponseEntity<?> updateDepense(
            @PathVariable Long id,
            @RequestBody Depense depenseDetails
    ) {
        Depense updatedDepense = depenseService.updateDepense(id, depenseDetails);
        return ResponseEntity.ok(updatedDepense);
    }

    @DeleteMapping("deleteDepense/{id}")
    public ResponseEntity<?> deleteDepense(@PathVariable Long id) {
        depenseService.deleteDepense(id);
        return ResponseEntity.ok("Dépense supprimée avec succès !");
    }




    @GetMapping("/total-depenses/{idProjet}")
    public Double getTotalDepenseByProjet(@PathVariable Long idProjet) {
        return depenseService.getTotalDepenseByProjet(idProjet);
    }

    @GetMapping("/depenses-par-categorie/{idCategorie}")
    public List<Depense> getDepenseByCategorie(@PathVariable Long idCategorie) {
        return depenseService.getDepenseByCategorie(idCategorie);
    }

    @GetMapping("/taux-utilisation/{idProjet}")
    public Double tauxDeUtilisation(@PathVariable Long idProjet) {
        return depenseService.tauxDeUtilisation(idProjet);
    }

    @GetMapping("/performance-budgets")
    public List<BudgetPerformanceDto> getPerformanceBudgets() {
        return depenseService.getPerformanceBudgets();
    }
}
