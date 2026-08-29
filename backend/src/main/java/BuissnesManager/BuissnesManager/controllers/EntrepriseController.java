package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.entity.CaracteristiqueFactureEntreprise;
import BuissnesManager.BuissnesManager.entity.Entreprise;
import BuissnesManager.BuissnesManager.security.AuthorizationUtil;
import BuissnesManager.BuissnesManager.service.EntrepriseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/entreprise")
public class EntrepriseController {

    private static final Logger logger = LoggerFactory.getLogger(EntrepriseController.class);

    @Autowired
    private EntrepriseService entrepriseService;

    @Autowired
    private AuthorizationUtil authorizationUtil;

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<?> ajouterEntreprise(@RequestBody Entreprise entreprise) {
        try {
            logger.info("═══════════════════════════════════════");
            logger.info("POST /entreprise/create RECEIVED");
            logger.info("Nom: " + entreprise.getNom());
            logger.info(" Email: " + entreprise.getEmail());
            logger.info("═══════════════════════════════════════");

            if (entreprise.getNom() == null || entreprise.getNom().isEmpty()) {
                logger.warn("Nom is required!");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Nom is required");
            }

            Entreprise saved = entrepriseService.ajouterEntreprise(entreprise);
            logger.info("Entreprise saved with ID: " + saved.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);

        } catch (Exception e) {
            logger.error("Error creating entreprise: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @PreAuthorize("@authorizationUtil.isAdminOfEnterprise(#id)")
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateEntreprise(
            @PathVariable Long id,
            @RequestBody Entreprise entrepriseDetails) {
        try {
            Entreprise updatedEntreprise = entrepriseService.updateEntreprise(id, entrepriseDetails);
            return ResponseEntity.ok(updatedEntreprise);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/assignerUserToEntreprise")
    public ResponseEntity<?> assignerUserToEntreprise(@RequestParam String username, @RequestParam String nom) {
        try {
            entrepriseService.assignerUserToEntreprise(username, nom);
            return ResponseEntity.status(HttpStatus.OK).body("assigner avec succes");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/disable/{id}")
    public ResponseEntity<?> disableEntreprise(@PathVariable Long id) {
        try {
            entrepriseService.disableEntreprise(id);
            return ResponseEntity.ok("Entreprise désactivée avec succès.");
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Entreprise non trouvée avec ID : " + id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error lors de la désactivation : " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/enable/{id}")
    public ResponseEntity<?> enableEntreprise(@PathVariable Long id) {
        try {
            entrepriseService.enableEntreprise(id);
            return ResponseEntity.ok("Entreprise réactivée avec succès.");
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Entreprise non trouvée avec ID : " + id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error lors de la réactivation : " + e.getMessage());
        }
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @GetMapping("/getAll")
    public ResponseEntity<?> getAllEntreprise() {
        try {
            if (!authorizationUtil.isSuperAdmin()) {
                Long entrepriseId = authorizationUtil.getCurrentEnterpriseId();
                if (entrepriseId == null) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Enterprise context required");
                }
                return ResponseEntity.ok(List.of(entrepriseService.getById(entrepriseId)));
            }
            return ResponseEntity.status(HttpStatus.OK).body(entrepriseService.getAllEntreprise());
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Entreprise non trouvee");
        }
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/getAllWithInactive")
    public ResponseEntity<?> getAllEntreprisesWithInactive() {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(entrepriseService.getAllEntreprises());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error lors du chargement : " + e.getMessage());
        }
    }

    @PreAuthorize("@authorizationUtil.canAccessEnterprise(#id)")
    @GetMapping("/getById/{id}")
    public ResponseEntity<?> getEntrepriseById(@PathVariable Long id) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(entrepriseService.getById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Entreprise non trouvee");
        }
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    @PostMapping("/ajouterCaracteristiqueFactureEntreprise")
    public ResponseEntity<?> ajouterCaracteristiqueFactureEntreprise(@RequestBody CaracteristiqueFactureEntreprise caracteristiqueFactureEntreprise) {
        try {
            if (caracteristiqueFactureEntreprise.getEntreprise() == null
                    || !authorizationUtil.isAdminOfEnterprise(caracteristiqueFactureEntreprise.getEntreprise().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied for this enterprise");
            }
            return ResponseEntity.status(HttpStatus.OK).body(entrepriseService.addCaracteristiqueFactureEntreprise(caracteristiqueFactureEntreprise));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
