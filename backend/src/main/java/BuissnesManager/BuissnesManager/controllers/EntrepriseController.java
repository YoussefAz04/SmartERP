package BuissnesManager.BuissnesManager.controllers;


import BuissnesManager.BuissnesManager.entity.CaracteristiqueFactureEntreprise;
import BuissnesManager.BuissnesManager.entity.Entreprise;
import BuissnesManager.BuissnesManager.service.EntrepriseService;
import org.apache.catalina.valves.rewrite.RewriteCond;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("entreprise")
public class EntrepriseController {

    @Autowired
    private EntrepriseService entrepriseService ;

    @PostMapping("create")
    private ResponseEntity<?> ajouterEntreprise(@RequestBody Entreprise entreprise){
        try {
            return ResponseEntity.status(HttpStatus.OK).body(entrepriseService.ajouterEntreprise(entreprise));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e.getMessage());
        }
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateEntreprise(@PathVariable Long id, @RequestBody Entreprise entrepriseDetails) {
        try {
            Entreprise updatedEntreprise = entrepriseService.updateEntreprise(id, entrepriseDetails);
            return ResponseEntity.ok(updatedEntreprise);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("assignerUserToEntreprise")
    private ResponseEntity<?> assignerUserToEntreprise(@RequestParam String username, @RequestParam String nom){
        try {
            entrepriseService.assignerUserToEntreprise(username,nom);
             return   ResponseEntity.status(HttpStatus.OK).body("assigner avec sucsse");
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteEntreprise(@PathVariable Long id) {
        try {
            entrepriseService.deleteEntreprise(id);
            return ResponseEntity.ok("Entreprise supprimée avec succès.");
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Entreprise non trouvée avec ID : " + id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la suppression : " + e.getMessage());
        }
    }
    @GetMapping("getAll")
    private ResponseEntity<?> getAllEntreprise(){
        try {
            return ResponseEntity.status(HttpStatus.OK).body(entrepriseService.getAllEntreprise());

        }catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Entreprise non trouver");
        }
    }
    @GetMapping("getById/{id}")
    private ResponseEntity<?> getEntrepriseById(@PathVariable Long id){
        try {
            return ResponseEntity.status(HttpStatus.OK).body(entrepriseService.getById(id));

        }catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Entreprise non trouver");
        }
    }


    @PostMapping("ajouterCaracteristiqueFactureEntreprise")
    private ResponseEntity<?> ajouterCaracteristiqueFactureEntreprise(@RequestBody CaracteristiqueFactureEntreprise caracteristiqueFactureEntreprise){
        try {
            return ResponseEntity.status(HttpStatus.OK).body(entrepriseService.addCaracteristiqueFactureEntreprise(caracteristiqueFactureEntreprise));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e.getMessage());
        }
    }


}
