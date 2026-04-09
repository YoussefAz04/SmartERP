package BuissnesManager.BuissnesManager.security;

import BuissnesManager.BuissnesManager.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class EnterpriseSecurityValidator {

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private UserRepo userRepo;

    /**
     * Extracts the enterprise ID from a Bearer JWT token in the Authorization header.
     *
     * @param authHeader the value of the Authorization header (e.g. "Bearer eyJ...")
     * @return the enterprise ID embedded in the token, or {@code null} if not present
     * @throws RuntimeException if the header is missing or not a Bearer token
     */
    public Long extractEnterpriseIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid authorization header");
        }
        String token = authHeader.substring(7);
        return jwtUtil.getEntrepriseIdFromToken(token);
    }

    /**
     * Validates that the authenticated user (identified by the JWT token) belongs to
     * the required enterprise. Throws {@link AccessDeniedException} if the IDs do not match.
     *
     * @param authHeader           the Authorization header containing the Bearer token
     * @param requiredEnterpriseId the enterprise ID that access is being requested for
     */
    public void validateAccess(String authHeader, Long requiredEnterpriseId) {
        Long userEnterpriseId = extractEnterpriseIdFromToken(authHeader);
        if (userEnterpriseId == null || !userEnterpriseId.equals(requiredEnterpriseId)) {
            throw new AccessDeniedException(
                    "Access denied. User belongs to enterprise " + userEnterpriseId
                    + " but is trying to access enterprise " + requiredEnterpriseId);
        }
    }
}
