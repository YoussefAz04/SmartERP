package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.entity.FactureAchat;
import BuissnesManager.BuissnesManager.repository.FactureAchatRepo;
import BuissnesManager.BuissnesManager.service.FactureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for purchase invoices (FactureAchat) under the {@code /facture} prefix.
 * <p>This is the canonical entry point for all purchase invoice CRUD operations.
 * Sales invoices (FactureDeVente) live in
 * {@link FactureDeVenteController} under {@code /factures-vente}.</p>
 */
@RestController
@RequestMapping("facture")
public class FactureController {

    @Autowired
    private FactureService factureService ;

    @Autowired
    private FactureAchatRepo factureAchatRepo;


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

}
