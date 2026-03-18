package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.entity.CategorieTache;
import BuissnesManager.BuissnesManager.service.CategorieTacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categorie-taches")
public class CategorieTacheController {

    @Autowired
    private CategorieTacheService categorieTacheService;

    @PostMapping("add")
    public ResponseEntity<?> createCategorie(@RequestBody CategorieTache categorie) {
        CategorieTache created = categorieTacheService.createCategorie(categorie);
        return ResponseEntity.ok(created);
    }

    @GetMapping("getAll")
    public ResponseEntity<?> getAllCategories() {
        return ResponseEntity.ok(categorieTacheService.getAllCategories());
    }

    @GetMapping("getById/{id}")
    public ResponseEntity<?> getCategorieById(@PathVariable Long id) {
        return categorieTacheService.getCategorieById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("update/{id}")
    public ResponseEntity<?> updateCategorie(@PathVariable Long id, @RequestBody CategorieTache categorie) {
        try {
            CategorieTache updated = categorieTacheService.updateCategorie(id, categorie);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }


    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> deleteCategorie(@PathVariable Long id) {
        try {
            categorieTacheService.deleteCategorie(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
