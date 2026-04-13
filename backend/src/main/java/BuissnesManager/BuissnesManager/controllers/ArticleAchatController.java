package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.dto.ArticleAchatResponseDto;
import BuissnesManager.BuissnesManager.entity.Article;
import BuissnesManager.BuissnesManager.entity.ArticleAchat;
import BuissnesManager.BuissnesManager.service.ArticleAchatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/articles-achat")
@CrossOrigin(origins = "http://localhost:4200")
public class ArticleAchatController {

    @Autowired
    private ArticleAchatService articleAchatService;

    @GetMapping
    public ResponseEntity<List<ArticleAchatResponseDto>> getAllArticlesAchat() {
        try {
            return ResponseEntity.ok(articleAchatService.getAllArticlesAchat());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticleAchatResponseDto> getArticleAchatById(@PathVariable Long id) {
        try {
            return articleAchatService.getArticleAchatById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/fournisseur/{fournisseurId}")
    public ResponseEntity<List<ArticleAchatResponseDto>> getArticleAchatByFournisseur(@PathVariable Long fournisseurId) {
        try {
            return ResponseEntity.ok(articleAchatService.getArticleAchatByFournisseur(fournisseurId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/categorie/{categorieId}")
    public ResponseEntity<List<ArticleAchatResponseDto>> getArticleAchatByCategorie(@PathVariable Long categorieId) {
        try {
            return ResponseEntity.ok(articleAchatService.getArticleAchatByCategorie(categorieId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    @PostMapping("/add")
    public ResponseEntity<?> addArticleAchat(@RequestBody ArticleAchat articleAchat) {
        try {
            Article created = articleAchatService.addArticleAchat(articleAchat);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur interne lors de la création de l'article");
        }
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Article> updateArticleAchat(@PathVariable Long id,
                                                      @RequestBody ArticleAchat articleAchat) {
        try {
            Article updated = articleAchatService.updateArticleAchat(id, articleAchat);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteArticleAchat(@PathVariable Long id) {
        try {
            articleAchatService.deleteArticleAchat(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().<String>build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}