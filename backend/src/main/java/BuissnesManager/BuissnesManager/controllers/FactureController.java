package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.entity.Entreprise;
import BuissnesManager.BuissnesManager.entity.FactureAchat;
import BuissnesManager.BuissnesManager.repository.EntrepriseRepo;
import BuissnesManager.BuissnesManager.repository.FactureAchatRepo;
import BuissnesManager.BuissnesManager.security.EnterpriseSecurityUtil;
import BuissnesManager.BuissnesManager.service.CloudinaryService;
import BuissnesManager.BuissnesManager.service.FactureOcrService;
import BuissnesManager.BuissnesManager.service.FactureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("facture")
public class FactureController {

    @Autowired
    private FactureService factureService ;

    @Autowired
    private FactureOcrService factureOcrService;

    @Autowired
    private FactureAchatRepo factureAchatRepo;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private EntrepriseRepo entrepriseRepo;

    @Autowired
    private EnterpriseSecurityUtil enterpriseSecurityUtil;



    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @PostMapping("addFacture/{idDocument}")
    public ResponseEntity<?> ajouterFacture(@PathVariable Long idDocument) throws Exception {
        try {
            factureService.AjouterFactureAchat(idDocument) ;
            return ResponseEntity.ok().build() ;
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("error: " + e.getMessage());
        }
    }

    @GetMapping("/getAllFactureAchat")
    public ResponseEntity<?> getAllFactureAchat(){
        try{
            return ResponseEntity.ok(factureService.getAllFactureAchat());
        }catch (Exception e){
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("getFactureAchatById/{id}")
    public ResponseEntity<?> getFactureAchatById(@PathVariable Long id ){
        try{
            return ResponseEntity.ok(factureService.getById(id));
        }catch (Exception e){
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/fournisseur/{fournisseurId}")
    public ResponseEntity<?> getFacturesByFournisseur(@PathVariable Long fournisseurId) {
        try {
            return ResponseEntity.ok(factureAchatRepo.findByFournisseurId(fournisseurId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @PutMapping("updateFactureAchat/{id}")
    public ResponseEntity<FactureAchat> updateFactureAchat(
            @PathVariable Long id,
            @RequestBody FactureAchat updatedFacture) {
        try {
            FactureAchat factureUpdated = factureService.updateFactureAchat(id, updatedFacture);
            return ResponseEntity.ok(factureUpdated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @DeleteMapping("deleteFactureAchat/{id}")
    public ResponseEntity<?> deleteFactureAchat(@PathVariable Long id) {
        try {
            factureService.deleteFactureAchat(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }


    /**
     * Importer les champs d'une facture d'achat depuis un fichier PDF en
     * exécutant l'OCR sur les zones précédemment enregistrées pour
     * {@code typeDeDocument = "FACTURE_ACHAT"} via le sélecteur de zones.
     * <p>Renvoie un payload pré-rempli (référence, montants, date, ligne…)
     * que le formulaire d'ajout peut directement appliquer.</p>
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @PostMapping(value = "/import-pdf", consumes = "multipart/form-data")
    public ResponseEntity<?> importFromPdf(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Fichier PDF requis"));
        }
        try {
            FactureOcrService.ExtractionResult result =
                    factureOcrService.extractFromPdf(file, FactureOcrService.TYPE_FACTURE_ACHAT);
            return ResponseEntity.ok(factureOcrService.toFactureAchatPayload(result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Échec de l'extraction OCR : " + e.getMessage()));
        }
    }

    /**
     * Importer + sauvegarder en une seule étape : exécute l'OCR sur les zones
     * enregistrées pour {@code FACTURE_ACHAT}, téléverse le PDF sur Cloudinary
     * et persiste la {@link FactureAchat} avec les champs extraits.
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @PostMapping(value = "/save-from-pdf", consumes = "multipart/form-data")
    public ResponseEntity<?> saveFromPdf(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Fichier PDF requis"));
        }
        try {
            FactureOcrService.ExtractionResult result =
                    factureOcrService.extractFromPdf(file, FactureOcrService.TYPE_FACTURE_ACHAT);
            Map<String, Object> payload = factureOcrService.toFactureAchatPayload(result);

            FactureAchat fa = new FactureAchat();
            fa.setReferance(asString(payload.get("referance")));
            fa.setDateAchat(asString(payload.get("dateAchat")));
            fa.setMontantHT(toDouble(payload.get("montantHT")));
            fa.setMontantTVA(toDouble(payload.get("montantTVA")));
            fa.setMontantTTC(toDouble(payload.get("montantTTC")));
            fa.setMontantTotal(toDouble(payload.get("montantTotal")));
            fa.setRemise(toDouble(payload.get("remise")));
            fa.setModePaiement(asString(payload.get("modePaiement")));
            fa.setStatut(asString(payload.get("statut")));
            fa.setDescription(asString(payload.get("description")));

            // Téléversement Cloudinary du PDF source.
            try {
                String url = cloudinaryService.uploadFile(file);
                fa.setCheminFichier(url);
            } catch (RuntimeException uploadEx) {
                // L'upload Cloudinary peut échouer (clé absente) — on persiste tout
                // de même la facture ; on remonte juste l'avertissement.
                payload.put("uploadWarning", uploadEx.getMessage());
            }

            // Rattachement à l'entreprise du contexte de sécurité.
            Long entrepriseId = enterpriseSecurityUtil.getContextEnterpriseId();
            if (entrepriseId != null) {
                Entreprise entreprise = entrepriseRepo.findById(entrepriseId).orElse(null);
                if (entreprise != null) fa.setEntreprise(entreprise);
            }

            FactureAchat saved = factureAchatRepo.save(fa);
            payload.put("id", saved.getId());
            payload.put("cheminFichier", saved.getCheminFichier());
            return ResponseEntity.ok(payload);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Échec de l'import + sauvegarde : " + e.getMessage()));
        }
    }

    private static String asString(Object o) {
        if (o == null) return null;
        String s = o.toString();
        return s.isEmpty() ? null : s;
    }

    private static Double toDouble(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).doubleValue();
        if (o instanceof BigDecimal) return ((BigDecimal) o).doubleValue();
        try { return Double.parseDouble(o.toString()); } catch (NumberFormatException e) { return null; }
    }


}
