package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.entity.AuditLog;
import BuissnesManager.BuissnesManager.security.AuthorizationUtil;
import BuissnesManager.BuissnesManager.security.EnterpriseSecurityUtil;
import BuissnesManager.BuissnesManager.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private AuthorizationUtil authorizationUtil;

    @Autowired
    private EnterpriseSecurityUtil enterpriseSecurityUtil;

    /**
     * Returns audit logs visible to the current user.
     * SUPER_ADMIN: all logs.
     * ADMIN: only logs for their enterprise.
     */
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<?> getAuditLogs() {
        try {
            if (authorizationUtil.isSuperAdmin()) {
                List<AuditLog> logs = auditLogService.getAllLogs();
                return ResponseEntity.ok(logs);
            }
            Long entrepriseId = enterpriseSecurityUtil.getContextEnterpriseId();
            if (entrepriseId == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Enterprise context not found");
            }
            List<AuditLog> logs = auditLogService.getLogsByEntreprise(entrepriseId);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * Returns audit logs for a specific enterprise.
     * SUPER_ADMIN can access any enterprise; ADMIN can only access their own.
     */
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    @GetMapping("/entreprise/{entrepriseId}")
    public ResponseEntity<?> getAuditLogsByEntreprise(@PathVariable Long entrepriseId) {
        try {
            if (!authorizationUtil.canAccessEnterprise(entrepriseId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
            }
            List<AuditLog> logs = auditLogService.getLogsByEntreprise(entrepriseId);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
