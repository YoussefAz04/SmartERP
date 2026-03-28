package BuissnesManager.BuissnesManager.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Component
public class JWTFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JWTFilter.class);

    @Autowired(required = false)
    private JWTUtil jwtUtil;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        
        // ✅ List of public paths that don't need JWT validation
        boolean isPublic = path.startsWith("/test") ||
                path.startsWith("/auth") ||
                path.startsWith("/entreprise") ||  // ✅ This should match /entreprise/create
                path.startsWith("/swagger-ui") ||
                path.startsWith("/swagger-resources") ||
                path.startsWith("/webjars") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/api") ||
                path.startsWith("/module") ||
                path.startsWith("/departement") ||
                path.startsWith("/permission") ||
                path.startsWith("/clients") ||
                path.startsWith("/document") ||
                path.startsWith("/facture");

        if (isPublic) {
            logger.info("✅ [JWTFilter] SKIPPING JWT validation for PUBLIC path: " + path);
            return true;  // Skip this filter
        }

        logger.info("🔒 [JWTFilter] VALIDATING JWT for PROTECTED path: " + path);
        return false;  // Apply filter
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        logger.info("🔍 [JWTFilter] Processing protected endpoint: " + path);

        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            logger.error("❌ [JWTFilter] Error: " + e.getMessage());
            throw e;
        }
    }
}