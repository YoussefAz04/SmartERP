package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.dto.StockMouvementDto;
import BuissnesManager.BuissnesManager.entity.StockMouvement;
import BuissnesManager.BuissnesManager.entity.TypeMouvement;
import BuissnesManager.BuissnesManager.service.StockMouvementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock-mouvement")
public class StockMouvementController {

    @Autowired
    private StockMouvementService stockMouvementService;

    @GetMapping("/all")
    public ResponseEntity<List<StockMouvement>> getAllMouvements() {
        try {
            return ResponseEntity.ok(stockMouvementService.getAllMouvements());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<StockMouvement> getMouvementById(@PathVariable Long id) {
        try {
            return stockMouvementService.getMouvementById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/stock/{stockId}")
    public ResponseEntity<List<StockMouvement>> getMouvementsByStock(@PathVariable Long stockId) {
        try {
            return ResponseEntity.ok(stockMouvementService.getMouvementsByStock(stockId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<StockMouvement>> getMouvementsByType(@PathVariable TypeMouvement type) {
        try {
            return ResponseEntity.ok(stockMouvementService.getMouvementsByType(type));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/user/{utilisateurId}")
    public ResponseEntity<List<StockMouvement>> getMouvementsByUser(@PathVariable Long utilisateurId) {
        try {
            return ResponseEntity.ok(stockMouvementService.getMouvementsByUser(utilisateurId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<StockMouvement> createMouvement(@RequestBody StockMouvementDto dto) {
        try {
            StockMouvement mouvement = stockMouvementService.createMouvement(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(mouvement);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMouvement(@PathVariable Long id) {
        try {
            stockMouvementService.deleteMouvement(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
