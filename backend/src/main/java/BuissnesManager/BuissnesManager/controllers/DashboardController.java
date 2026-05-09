package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.dto.DashboardHomeSummaryDto;
import BuissnesManager.BuissnesManager.dto.DashboardStatsDto;
import BuissnesManager.BuissnesManager.security.JWTUtil;
import BuissnesManager.BuissnesManager.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private JWTUtil jwtUtil;

    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long entrepriseId = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                entrepriseId = jwtUtil.getEntrepriseIdFromToken(token);
            }
            if (entrepriseId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Unable to determine enterprise from token");
            }
            DashboardStatsDto stats = dashboardService.getDashboardStats(entrepriseId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching dashboard stats. Please try again later.");
        }
    }

    /**
     * Lightweight summary for the home dashboard landing page: 4 counts +
     * 5 most-recent rows of clients / fournisseurs / stocks / commandes.
     * Keeps the dashboard snappy by replacing five full-list "getAll" calls
     * with a single cached aggregate query.
     */
    @GetMapping("/home-summary")
    public ResponseEntity<?> getHomeSummary(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long entrepriseId = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                entrepriseId = jwtUtil.getEntrepriseIdFromToken(token);
            }
            if (entrepriseId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Unable to determine enterprise from token");
            }
            DashboardHomeSummaryDto summary = dashboardService.getHomeSummary(entrepriseId);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching dashboard home summary. Please try again later.");
        }
    }
}
