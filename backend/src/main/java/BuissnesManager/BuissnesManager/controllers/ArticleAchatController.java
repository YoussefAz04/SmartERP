package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.entity.ArticleAchat;
import BuissnesManager.BuissnesManager.service.ArticleAchatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/articles-achat")
@CrossOrigin(origins = "http://localhost:4200")
public class ArticleAchatController {

    @Autowired
    private ArticleAchatService articleAchatService;

    @GetMapping
    public ResponseEntity<List<ArticleAchat>> getAllArticlesAchat() {
        try {
            return ResponseEntity.ok(articleAchatService.getAllArticlesAchat());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticleAchat> getArticleAchatById(@PathVariable Long id) {
        try {
            return articleAchatService.getArticleAchatById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/add")
    public ResponseEntity<ArticleAchat> addArticleAchat(@RequestBody ArticleAchat articleAchat) {
        try {
            ArticleAchat created = articleAchatService.addArticleAchat(articleAchat);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ArticleAchat> updateArticleAchat(@PathVariable Long id,
                                                            @RequestBody ArticleAchat articleAchat) {
        try {
            ArticleAchat updated = articleAchatService.updateArticleAchat(id, articleAchat);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArticleAchat(@PathVariable Long id) {
        try {
            articleAchatService.deleteArticleAchat(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}