package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.entity.SousTache;
import BuissnesManager.BuissnesManager.entity.StatutProjet;
import BuissnesManager.BuissnesManager.entity.StatutTache;
import BuissnesManager.BuissnesManager.entity.Tache;
import BuissnesManager.BuissnesManager.service.TacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/taches")
public class TacheController {

    @Autowired
    private TacheService tacheService;

    @PostMapping("addTache/{idCategorieTache}/{idProjet}")
    public ResponseEntity<?> createTache(@RequestBody Tache tache,@PathVariable Long idCategorieTache , @PathVariable Long idProjet) {
        return ResponseEntity.ok(tacheService.createTache(tache, idCategorieTache, idProjet));
    }

    @GetMapping("getAll")
    public ResponseEntity<?> getAllTaches() {
        return ResponseEntity.ok(tacheService.getAllTaches());
    }

    @GetMapping("getById/{id}")
    public ResponseEntity<?> getTacheById(@PathVariable Long id) {
        return tacheService.getTacheById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("update/{id}")
    public ResponseEntity<?> updateTache(@PathVariable Long id, @RequestBody Tache tache) {
        try {
            return ResponseEntity.ok(tacheService.updateTache(id, tache));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> deleteTache(@PathVariable Long id) {
        try {
            tacheService.deleteTache(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping("getAllTacheByProjet/{idProjet}")
    public ResponseEntity<?> getAllTacheByProjet(@PathVariable Long idProjet) {
        return ResponseEntity.status(HttpStatus.OK).body(tacheService.getAllTachesByProjet(idProjet)) ;

    }

    @PutMapping("/assign-tache-membre/{idTache}/{idMembre}")
    public ResponseEntity<Void> assignTacheToMembre(
            @PathVariable Long idTache,
            @PathVariable Long idMembre) {
        try {
            tacheService.assigneTacheToMembre(idTache, idMembre);
            return ResponseEntity.ok().build();
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @PutMapping("/{id}/{statutTache}")
    public ResponseEntity<Void> updateStatutTache(
            @PathVariable Long id,
            @PathVariable StatutTache statutTache

    ) {
        try {

            tacheService.updateTacheStatut(id, statutTache);

            return ResponseEntity.ok().build();
        }catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/count-by-status")
    public ResponseEntity<Integer> getNombreTacheByStatus(@RequestParam StatutTache statut) {
        int count = tacheService.nombreTacheByStatus(statut);
        return ResponseEntity.ok(count);
    }


    @GetMapping("getSousTacheById/{id}")
    public ResponseEntity<?> getSousTacheById(@PathVariable Long id) {
        try {
            tacheService.getSousTacheById(id);
            return ResponseEntity.ok().build();
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("updateSousTache/{id}")
    public ResponseEntity<?> updateSousTache(@PathVariable Long id, @RequestBody SousTache sousTache) {
        try {
            return ResponseEntity.ok(tacheService.updateSousTache(id, sousTache));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }



}
