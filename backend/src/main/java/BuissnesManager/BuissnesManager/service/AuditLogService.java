package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.entity.AuditLog;
import BuissnesManager.BuissnesManager.repository.AuditLogRepository;
import BuissnesManager.BuissnesManager.security.EnterpriseSecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

@Service
public class AuditLogService {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogService.class);

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private EnterpriseSecurityUtil enterpriseSecurityUtil;

    





    @Transactional
    public void log(String action, String details) {
        String username = null;
        Long userId = null;
        Long entrepriseId = enterpriseSecurityUtil.getContextEnterpriseId();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            username = auth.getName();
        }

        
        Long resolvedUserId = userId;
        String resolvedUsername = username;
        if (username != null) {
            var userOpt = enterpriseSecurityUtil.getCurrentUser();
            if (userOpt.isPresent()) {
                resolvedUserId = userOpt.get().getId();
            }
        }

        doLog(action, details, resolvedUsername, resolvedUserId, entrepriseId, extractClientIp());
    }

    



    @Transactional
    public void log(String action, String details, String username, Long userId, Long entrepriseId) {
        doLog(action, details, username, userId, entrepriseId, extractClientIp());
    }

    private void doLog(String action, String details, String username, Long userId, Long entrepriseId, String ipAddress) {
        try {
            AuditLog entry = new AuditLog();
            entry.setAction(action);
            entry.setDetails(details);
            entry.setUsername(username);
            entry.setUserId(userId);
            entry.setEntrepriseId(entrepriseId);
            entry.setIpAddress(ipAddress);
            auditLogRepository.save(entry);
            logger.debug("AuditLog recorded: action={}, user={}, entreprise={}", action, username, entrepriseId);
        } catch (Exception e) {
            
            logger.error("Failed to persist audit log entry: action={}, error={}", action, e.getMessage());
        }
    }

    private String extractClientIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                return null;
            }
            HttpServletRequest request = attrs.getRequest();
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                return forwarded.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        } catch (Exception e) {
            return null;
        }
    }

    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<AuditLog> getLogsByEntreprise(Long entrepriseId) {
        return auditLogRepository.findByEntrepriseIdOrderByCreatedAtDesc(entrepriseId);
    }
}
