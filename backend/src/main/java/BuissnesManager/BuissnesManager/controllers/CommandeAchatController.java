package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.dto.CommandeAchatDto;
import BuissnesManager.BuissnesManager.entity.CommandeAchat;
import BuissnesManager.BuissnesManager.entity.FactureAchat;
import BuissnesManager.BuissnesManager.service.CommandeAchatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/commandes-achat")
public class CommandeAchatController {

    @Autowired
    private CommandeAchatService commandeAchatService;

    @GetMapping
    public ResponseEntity<List<CommandeAchat>> getAllCommandes() {
        try {
            return ResponseEntity.ok(commandeAchatService.getAllCommandes());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommandeAchat> getCommandeById(@PathVariable Long id) {
        try {
            return commandeAchatService.getCommandeById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/entreprise/{entrepriseId}")
    public ResponseEntity<List<CommandeAchat>> getCommandesByEntreprise(@PathVariable Long entrepriseId) {
        try {
            return ResponseEntity.ok(commandeAchatService.getCommandesByEntreprise(entrepriseId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<CommandeAchat> createCommande(@RequestBody CommandeAchatDto dto) {
        try {
            CommandeAchat commande = commandeAchatService.createCommande(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(commande);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommandeAchat> updateCommande(@PathVariable Long id, @RequestBody CommandeAchatDto dto) {
        try {
            CommandeAchat commande = commandeAchatService.updateCommande(id, dto);
            return ResponseEntity.ok(commande);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/{id}/statut")
    public ResponseEntity<CommandeAchat> updateStatut(@PathVariable Long id, @RequestParam String statut) {
        try {
            CommandeAchat commande = commandeAchatService.updateStatut(id, statut);
            return ResponseEntity.ok(commande);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}/valider")
    public ResponseEntity<FactureAchat> validerCommande(@PathVariable Long id) {
        try {
            FactureAchat facture = commandeAchatService.validerCommande(id);
            return ResponseEntity.ok(facture);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}/annuler")
    public ResponseEntity<CommandeAchat> annulerCommande(@PathVariable Long id) {
        try {
            CommandeAchat commande = commandeAchatService.annulerCommande(id);
            return ResponseEntity.ok(commande);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCommande(@PathVariable Long id) {
        try {
            commandeAchatService.deleteCommande(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
