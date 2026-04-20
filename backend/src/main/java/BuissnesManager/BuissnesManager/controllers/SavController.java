package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.dto.SavDto;
import BuissnesManager.BuissnesManager.dto.SavInterventionDto;
import BuissnesManager.BuissnesManager.entity.Sav;
import BuissnesManager.BuissnesManager.entity.SavIntervention;
import BuissnesManager.BuissnesManager.service.SavService;
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

@RestController
@RequestMapping("/api/sav")
@CrossOrigin(origins = "http://localhost:4200")
public class SavController {

    private static final Logger logger = LoggerFactory.getLogger(SavController.class);

    @Autowired
    private SavService savService;

    // ---------- Tickets ----------

    @GetMapping
    public ResponseEntity<?> getAll() {
        try {
            return ResponseEntity.ok(savService.getAll());
        } catch (Exception e) {
            logger.error("Error listing SAV tickets", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error(e.getMessage()));
        }
    }

    @GetMapping("/entreprise/{entrepriseId}")
    public ResponseEntity<?> getByEntreprise(@PathVariable Long entrepriseId) {
        try {
            return ResponseEntity.ok(savService.getByEntreprise(entrepriseId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error(e.getMessage()));
        }
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<?> getByClient(@PathVariable Long clientId) {
        try {
            return ResponseEntity.ok(savService.getByClient(clientId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            Sav sav = savService.getById(id);
            return ResponseEntity.ok(sav);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @PostMapping("/add")
    public ResponseEntity<?> create(@RequestBody SavDto dto) {
        try {
            Sav sav = savService.create(dto);
            return ResponseEntity.ok(sav);
        } catch (RuntimeException e) {
            logger.error("Error creating SAV", e);
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error creating SAV", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error(e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody SavDto dto) {
        try {
            return ResponseEntity.ok(savService.update(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            savService.delete(id);
            Map<String, Object> body = new HashMap<>();
            body.put("message", "Ticket SAV supprimé");
            body.put("status", "success");
            body.put("id", id);
            return ResponseEntity.ok(body);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    // ---------- Workflow ----------

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @PutMapping("/{id}/statut")
    public ResponseEntity<?> changeStatut(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String statut = body.get("statut");
            String commentaire = body.get("commentaire");
            return ResponseEntity.ok(savService.changeStatut(id, statut, commentaire));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    // ---------- Interventions (timeline) ----------

    @GetMapping("/{id}/interventions")
    public ResponseEntity<?> getInterventions(@PathVariable Long id) {
        try {
            List<SavIntervention> list = savService.getInterventions(id);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error(e.getMessage()));
        }
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @PostMapping("/{id}/interventions")
    public ResponseEntity<?> addIntervention(@PathVariable Long id, @RequestBody SavInterventionDto dto) {
        try {
            SavIntervention evt = savService.addIntervention(id, dto);
            return ResponseEntity.ok(evt);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    // ---------- Stats ----------

    @GetMapping("/stats")
    public ResponseEntity<?> stats(@RequestParam(required = false) Long entrepriseId) {
        try {
            return ResponseEntity.ok(savService.stats(entrepriseId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error(e.getMessage()));
        }
    }

    // ---------- Helpers ----------

    private Map<String, Object> error(String msg) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", msg);
        body.put("timestamp", System.currentTimeMillis());
        return body;
    }
}
