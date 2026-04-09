package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.dto.DashboardStatsDto;
import BuissnesManager.BuissnesManager.security.JWTUtil;
import BuissnesManager.BuissnesManager.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
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
                    .body("Error fetching dashboard stats: " + e.getMessage());
        }
    }
}
