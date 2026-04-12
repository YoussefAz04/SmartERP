package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.entity.ArticleVente;
import BuissnesManager.BuissnesManager.service.ArticleVenteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    @PostMapping("/add")
    public ResponseEntity<?> addArticleVente(
            @RequestBody ArticleVente articleVente,
            @RequestParam(required = false) Long articleAchatId,
            @RequestParam(required = false) Long clientId) {
        try {
            if (articleAchatId == null) {
                return ResponseEntity.badRequest().body("articleAchatId est requis. Veuillez sélectionner un article achat valide.");
            }
            ArticleVente created = articleVenteService.addArticleVente(articleVente, articleAchatId, clientId);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body("Contrainte d'intégrité violée: l'article avec id=" + articleAchatId + " n'existe pas ou la référence est invalide.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la création de l'article vente");
        }
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
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

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
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
