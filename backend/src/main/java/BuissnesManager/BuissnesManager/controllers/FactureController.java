package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.entity.FactureAchat;
import BuissnesManager.BuissnesManager.service.FactureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("facture")
public class FactureController {

    @Autowired
    private FactureService factureService ;



    @PostMapping("addFacture/{idDocument}")
    private ResponseEntity<?> ajouterFacture(@PathVariable Long idDocument) throws Exception {
        try {
            factureService.AjouterFactureAchat(idDocument) ;
            return ResponseEntity.ok().build() ;
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("error" + e.getMessage());
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




}
