package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.entity.CaracteristiqueFactureEntreprise;
import BuissnesManager.BuissnesManager.entity.Entreprise;
import BuissnesManager.BuissnesManager.service.EntrepriseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/entreprise")
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class EntrepriseController {

    private static final Logger logger = LoggerFactory.getLogger(EntrepriseController.class);

    @Autowired
    private EntrepriseService entrepriseService;

    @PostMapping("/create")
    public ResponseEntity<?> ajouterEntreprise(@RequestBody Entreprise entreprise) {
        try {
            logger.info("═══════════════════════════════════════");
            logger.info("✅ POST /entreprise/create RECEIVED");
            logger.info("📦 Nom: " + entreprise.getNom());
            logger.info("📦 Email: " + entreprise.getEmail());
            logger.info("═══════════════════════════════════════");

            if (entreprise.getNom() == null || entreprise.getNom().isEmpty()) {
                logger.warn("⚠️ Nom is required!");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Nom is required");
            }

            Entreprise saved = entrepriseService.ajouterEntreprise(entreprise);
            logger.info("✅ Entreprise saved with ID: " + saved.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);

        } catch (Exception e) {
            logger.error("❌ Error creating entreprise: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
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

    @PostMapping("/assignerUserToEntreprise")
    public ResponseEntity<?> assignerUserToEntreprise(@RequestParam String username, @RequestParam String nom) {
        try {
            entrepriseService.assignerUserToEntreprise(username, nom);
            return ResponseEntity.status(HttpStatus.OK).body("assigner avec succes");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
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

    @GetMapping("/getAll")
    public ResponseEntity<?> getAllEntreprise() {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(entrepriseService.getAllEntreprise());
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Entreprise non trouvee");
        }
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<?> getEntrepriseById(@PathVariable Long id) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(entrepriseService.getById(id));
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Entreprise non trouvee");
        }
    }

    @PostMapping("/ajouterCaracteristiqueFactureEntreprise")
    public ResponseEntity<?> ajouterCaracteristiqueFactureEntreprise(@RequestBody CaracteristiqueFactureEntreprise caracteristiqueFactureEntreprise) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(entrepriseService.addCaracteristiqueFactureEntreprise(caracteristiqueFactureEntreprise));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}