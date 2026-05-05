package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.dto.AiDashboardDto;
import BuissnesManager.BuissnesManager.dto.ForecastDto;
import BuissnesManager.BuissnesManager.dto.PredictionAccuracyDto;
import BuissnesManager.BuissnesManager.dto.ProductIntelligenceDto;
import BuissnesManager.BuissnesManager.dto.RecommendationDto;
import BuissnesManager.BuissnesManager.dto.SalesAnalysisDto;
import BuissnesManager.BuissnesManager.security.JWTUtil;
import BuissnesManager.BuissnesManager.service.AiDashboardService;
import BuissnesManager.BuissnesManager.service.AnalyticService;
import BuissnesManager.BuissnesManager.service.ForecastService;
import BuissnesManager.BuissnesManager.service.PredictionLogService;
import BuissnesManager.BuissnesManager.service.RecommendationService;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
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
    private PredictionLogService predictionLogService;

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
            // Log the freshly-issued forecast so we can later compare it to
            // the real CA. Idempotent per (entreprise, predictionMonth, horizon).
            try {
                if (dashboard != null && dashboard.getForecast() != null) {
                    predictionLogService.recordForecast(entrepriseId, dashboard.getForecast());
                }
            } catch (RuntimeException logEx) {
                log.warn("Prediction logging skipped: {}", logEx.toString());
            }
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
            try {
                predictionLogService.recordForecast(entrepriseId, forecast);
            } catch (RuntimeException logEx) {
                log.warn("Prediction logging skipped: {}", logEx.toString());
            }
            return ResponseEntity.ok(forecast);
        } catch (Exception e) {
            log.error("Forecast generation failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la génération des prévisions.");
        }
    }

    /**
     * What-if mode: re-runs the forecast with calendar / promo overrides for
     * a list of named scenarios. Body shape:
     * <pre>{ "scenarios": [{ "name": "Q4 push", "deltas": { "workingDays": -2,
     * "ramadan": false, "promo": true } }] }</pre>
     *
     * <p>Currently we apply the deltas as a multiplicative perturbation on
     * the baseline forecast: working-days deltas scale linearly with the
     * default working-days base (22), Ramadan toggling scales by the
     * Ramadan elasticity (−6%), promo by +10%. The same baseline model is
     * reused — no retraining — so latency stays low.
     */
    @PostMapping("/forecast/what-if")
    public ResponseEntity<?> whatIfForecast(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody(required = false) java.util.Map<String, Object> body) {
        try {
            Long entrepriseId = extractEntrepriseId(authHeader);
            if (entrepriseId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Impossible de déterminer l'entreprise depuis le token.");
            }
            ForecastDto baseline = forecastService.generateForecast(entrepriseId);
            java.util.Map<String, Object> response = new java.util.LinkedHashMap<>();
            response.put("baseline", baseline);

            java.util.List<java.util.Map<String, Object>> scenariosOut = new java.util.ArrayList<>();
            Object scenariosObj = body == null ? null : body.get("scenarios");
            if (scenariosObj instanceof java.util.List<?>) {
                for (Object o : (java.util.List<?>) scenariosObj) {
                    if (!(o instanceof java.util.Map)) continue;
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> scenario = (java.util.Map<String, Object>) o;
                    scenariosOut.add(applyScenario(baseline, scenario));
                }
            }
            response.put("scenarios", scenariosOut);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("What-if forecast failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors du calcul du scénario.");
        }
    }

    /**
     * Apply a multiplicative perturbation to the baseline forecast points.
     * Pure post-processing — keeps the same model, same Box-Cox, same data.
     */
    private static java.util.Map<String, Object> applyScenario(
            ForecastDto baseline, java.util.Map<String, Object> scenario) {
        Object name = scenario.getOrDefault("name", "scenario");
        Object deltasObj = scenario.get("deltas");
        double mult = 1.0;
        if (deltasObj instanceof java.util.Map<?, ?> deltas) {
            mult *= 1.0 + asDouble(deltas.get("marketingSpend"), 0.0) * 0.30;
            mult *= 1.0 + asDouble(deltas.get("workingDays"), 0.0) / 22.0;
            if (Boolean.TRUE.equals(deltas.get("ramadan")))  mult *= 0.94; // -6%
            if (Boolean.FALSE.equals(deltas.get("ramadan"))) mult *= 1.06; // +6%
            if (Boolean.TRUE.equals(deltas.get("promo")))    mult *= 1.10; // +10%
        }
        java.util.List<ForecastDto.ForecastPointDto> baselinePoints = baseline.getForecastPoints();
        java.util.List<ForecastDto.ForecastPointDto> scenarioPoints = new java.util.ArrayList<>();
        if (baselinePoints != null) {
            for (ForecastDto.ForecastPointDto p : baselinePoints) {
                if (p.getActual() != null) { scenarioPoints.add(p); continue; }
                Double f  = p.getForecast()    == null ? null : Math.max(0.0, p.getForecast()    * mult);
                Double up = p.getUpperBound()  == null ? null : Math.max(0.0, p.getUpperBound()  * mult);
                Double lo = p.getLowerBound()  == null ? null : Math.max(0.0, p.getLowerBound()  * mult);
                scenarioPoints.add(new ForecastDto.ForecastPointDto(p.getPeriod(), null,
                        f == null ? null : Math.round(f * 100.0) / 100.0,
                        up == null ? null : Math.round(up * 100.0) / 100.0,
                        lo == null ? null : Math.round(lo * 100.0) / 100.0));
            }
        }
        java.util.Map<String, Object> out = new java.util.LinkedHashMap<>();
        out.put("name", name);
        out.put("multiplier", Math.round(mult * 1000.0) / 1000.0);
        out.put("forecastPoints", scenarioPoints);
        return out;
    }

    private static double asDouble(Object o, double def) {
        if (o instanceof Number) return ((Number) o).doubleValue();
        if (o instanceof String s) {
            try { return Double.parseDouble(s); } catch (NumberFormatException ignored) { /* keep default */ }
        }
        return def;
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

    @GetMapping("/predictions")
    public ResponseEntity<?> getPredictionHistory(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long entrepriseId = extractEntrepriseId(authHeader);
            if (entrepriseId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Impossible de déterminer l'entreprise depuis le token.");
            }
            PredictionAccuracyDto history = predictionLogService.getHistory(entrepriseId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Prediction history failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors du chargement de l'historique des prédictions.");
        }
    }

    @PostMapping("/predictions/reconcile")
    public ResponseEntity<?> reconcilePredictions(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long entrepriseId = extractEntrepriseId(authHeader);
            if (entrepriseId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Impossible de déterminer l'entreprise depuis le token.");
            }
            int settled = predictionLogService.reconcile(entrepriseId);
            Map<String, Object> body = new HashMap<>();
            body.put("settled", settled);
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            log.error("Prediction reconciliation failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la réconciliation des prédictions.");
        }
    }
}
