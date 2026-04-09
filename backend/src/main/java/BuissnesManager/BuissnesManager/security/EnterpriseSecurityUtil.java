package BuissnesManager.BuissnesManager.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class EnterpriseSecurityUtil {

    @Autowired
    private JWTUtil jwtUtil;

    /**
     * Extracts the enterprise ID from the JWT token in the current HTTP request.
     *
     * @return the enterprise ID, or null if not present or token is missing
     */
    public Long getContextEnterpriseId() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                return null;
            }
            HttpServletRequest request = attrs.getRequest();
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return null;
            }
            String token = authHeader.substring(7);
            return jwtUtil.getEntrepriseIdFromToken(token);
        } catch (Exception e) {
            return null;
        }
    }
}
