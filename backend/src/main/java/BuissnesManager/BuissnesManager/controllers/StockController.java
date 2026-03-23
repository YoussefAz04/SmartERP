package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.dto.StockDto;
import BuissnesManager.BuissnesManager.entity.Stock;
import BuissnesManager.BuissnesManager.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
public class StockController {

    @Autowired
    private StockService stockService;

    @GetMapping
    public ResponseEntity<List<Stock>> getAllStocks() {
        try {
            return ResponseEntity.ok(stockService.getAllStocks());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Stock> getStockById(@PathVariable Long id) {
        try {
            return stockService.getStockById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/entreprise/{entrepriseId}")
    public ResponseEntity<List<Stock>> getStocksByEntreprise(@PathVariable Long entrepriseId) {
        try {
            return ResponseEntity.ok(stockService.getStocksByEntreprise(entrepriseId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/low-stock/{entrepriseId}")
    public ResponseEntity<List<Stock>> getLowStockAlerts(@PathVariable Long entrepriseId) {
        try {
            return ResponseEntity.ok(stockService.getLowStockAlerts(entrepriseId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<Stock> createStock(@RequestBody StockDto stockDto) {
        try {
            Stock stock = stockService.createStock(stockDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(stock);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Stock> updateStock(@PathVariable Long id, @RequestBody StockDto stockDto) {
        try {
            Stock stock = stockService.updateStock(id, stockDto);
            return ResponseEntity.ok(stock);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStock(@PathVariable Long id) {
        try {
            stockService.deleteStock(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
