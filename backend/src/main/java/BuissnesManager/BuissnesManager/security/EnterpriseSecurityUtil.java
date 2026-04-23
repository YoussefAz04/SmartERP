package BuissnesManager.BuissnesManager.security;

import BuissnesManager.BuissnesManager.entity.User;
import BuissnesManager.BuissnesManager.repository.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class EnterpriseSecurityUtil {

    private static final Logger logger = LoggerFactory.getLogger(EnterpriseSecurityUtil.class);

    @Autowired
    private UserRepo userRepo;

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

    /**
     * Returns the {@link User} entity for the currently authenticated user,
     * as determined by the Spring Security context username.
     *
     * @return an Optional containing the User, or empty if no authenticated user
     */
    public Optional<User> getCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                return Optional.empty();
            }
            String username = auth.getName();
            return userRepo.findByUsername(username);
        } catch (Exception e) {
            logger.error("❌ Error resolving current user from security context", e);
            return Optional.empty();
        }
    }
}