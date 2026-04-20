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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsController.class);

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
            try {
                return jwtUtil.getEntrepriseIdFromToken(authHeader.substring(7));
            } catch (Exception e) {
                // Invalid/expired token — treat as anonymous so the UNAUTHORIZED
                // branch in each endpoint returns a 401 instead of leaking a 500.
                log.warn("Failed to extract entrepriseId from token: {}", e.toString());
                return null;
            }
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
            log.error("AI dashboard failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors du chargement du tableau de bord IA.");
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
            log.error("Sales analysis failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'analyse des ventes.");
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
            log.error("Forecast generation failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la génération des prévisions.");
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
            log.error("Product intelligence failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'analyse des produits.");
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
            log.error("Recommendations generation failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la génération des recommandations.");
        }
    }
}
