package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.entity.FactureDeVente;
import BuissnesManager.BuissnesManager.service.FactureDeVenteService;
import BuissnesManager.BuissnesManager.service.FactureOcrService;
import BuissnesManager.BuissnesManager.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * REST controller for sales invoices (FactureDeVente) under the {@code /factures-vente} prefix.
 * <p>This is the canonical entry point for all sales invoice operations:
 * CRUD ({@code ajouterFacture}, {@code getAllFactures}, {@code updateFacture/{id}},
 * {@code deleteFacture/{id}}), client assignment, email send, and OCR PDF import.
 * Purchase invoices (FactureAchat) live in {@link FactureController} under {@code /facture}.</p>
 */
@RestController
@RequestMapping("factures-vente")
public class FactureDeVenteController {

    @Autowired
    private FactureDeVenteService factureDeVenteService;

    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
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

    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
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

    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @DeleteMapping("deleteFacture/{id}")
    public ResponseEntity<Void> deleteFacture(@PathVariable Long id) {
        try {
            factureDeVenteService.deleteFacture(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }



    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @GetMapping("client/{clientId}")
    public ResponseEntity<List<FactureDeVente>> getFacturesByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(factureDeVenteService.getFacturesByClient(clientId));
    }


    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @PutMapping("affecterFactureClient/{idFacture}/{idClient}")

    public ResponseEntity<Void> affecterFactureClient(
            @PathVariable Long idFacture,
            @PathVariable Long idClient) {

        factureDeVenteService.affecterFactureClient(idFacture, idClient);
        return ResponseEntity.ok().build();
    }


    @Autowired
    private MailService mailService;

    @Autowired
    private FactureOcrService factureOcrService;

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


    /**
     * Importer les champs d'une facture de vente depuis un fichier PDF en
     * exécutant l'OCR sur les zones précédemment enregistrées pour
     * {@code typeDeDocument = "FACTURE_VENTE"} via le sélecteur de zones.
     * <p>Renvoie un payload pré-rempli au format du formulaire d'ajout (clé
     * {@code dateAchat} pour la date, {@code detaisFactureDeVente} pour la
     * ligne, …).</p>
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @PostMapping(value = "/import-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importFromPdf(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Fichier PDF requis"));
        }
        try {
            FactureOcrService.ExtractionResult result =
                    factureOcrService.extractFromPdf(file, FactureOcrService.TYPE_FACTURE_VENTE);
            return ResponseEntity.ok(factureOcrService.toFactureDeVentePayload(result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Échec de l'extraction OCR : " + e.getMessage()));
        }
    }
}
