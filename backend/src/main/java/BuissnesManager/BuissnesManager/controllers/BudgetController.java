package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.entity.Budget;
import BuissnesManager.BuissnesManager.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("budgets")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    @GetMapping("getAllBudgets")
    public List<?> getAllBudgets() {
        return budgetService.getAllBudgets();
    }

    @GetMapping("getBudgetById/{id}")
    public ResponseEntity<?> getBudgetById(@PathVariable Long id) {
        Budget budget = budgetService.getBudgetById(id);
        return ResponseEntity.ok(budget);
    }

    @GetMapping("getBudgetsByProjet/{id}")
    public ResponseEntity<?> getBudgetsByProjet(@PathVariable Long id) {
        Budget budget = budgetService.getBudgetsByProjet(id);
        return ResponseEntity.ok(budget);
    }

    @PostMapping("addBudget")
    public ResponseEntity<?> addBudget(@RequestBody Budget budget) {
        Budget newBudget = budgetService.addBudget(budget);
        return ResponseEntity.ok(newBudget);
    }

    @PutMapping("updateBudget/{id}")
    public ResponseEntity<?> updateBudget(
            @PathVariable Long id,
            @RequestBody Budget budgetDetails
    ) {
        Budget updatedBudget = budgetService.updateBudget(id, budgetDetails);
        return ResponseEntity.ok(updatedBudget);
    }

    @DeleteMapping("deleteBudget/{id}")
    public ResponseEntity<?> deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.ok("Budget supprimé avec succès !");
    }
}
