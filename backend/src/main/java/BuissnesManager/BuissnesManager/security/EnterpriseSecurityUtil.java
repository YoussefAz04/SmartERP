package BuissnesManager.BuissnesManager.security;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class EnterpriseSecurityUtil {

    private static final Logger logger = LoggerFactory.getLogger(EnterpriseSecurityUtil.class);

    /**
     * Extracts the enterprise ID from the current request context.
     * Reads from the ThreadLocal populated by {@link JWTFilter} during JWT validation.
     * The ThreadLocal is cleared by JWTFilter after each request, so there is no
     * risk of enterprise ID leaking between requests.
     *
     * @return Enterprise ID if present in the JWT token, null otherwise
     */
    public Long getContextEnterpriseId() {
        try {
            Long enterpriseId = JWTFilter.getCurrentEnterpriseId();
            if (enterpriseId != null) {
                logger.debug("✅ Enterprise ID from request context: {}", enterpriseId);
                return enterpriseId;
            }

            logger.warn("⚠️ No enterprise ID found in request context");
            return null;

        } catch (Exception e) {
            logger.error("❌ Error extracting enterprise ID from request context", e);
            return null;
        }
    }

    /**
     * Gets enterprise ID from request context (set by JWTFilter).
     * Alias for {@link #getContextEnterpriseId()}.
     */
    public Long getEnterpriseIdFromRequest() {
        return getContextEnterpriseId();
    }
}