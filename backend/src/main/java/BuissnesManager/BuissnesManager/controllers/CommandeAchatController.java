package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.entity.BonReception;
import BuissnesManager.BuissnesManager.entity.CommandeAchat;
import BuissnesManager.BuissnesManager.entity.FactureAchat;
import BuissnesManager.BuissnesManager.dto.CommandeAchatDto;
import BuissnesManager.BuissnesManager.dto.LigneCommandeAchatDto;
import BuissnesManager.BuissnesManager.service.CommandeAchatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/commandes-achat")
@PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
public class CommandeAchatController {

    private static final Logger logger = LoggerFactory.getLogger(CommandeAchatController.class);

    @Autowired
    private CommandeAchatService commandeAchatService;

    @GetMapping
    public ResponseEntity<List<CommandeAchat>> getAllCommandes() {
        try {
            logger.info("📋 Getting all purchase orders");
            return ResponseEntity.ok(commandeAchatService.getAllCommandes());
        } catch (Exception e) {
            logger.error("❌ Error getting commandes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommandeAchat> getCommandeById(@PathVariable Long id) {
        try {
            logger.info("📋 Getting commande with ID: {}", id);
            return commandeAchatService.getCommandeById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("❌ Error getting commande", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/entreprise/{entrepriseId}")
    public ResponseEntity<List<CommandeAchat>> getCommandesByEntreprise(@PathVariable Long entrepriseId) {
        try {
            logger.info("📋 Getting commandes for entreprise: {}", entrepriseId);
            return ResponseEntity.ok(commandeAchatService.getCommandesByEntreprise(entrepriseId));
        } catch (Exception e) {
            logger.error("❌ Error getting commandes by entreprise", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> createCommande(@RequestBody CommandeAchatDto dto) {
        try {
            logger.info("📥 Received DTO from frontend:");
            logger.info("   Reference: {}", dto.getReference());
            logger.info("   Fournisseur ID: {}", dto.getFournisseurId());
            logger.info("   Enterprise ID: {}", dto.getEntrepriseId());
            logger.info("   Number of lines: {}", dto.getLignes() != null ? dto.getLignes().size() : 0);
            
            // ✅ LOG EACH LINE IN DETAIL
            if (dto.getLignes() != null && !dto.getLignes().isEmpty()) {
                for (int i = 0; i < dto.getLignes().size(); i++) {
                    LigneCommandeAchatDto ligne = dto.getLignes().get(i);
                    logger.info("   ========== LINE {} ==========", i + 1);
                    logger.info("      articleAchatId: {} (type: {})", 
                        ligne.getArticleAchatId(), 
                        ligne.getArticleAchatId() == null ? "NULL" : ligne.getArticleAchatId().getClass().getSimpleName());
                    logger.info("      quantite: {}", ligne.getQuantite());
                    logger.info("      prixUnitaireHT: {}", ligne.getPrixUnitaireHT());
                    logger.info("      tva: {}", ligne.getTva());
                    logger.info("   ===========================");
                }
            }
            
            CommandeAchat commande = commandeAchatService.createCommande(dto);
            return ResponseEntity.ok(commande);
        } catch (Exception e) {
            logger.error("❌ Error creating commande: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            errorResponse.put("details", e.getCause() != null ? e.getCause().getMessage() : "No details");
            errorResponse.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(400).body(errorResponse);
        }
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCommande(@PathVariable Long id, @RequestBody CommandeAchatDto dto) {
        try {
            logger.info("✏️ Updating commande with ID: {}", id);
            CommandeAchat commande = commandeAchatService.updateCommande(id, dto);
            return ResponseEntity.ok(commande);
        } catch (RuntimeException e) {
            logger.error("❌ Commande not found: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("❌ Error updating commande", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erreur lors de la mise à jour"));
        }
    }

    @PatchMapping("/{id}/statut")
    public ResponseEntity<?> updateStatut(@PathVariable Long id, @RequestParam String statut) {
        try {
            logger.info("🔄 Updating status for commande {}: {}", id, statut);
            CommandeAchat commande = commandeAchatService.updateStatut(id, statut);
            return ResponseEntity.ok(commande);
        } catch (RuntimeException e) {
            logger.error("❌ Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("❌ Error updating status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erreur lors de la mise à jour du statut"));
        }
    }

    @PutMapping("/{id}/valider")
    public ResponseEntity<?> validerCommande(@PathVariable Long id) {
        try {
            logger.info("🔄 [VALIDATION] Starting invoice generation for order ID: {}", id);
            FactureAchat facture = commandeAchatService.validerCommande(id);
            logger.info("✅ [VALIDATION] Invoice created successfully with ID: {}", facture.getId());
            return ResponseEntity.ok(facture);
        } catch (RuntimeException e) {
            logger.error("❌ [VALIDATION ERROR] {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("❌ [VALIDATION CRITICAL ERROR] Unexpected error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erreur lors de la génération de la facture: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/recevoir")
    public ResponseEntity<?> recevoirCommande(@PathVariable Long id) {
        try {
            logger.info("📦 [RECEPTION] Receiving order ID: {}", id);
            BonReception bon = commandeAchatService.recevoirCommande(id);
            logger.info("✅ [RECEPTION] Bon réception {} created, stock updated", bon.getId());
            return ResponseEntity.ok(bon);
        } catch (RuntimeException e) {
            logger.error("❌ [RECEPTION ERROR] {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("❌ [RECEPTION CRITICAL ERROR] Unexpected error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erreur lors de la réception: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/annuler")
    public ResponseEntity<?> annulerCommande(@PathVariable Long id) {
        try {
            logger.info("🚫 Cancelling commande with ID: {}", id);
            CommandeAchat commande = commandeAchatService.annulerCommande(id);
            return ResponseEntity.ok(commande);
        } catch (RuntimeException e) {
            logger.error("❌ Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("❌ Error cancelling commande", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erreur lors de l'annulation"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCommande(@PathVariable Long id) {
        try {
            logger.info("🗑️ Deleting commande with ID: {}", id);
            commandeAchatService.deleteCommande(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("❌ Error deleting commande", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Erreur lors de la suppression"));
        }
    }

    // Inner ErrorResponse class
    public static class ErrorResponse {
        private String message;
        private long timestamp;

        public ErrorResponse(String message) {
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }

        public String getMessage() { return message; }
        public long getTimestamp() { return timestamp; }
    }
}