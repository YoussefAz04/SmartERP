package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.entity.CategorieFournisseur;
import BuissnesManager.BuissnesManager.entity.Fournisseur;
import BuissnesManager.BuissnesManager.security.JWTUtil;
import BuissnesManager.BuissnesManager.service.FournisseurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/fournisseurs")
public class FournisseurController {

    @Autowired
    private FournisseurService fournisseurService;

    @Autowired
    private JWTUtil jwtUtil;

    // ✅ ADD FOURNISSEUR
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/addFournisseur/{idCategorie}")
    public ResponseEntity<Fournisseur> addFournisseur(@RequestBody Fournisseur fournisseur, @PathVariable Long idCategorie) {
        try {
            Fournisseur newFournisseur = fournisseurService.addFournisseur(fournisseur, idCategorie);
            return ResponseEntity.ok(newFournisseur);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // ✅ GET ALL FOURNISSEURS
    @GetMapping("/getAllFournisseurs")
    public ResponseEntity<List<Fournisseur>> getAllFournisseurs() {
        try {
            return ResponseEntity.ok(fournisseurService.getAllFournisseurs());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // ✅ GET FOURNISSEURS BY ENTERPRISE
    @GetMapping("/byEntreprise/{entrepriseId}")
    public ResponseEntity<List<Fournisseur>> getFournisseursByEntreprise(
            @PathVariable Long entrepriseId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                Long tokenEntrepriseId = jwtUtil.getEntrepriseIdFromToken(token);
                if (tokenEntrepriseId != null && !tokenEntrepriseId.equals(entrepriseId)) {
                    return ResponseEntity.status(403).build();
                }
            }
            return ResponseEntity.ok(fournisseurService.getFournisseursByEntreprise(entrepriseId));
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // ✅ GET FOURNISSEUR BY ID
    @GetMapping("/getFournisseurById/{id}")
    public ResponseEntity<Fournisseur> getFournisseurById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(fournisseurService.getFournisseurById(id));
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
    }

    // ✅ DELETE FOURNISSEUR
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/deleteFournisseur/{id}")
    public ResponseEntity<Map<String, Object>> deleteFournisseur(@PathVariable Long id) {
        try {
            fournisseurService.deleteFournisseur(id);
            return ResponseEntity.ok(Map.of(
                "message", "Fournisseur supprimé avec succès",
                "status", "success",
                "id", id
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "message", "Error: " + e.getMessage(),
                "status", "error"
            ));
        }
    }

    // ✅ UPDATE FOURNISSEUR
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/updateFournisseur/{id}")
    public ResponseEntity<Fournisseur> updateFournisseur(@PathVariable Long id, @RequestBody Fournisseur fournisseur) {
        try {
            Fournisseur updated = fournisseurService.updateFournisseur(id, fournisseur);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // ✅ ADD CATEGORIE FOURNISSEUR
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/addCategorieFournisseur")
    public ResponseEntity<CategorieFournisseur> addCategorieFournisseur(@RequestBody CategorieFournisseur categorie) {
        try {
            CategorieFournisseur newCategorie = fournisseurService.addCategorieFournisseur(categorie);
            return ResponseEntity.ok(newCategorie);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // ✅ GET ALL CATEGORIES FOURNISSEUR
    @GetMapping("/getAllCategorieFournisseurs")
    public ResponseEntity<List<CategorieFournisseur>> getAllCategorieFournisseurs() {
        try {
            return ResponseEntity.ok(fournisseurService.getAllCategorieFournisseurs());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // ✅ GET CATEGORIE FOURNISSEUR BY ID
    @GetMapping("/getCategorieFournisseurById/{id}")
    public ResponseEntity<CategorieFournisseur> getCategorieFournisseurById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(fournisseurService.getCategorieFournisseurById(id));
        } catch (Exception e) {
            return ResponseEntity.status(404).build();
        }
    }

    // ✅ DELETE CATEGORIE FOURNISSEUR
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/deleteCategorieFournisseur/{id}")
    public ResponseEntity<Map<String, Object>> deleteCategorieFournisseur(@PathVariable Long id) {
        try {
            fournisseurService.deleteCategorieFournisseur(id);
            return ResponseEntity.ok(Map.of(
                "message", "Category supprimée avec succès",
                "status", "success",
                "id", id
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "message", "Error: " + e.getMessage(),
                "status", "error"
            ));
        }
    }

    // ✅ UPDATE CATEGORIE FOURNISSEUR
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/updateCategorieFournisseur/{id}")
    public ResponseEntity<CategorieFournisseur> updateCategorieFournisseur(@PathVariable Long id, @RequestBody CategorieFournisseur categorie) {
        try {
            CategorieFournisseur updated = fournisseurService.updateCategorieFournisseur(id, categorie);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
