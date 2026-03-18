package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.entity.Projet;
import BuissnesManager.BuissnesManager.entity.StatutProjet;
import BuissnesManager.BuissnesManager.service.ProjetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projets")
public class ProjetController {

    @Autowired
    private ProjetService projetService;

    @PostMapping("addProjet/{idCategorieProjet}")
    public ResponseEntity<?> createProjet(@RequestBody Projet projet,@PathVariable Long idCategorieProjet) {
        Projet newProjet = projetService.createProjet(projet,idCategorieProjet);
        return ResponseEntity.ok(newProjet);
    }
    @PostMapping("addProjetCategorie")
    public ResponseEntity<?> addProjet(@RequestBody Projet projet) {
        Projet newProjet = projetService.addProjet(projet);
        return ResponseEntity.ok(newProjet);
    }

    @GetMapping("getAll")
    public ResponseEntity<?> getAllProjets() {
        List<Projet> projets = projetService.getAllProjets();
        return ResponseEntity.ok(projets);
    }
    @GetMapping("getAllProjetNotBudget")
    public ResponseEntity<?> getAllProjetNotBudget() {
        List<Projet> projets = projetService.getAllProjetNotBudget();
        return ResponseEntity.ok(projets);
    }

    @GetMapping("getById/{id}")
    public ResponseEntity<?> getProjetById(@PathVariable Long id) {
        return projetService.getProjetById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("update/{id}")
    public ResponseEntity<?> updateProjet(@PathVariable Long id, @RequestBody Projet projet) {
        try {
            Projet updated = projetService.updateProjet(id, projet);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> deleteProjet(@PathVariable Long id) {
        try {
            projetService.deleteProjet(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("getAllByStatus")
    public ResponseEntity<?> getAllByStatus(@RequestParam("statusProjet") StatutProjet statusProjet) {
        List<Projet> projets = projetService.getAllProjetByStatus(statusProjet);
        return ResponseEntity.ok(projets);
    }

    @PutMapping("/updateStatut/{idProjet}")
    public ResponseEntity<?> updateStatutProjet(@PathVariable Long idProjet,
                                                @RequestParam StatutProjet statutProjet) {
        try {
            projetService.update(idProjet, statutProjet);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }
    }
    @GetMapping("/count-by-status")
    public ResponseEntity<Integer> getNombreProjetByStatus(@RequestParam StatutProjet statut) {
        int count = projetService.nombreProjetByStatus(statut);
        return ResponseEntity.ok(count);
    }
    @GetMapping("/{id}/nombre-taches")
    public ResponseEntity<Integer> nombreDeTacheByProjet(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(projetService.nombreDeTacheByProjet(id));
        } catch (Exception e){
            return ResponseEntity.notFound().build();
        }
    }

}
