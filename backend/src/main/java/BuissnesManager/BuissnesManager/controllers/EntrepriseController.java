package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.entity.Entreprise;
import BuissnesManager.BuissnesManager.service.EntrepriseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/entreprise")
<<<<<<< Updated upstream
@CrossOrigin(origins = "http://localhost:4200")
=======
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
>>>>>>> Stashed changes
public class EntrepriseController {

    @Autowired
    private EntrepriseService entrepriseService;

    @PostMapping("/create")
    public ResponseEntity<?> ajouterEntreprise(@RequestBody Entreprise entreprise){
<<<<<<< Updated upstream
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(entrepriseService.ajouterEntreprise(entreprise));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
=======
        System.out.println("✅ POST /entreprise/create received");
        System.out.println("Data: nom=" + entreprise.getNom() + ", raison=" + entreprise.getRaisonSociale());
        try {
            Entreprise saved = entrepriseService.ajouterEntreprise(entreprise);
            System.out.println("✅ Saved with ID: " + saved.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e){
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
>>>>>>> Stashed changes
        }
    }

    @GetMapping("/getAll")
    public ResponseEntity<?> getAllEntreprise(){
        System.out.println("✅ GET /entreprise/getAll received");
        try {
            return ResponseEntity.ok(entrepriseService.getAllEntreprises());
        }catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<?> getEntrepriseById(@PathVariable Long id){
        System.out.println("✅ GET /entreprise/getById/" + id + " received");
        try {
            return ResponseEntity.ok(entrepriseService.getById(id));
        }catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateEntreprise(@PathVariable Long id, @RequestBody Entreprise entrepriseDetails) {
        System.out.println("✅ PUT /entreprise/update/" + id + " received");
        try {
            Entreprise updated = entrepriseService.updateEntreprise(id, entrepriseDetails);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/assignerUserToEntreprise")
    public ResponseEntity<?> assignerUserToEntreprise(@RequestParam String username, @RequestParam String nom){
<<<<<<< Updated upstream
=======
        System.out.println("✅ POST /entreprise/assignerUserToEntreprise received");
>>>>>>> Stashed changes
        try {
            entrepriseService.assignerUserToEntreprise(username, nom);
            return ResponseEntity.ok("Success");
        }catch (Exception e){
<<<<<<< Updated upstream
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
=======
            System.err.println("❌ Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
>>>>>>> Stashed changes
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteEntreprise(@PathVariable Long id) {
        System.out.println("✅ DELETE /entreprise/delete/" + id + " received");
        try {
            entrepriseService.deleteEntreprise(id);
            return ResponseEntity.ok("Deleted");
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
<<<<<<< Updated upstream

    @GetMapping("/getAll")
    public ResponseEntity<?> getAllEntreprise(){
        try {
            return ResponseEntity.status(HttpStatus.OK).body(entrepriseService.getAllEntreprise());

        }catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Entreprise non trouver");
        }
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<?> getEntrepriseById(@PathVariable Long id){
        try {
            return ResponseEntity.status(HttpStatus.OK).body(entrepriseService.getById(id));

        }catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Entreprise non trouver");
        }
    }


    @PostMapping("/ajouterCaracteristiqueFactureEntreprise")
    public ResponseEntity<?> ajouterCaracteristiqueFactureEntreprise(@RequestBody CaracteristiqueFactureEntreprise caracteristiqueFactureEntreprise){
        try {
            return ResponseEntity.status(HttpStatus.OK).body(entrepriseService.addCaracteristiqueFactureEntreprise(caracteristiqueFactureEntreprise));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


}
=======
}
>>>>>>> Stashed changes
