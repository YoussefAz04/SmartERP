package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.entity.CategorieProjet;
import BuissnesManager.BuissnesManager.service.CategorieProjetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categorieProjet")
public class CategorieProjetController {

    @Autowired
    private CategorieProjetService categorieProjetService;

    @PostMapping("add")
    public ResponseEntity<?> createCategorie(@RequestBody CategorieProjet categorie) {
        CategorieProjet created = categorieProjetService.createCategorie(categorie);
        return ResponseEntity.ok(created);
    }

    @GetMapping("getAll")
    public ResponseEntity<?> getAllCategories() {
        List<CategorieProjet> list = categorieProjetService.getAllCategories();
        return ResponseEntity.ok(list);
    }

    @GetMapping("getById/{id}")
    public ResponseEntity<?> getCategorieById(@PathVariable Long id) {
        return categorieProjetService.getCategorieById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("update/{id}")
    public ResponseEntity<?> updateCategorie(@PathVariable Long id, @RequestBody CategorieProjet categorie) {
        try {
            CategorieProjet updated = categorieProjetService.updateCategorie(id, categorie);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> deleteCategorie(@PathVariable Long id) {
        try {
            categorieProjetService.deleteCategorie(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
