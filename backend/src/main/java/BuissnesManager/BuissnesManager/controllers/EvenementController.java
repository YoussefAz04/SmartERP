package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.entity.CategorieEvenement;
import BuissnesManager.BuissnesManager.entity.Evenement;
import BuissnesManager.BuissnesManager.service.EvenementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("evenement")
public class EvenementController {

    @Autowired
    private EvenementService service;

    @GetMapping("getAllEvenement")
    public ResponseEntity<?> getAllEvenement() {
        return ResponseEntity.status(HttpStatus.OK).body(service.getAllEvenement());
    }
    @GetMapping("getAllCategorie")
    public ResponseEntity<?> getAllCategorie() {
        return ResponseEntity.status(HttpStatus.OK).body(service.getAllCategorie());
    }

    @PostMapping("saveEvenement/{idCategorie}")
    public ResponseEntity<?> saveEvenement(@RequestBody Evenement e,@PathVariable Long idCategorie) {
        return ResponseEntity.status(HttpStatus.OK).body(service.save(e,idCategorie));
    }

    @DeleteMapping("deleteEvenement/{id}")
    public ResponseEntity<?> deleteEvenement(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }
    @PostMapping("createCategorieEvenement")
    public CategorieEvenement createCategorieEvenement(@RequestBody CategorieEvenement e) {
        return service.saveCategorieEvenement(e);
    }
}
