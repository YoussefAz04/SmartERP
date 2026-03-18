package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.entity.FactureDeVente;
import BuissnesManager.BuissnesManager.service.FactureDeVenteService;
import BuissnesManager.BuissnesManager.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("factures-vente")
public class FactureDeVenteController {

    @Autowired
    private FactureDeVenteService factureDeVenteService;

    @PostMapping("ajouterFacture")
    public ResponseEntity<FactureDeVente> ajouterFacture(@RequestBody FactureDeVente facture) {
        FactureDeVente savedFacture = factureDeVenteService.ajouterFactureDeVente(facture);
        return ResponseEntity.ok(savedFacture);
    }

    @GetMapping("getAllFactures")
    public ResponseEntity<List<FactureDeVente>> getAllFactures() {
        return ResponseEntity.ok(factureDeVenteService.getAllFactures());
    }

    @GetMapping("getFactureById/{id}")
    public ResponseEntity<FactureDeVente> getFactureById(@PathVariable Long id) {
        return factureDeVenteService.getFactureById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("updateFacture/{id}")
    public ResponseEntity<FactureDeVente> updateFacture(@PathVariable Long id,
                                                        @RequestBody FactureDeVente updatedFacture) {
        try {
            FactureDeVente facture = factureDeVenteService.updateFactureVente(id, updatedFacture);
            return ResponseEntity.ok(facture);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("deleteFacture/{id}")
    public ResponseEntity<Void> deleteFacture(@PathVariable Long id) {
        try {
            factureDeVenteService.deleteFacture(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }



    @PutMapping("affecterFactureClient/{idFacture}/{idClient}")

    public ResponseEntity<String> affecterFactureClient(
            @PathVariable Long idFacture,
            @PathVariable Long idClient) {

        factureDeVenteService.affecterFatureClient(idFacture, idClient);
        return ResponseEntity.ok().build();
    }


    @Autowired
    private MailService mailService;

    @PostMapping(value = "/send-facture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> sendFacture(
            @RequestParam("file") MultipartFile file,
            @RequestParam("to") String to,
            @RequestParam("subject") String subject,
            @RequestParam("text") String text
    ) {
        try {
            mailService.sendMailWithAttachment(to, subject, text, file);
            return ResponseEntity.ok("Email envoyé avec succès !");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur : " + e.getMessage());
        }
    }
}
