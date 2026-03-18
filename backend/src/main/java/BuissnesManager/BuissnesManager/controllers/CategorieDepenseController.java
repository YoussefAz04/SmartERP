package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.entity.CategorieDepense;
import BuissnesManager.BuissnesManager.service.CategorieDepenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories-depenses")
public class CategorieDepenseController {

    @Autowired
    private CategorieDepenseService categorieDepenseService;

    @GetMapping("getAllCategories")
    public List<CategorieDepense> getAllCategories() {
        return categorieDepenseService.getAllCategories();
    }

    @GetMapping("getCategorieById/{id}")
    public ResponseEntity<?> getCategorieById(@PathVariable Long id) {
        CategorieDepense categorie = categorieDepenseService.getCategorieById(id);
        return categorie != null ? ResponseEntity.ok(categorie) : ResponseEntity.notFound().build();
    }
    @PostMapping("createCategorie")
    public ResponseEntity<?> createCategorie(@RequestBody CategorieDepense categorie) {
           try {
               categorieDepenseService.saveCategorie(categorie);
               return ResponseEntity.ok(HttpStatus.CREATED);
           }catch (Exception e){
               return ResponseEntity.noContent().build() ;
           }
    }

    @PutMapping("updateCategorie/{id}")
    public ResponseEntity<?> updateCategorie(@PathVariable Long id,
                                                            @RequestBody CategorieDepense categorieDetails) {
        CategorieDepense categorie = categorieDepenseService.getCategorieById(id);
        if (categorie == null) {
            return ResponseEntity.notFound().build();
        }

        categorie.setNom(categorieDetails.getNom());
        categorie.setDescription(categorieDetails.getDescription());

        CategorieDepense updatedCategorie = categorieDepenseService.saveCategorie(categorie);
        return ResponseEntity.ok(updatedCategorie);
    }

    // Supprimer une catégorie
    @DeleteMapping("deleteCategorie/{id}")
    public ResponseEntity<?> deleteCategorie(@PathVariable Long id) {
        categorieDepenseService.deleteCategorie(id);
        return ResponseEntity.noContent().build();
    }
}
