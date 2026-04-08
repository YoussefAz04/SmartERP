package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.dto.CommandeVenteDto;
import BuissnesManager.BuissnesManager.entity.BonLivraison;
import BuissnesManager.BuissnesManager.entity.CommandeVente;
import BuissnesManager.BuissnesManager.entity.FactureVente;
import BuissnesManager.BuissnesManager.service.CommandeVenteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/commandes-vente")
public class CommandeVenteController {

    @Autowired
    private CommandeVenteService commandeVenteService;

    @GetMapping
    public ResponseEntity<List<CommandeVente>> getAllCommandes() {
        try {
            return ResponseEntity.ok(commandeVenteService.getAllCommandes());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommandeVente> getCommandeById(@PathVariable Long id) {
        try {
            return commandeVenteService.getCommandeById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/entreprise/{entrepriseId}")
    public ResponseEntity<List<CommandeVente>> getCommandesByEntreprise(@PathVariable Long entrepriseId) {
        try {
            return ResponseEntity.ok(commandeVenteService.getCommandesByEntreprise(entrepriseId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<CommandeVente> createCommande(@RequestBody CommandeVenteDto dto) {
        try {
            CommandeVente commande = commandeVenteService.createCommande(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(commande);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommandeVente> updateCommande(@PathVariable Long id, @RequestBody CommandeVenteDto dto) {
        try {
            CommandeVente commande = commandeVenteService.updateCommande(id, dto);
            return ResponseEntity.ok(commande);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/{id}/statut")
    public ResponseEntity<CommandeVente> updateStatut(@PathVariable Long id, @RequestParam String statut) {
        try {
            CommandeVente commande = commandeVenteService.updateStatut(id, statut);
            return ResponseEntity.ok(commande);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}/valider")
    public ResponseEntity<FactureVente> validerCommande(@PathVariable Long id) {
        try {
            FactureVente facture = commandeVenteService.validerCommande(id);
            return ResponseEntity.ok(facture);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}/livrer")
    public ResponseEntity<?> livrerCommande(@PathVariable Long id) {
        try {
            BonLivraison bon = commandeVenteService.livrerCommande(id);
            return ResponseEntity.ok(bon);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}/annuler")
    public ResponseEntity<CommandeVente> annulerCommande(@PathVariable Long id) {
        try {
            CommandeVente commande = commandeVenteService.annulerCommande(id);
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
            commandeVenteService.deleteCommande(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
