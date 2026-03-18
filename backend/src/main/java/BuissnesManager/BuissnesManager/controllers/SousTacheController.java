package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.entity.SousTache;
import BuissnesManager.BuissnesManager.service.SousTacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/sous-taches")
public class SousTacheController {

    @Autowired
    private SousTacheService sousTacheService;


    @PostMapping("/with-tache/{idTache}")
    public ResponseEntity<SousTache> createWithTache(@RequestBody SousTache sousTache, @PathVariable Long idTache) {
        try {
            SousTache created = sousTacheService.createSousTacheWithTache(sousTache, idTache);
            return ResponseEntity.ok(created);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }


    @PostMapping("add")
    public ResponseEntity<?> create(@RequestBody SousTache sousTache) {
        SousTache created = sousTacheService.create(sousTache);
        return ResponseEntity.ok(created); // 200 OK avec l'objet créé
    }

    @GetMapping("getAll")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(sousTacheService.getAll());
    }

    @GetMapping("getById/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return sousTacheService.getById(id)
                .map(ResponseEntity::ok) // 200 OK
                .orElse(ResponseEntity.notFound().build()); // 404 Not Found
    }

    @PutMapping("update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody SousTache updated) {
        try {
            SousTache updatedTache = sousTacheService.update(id, updated);
            return ResponseEntity.ok(updatedTache); // 200 OK
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        sousTacheService.delete(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    @PutMapping("/assign-sous-tache/{idSousTache}/{idTache}")
    public ResponseEntity<String> assignerSousTache(
            @PathVariable Long idSousTache,
            @PathVariable Long idTache) {
        try {
            sousTacheService.assignedSousTache(idTache, idSousTache);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_ACCEPTABLE)
                    .body("Erreur lors de l’assignation : " + e.getMessage()); // HTTP 406 avec message
        }
    }

}
