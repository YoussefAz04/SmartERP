package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.entity.ArticleVente;
import BuissnesManager.BuissnesManager.service.ArticleVenteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/articles-vente")
@CrossOrigin(origins = "http://localhost:4200")
public class ArticleVenteController {

    @Autowired
    private ArticleVenteService articleVenteService;

    @GetMapping
    public ResponseEntity<List<ArticleVente>> getAllArticlesVente() {
        try {
            return ResponseEntity.ok(articleVenteService.getAllArticlesVente());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticleVente> getArticleVenteById(@PathVariable Long id) {
        try {
            return articleVenteService.getArticleVenteById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<ArticleVente>> getArticleVenteByClient(@PathVariable Long clientId) {
        try {
            return ResponseEntity.ok(articleVenteService.getArticleVenteByClient(clientId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/add")
    public ResponseEntity<ArticleVente> addArticleVente(
            @RequestBody ArticleVente articleVente,
            @RequestParam(required = false) Long articleAchatId,
            @RequestParam(required = false) Long clientId) {
        try {
            ArticleVente created = articleVenteService.addArticleVente(articleVente, articleAchatId, clientId);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ArticleVente> updateArticleVente(@PathVariable Long id,
                                                            @RequestBody ArticleVente articleVente) {
        try {
            ArticleVente updated = articleVenteService.updateArticleVente(id, articleVente);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArticleVente(@PathVariable Long id) {
        try {
            articleVenteService.deleteArticleVente(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
