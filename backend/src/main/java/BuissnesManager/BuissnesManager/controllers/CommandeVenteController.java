package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.dto.CommandeVenteDto;
import BuissnesManager.BuissnesManager.entity.CommandeVente;
import BuissnesManager.BuissnesManager.entity.FactureDeVente;
import BuissnesManager.BuissnesManager.service.CommandeVenteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/commandes-vente")
@PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
public class CommandeVenteController {

    private static final Logger logger = LoggerFactory.getLogger(CommandeVenteController.class);

    @Autowired
    private CommandeVenteService commandeVenteService;

    @PostMapping("/add")
    public ResponseEntity<?> createCommande(@RequestBody CommandeVenteDto dto) {
        try {
            logger.info("? Received DTO from frontend:");
            logger.info("   Reference: {}", dto.getReference());
            logger.info("   Client ID: {}", dto.getClientId());
            logger.info("   Enterprise ID: {}", dto.getEntrepriseId());
            logger.info("   Number of lines: {}", dto.getLignes() != null ? dto.getLignes().size() : 0);

            if (dto.getLignes() != null) {
                for (int i = 0; i < dto.getLignes().size(); i++) {
                    logger.info("   ========== LINE {} ==========", i + 1);
                    logger.info("      articleAchatId: {} (type: Long)", dto.getLignes().get(i).getArticleVenteId());
                    logger.info("      quantite: {}", dto.getLignes().get(i).getQuantite());
                    logger.info("      prixUnitaireHT: {}", dto.getLignes().get(i).getPrixUnitaireHT());
                    logger.info("      tva: {}", dto.getLignes().get(i).getTva());
                    logger.info("   ===========================");
                }
            }

            CommandeVente created = commandeVenteService.createCommande(dto);
            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            logger.error("❌ Error creating commande vente: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            errorResponse.put("details", e.getCause() != null ? e.getCause().getMessage() : null);
            errorResponse.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            logger.error("❌ Unexpected error creating commande vente: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Une erreur système est survenue");
            errorResponse.put("details", e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    public ResponseEntity<?> getAllCommandes() {
        try {
            logger.info("? Getting all purchase orders");
            List<CommandeVente> commandes = commandeVenteService.getAllCommandes();
            return ResponseEntity.ok(commandes);
        } catch (Exception e) {
            logger.error("Error getting commandes: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCommandeById(@PathVariable Long id) {
        try {
            logger.info("? Getting commande with ID: {}", id);
            Optional<CommandeVente> commande = commandeVenteService.getCommandeById(id);
            return commande.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        } catch (Exception e) {
            logger.error("Error getting commande: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCommande(@PathVariable Long id, @RequestBody CommandeVenteDto dto) {
        try {
            CommandeVente updated = commandeVenteService.updateCommande(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            logger.error("Error updating commande: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(errorBody(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating commande: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorBody("Erreur lors de la mise à jour de la commande"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCommande(@PathVariable Long id) {
        try {
            commandeVenteService.deleteCommande(id);
            Map<String, Object> body = new HashMap<>();
            body.put("message", "Commande deleted successfully");
            body.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(body);
        } catch (RuntimeException e) {
            logger.error("Error deleting commande: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(errorBody(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error deleting commande: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorBody("Erreur lors de la suppression"));
        }
    }

    @PutMapping("/{id}/valider")
    public ResponseEntity<?> validerCommande(@PathVariable Long id) {
        try {
            logger.info("? [VALIDATION] Starting invoice generation for order ID: {}", id);
            
            // ✅ FIXED: Return type is now FactureDeVente (not FactureVente)
            FactureDeVente facture = commandeVenteService.validerCommande(id);
            
            logger.info("? [VALIDATION] Invoice created successfully with ID: {}", facture.getId());
            return ResponseEntity.ok(facture);
        } catch (RuntimeException e) {
            logger.error("? [VALIDATION] Error during validation: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(errorBody(e.getMessage()));
        } catch (Exception e) {
            logger.error("? [VALIDATION] Error during validation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorBody("Erreur lors de la validation de la commande"));
        }
    }

    @PutMapping("/{id}/annuler")
    public ResponseEntity<?> annulerCommande(@PathVariable Long id) {
        try {
            CommandeVente commande = commandeVenteService.annulerCommande(id);
            return ResponseEntity.ok(commande);
        } catch (RuntimeException e) {
            logger.error("Error cancelling commande: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(errorBody(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error cancelling commande: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorBody("Erreur lors de l'annulation"));
        }
    }

    @PutMapping("/{id}/livrer")
    public ResponseEntity<?> livrerCommande(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(commandeVenteService.livrerCommande(id));
        } catch (RuntimeException e) {
            logger.error("Error delivering commande: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(errorBody(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error delivering commande: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorBody("Erreur lors de la livraison"));
        }
    }

    private Map<String, Object> errorBody(String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", message);
        body.put("timestamp", System.currentTimeMillis());
        return body;
    }
}