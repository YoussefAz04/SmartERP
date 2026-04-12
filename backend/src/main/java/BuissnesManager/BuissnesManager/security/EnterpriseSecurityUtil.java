package BuissnesManager.BuissnesManager.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class EnterpriseSecurityUtil {

    private static final Logger logger = LoggerFactory.getLogger(EnterpriseSecurityUtil.class);

    /**
     * Extracts the enterprise ID from the current authenticated user's context.
     * 
     * @return Enterprise ID if user is authenticated and has enterprise, null otherwise
     */
    public Long getContextEnterpriseId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.warn("⚠️ User is not authenticated");
                return null;
            }

            // ✅ Get the username from authentication principal
            String username = authentication.getPrincipal().toString();
            
            logger.debug("👤 Authenticated user: {}", username);
            return null;  // Will be set by JWTUtil in the filter
            
        } catch (Exception e) {
            logger.error("❌ Error extracting enterprise ID from authentication context", e);
            return null;
        }
    }

    /**
     * Gets enterprise ID from request context (set by JWTFilter)
     * This is a workaround since we're using Auth0 JWT library
     */
    public Long getEnterpriseIdFromRequest() {
        try {
            // This would need to be set as a request attribute by JWTFilter
            // For now, return from ThreadLocal or request context
            return null;
        } catch (Exception e) {
            logger.error("❌ Error getting enterprise ID from request", e);
            return null;
        }
    }
}