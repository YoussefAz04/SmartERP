package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.entity.Membre;
import BuissnesManager.BuissnesManager.entity.MembreDto;
import BuissnesManager.BuissnesManager.entity.User;
import BuissnesManager.BuissnesManager.service.MembreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("membre")
public class MembreController {

    @Autowired
    private MembreService membreService;

    @PostMapping("/addMembre/{idEquipe}")
    public ResponseEntity<?> addMembre(@RequestBody Membre membre, @PathVariable Long idEquipe) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(membreService.addMembre(membre, idEquipe));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(e.getMessage());
        }
    }

    @PostMapping("/affecterUserToMembre/{idMembre}")
    public ResponseEntity<Void> affecterUserToMembre(@RequestBody List<User> users, @PathVariable Long idMembre) {
        try {
            membreService.affecterUserToMembre(users, idMembre);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    @GetMapping("getAllMembre")
    public ResponseEntity<?> getAllMembre(){
        return ResponseEntity.ok(membreService.getAllMembre());
    }


    @GetMapping("getMembreById/{idMembre}")
    public ResponseEntity<?> getMembreDtoById(@PathVariable Long idMembre) {
        return ResponseEntity.ok(membreService.getMembreDtoById(idMembre));
    }


    @PutMapping("/update")
    public ResponseEntity<String> updateMembre(@RequestBody MembreDto membreDto) {
            membreService.updateMembre(membreDto);
            return ResponseEntity.ok("Membre mis à jour avec succès");
    }

    @GetMapping("getAllMembreNotAffected")
    public ResponseEntity<?> getAllMembreNotAffected(){
        return ResponseEntity.ok(membreService.getAllMembreNotAffected());
    }

    @PostMapping("/affecter-a-equipe/{idEquipe}")
    public ResponseEntity<String> affecterMembresAEquipe(@RequestBody List<Membre> membres, @PathVariable Long idEquipe) {
        try {
            membreService.affectedMembreToEquipe(membres, idEquipe);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'affectation des membres : " + e.getMessage());
        }
    }

}
