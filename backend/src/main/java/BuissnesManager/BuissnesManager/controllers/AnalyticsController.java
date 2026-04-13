package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.dto.AiDashboardDto;
import BuissnesManager.BuissnesManager.dto.ForecastDto;
import BuissnesManager.BuissnesManager.dto.ProductIntelligenceDto;
import BuissnesManager.BuissnesManager.dto.RecommendationDto;
import BuissnesManager.BuissnesManager.dto.SalesAnalysisDto;
import BuissnesManager.BuissnesManager.security.JWTUtil;
import BuissnesManager.BuissnesManager.service.AiDashboardService;
import BuissnesManager.BuissnesManager.service.AnalyticService;
import BuissnesManager.BuissnesManager.service.ForecastService;
import BuissnesManager.BuissnesManager.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private AiDashboardService aiDashboardService;

    @Autowired
    private AnalyticService analyticService;

    @Autowired
    private ForecastService forecastService;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private JWTUtil jwtUtil;

    private Long extractEntrepriseId(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return jwtUtil.getEntrepriseIdFromToken(authHeader.substring(7));
        }
        return null;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getAiDashboard(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long entrepriseId = extractEntrepriseId(authHeader);
            if (entrepriseId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Impossible de déterminer l'entreprise depuis le token.");
            }
            AiDashboardDto dashboard = aiDashboardService.getAiDashboard(entrepriseId);
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors du chargement du tableau de bord IA: " + e.getMessage());
        }
    }

    @GetMapping("/sales")
    public ResponseEntity<?> getSalesAnalysis(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long entrepriseId = extractEntrepriseId(authHeader);
            if (entrepriseId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Impossible de déterminer l'entreprise depuis le token.");
            }
            SalesAnalysisDto analysis = analyticService.analyzeSales(entrepriseId);
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'analyse des ventes: " + e.getMessage());
        }
    }

    @GetMapping("/forecast")
    public ResponseEntity<?> getForecast(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long entrepriseId = extractEntrepriseId(authHeader);
            if (entrepriseId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Impossible de déterminer l'entreprise depuis le token.");
            }
            ForecastDto forecast = forecastService.generateForecast(entrepriseId);
            return ResponseEntity.ok(forecast);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la génération des prévisions: " + e.getMessage());
        }
    }

    @GetMapping("/products")
    public ResponseEntity<?> getProductIntelligence(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long entrepriseId = extractEntrepriseId(authHeader);
            if (entrepriseId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Impossible de déterminer l'entreprise depuis le token.");
            }
            ProductIntelligenceDto products = analyticService.analyzeProducts(entrepriseId);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'analyse des produits: " + e.getMessage());
        }
    }

    @GetMapping("/recommendations")
    public ResponseEntity<?> getRecommendations(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long entrepriseId = extractEntrepriseId(authHeader);
            if (entrepriseId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Impossible de déterminer l'entreprise depuis le token.");
            }
            List<RecommendationDto> recommendations = recommendationService.generateRecommendations(entrepriseId);
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la génération des recommandations: " + e.getMessage());
        }
    }
}
