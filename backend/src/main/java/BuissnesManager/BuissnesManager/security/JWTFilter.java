package BuissnesManager.BuissnesManager.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JWTFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JWTFilter.class);

    // ThreadLocal to store enterprise ID for the duration of the current request
    private static final ThreadLocal<Long> enterpriseIdThreadLocal = new ThreadLocal<>();

    /**
     * Returns the enterprise ID associated with the current request, as extracted
     * from the JWT token during filter processing. Returns {@code null} if the
     * current request has no enterprise context (e.g. public endpoints).
     */
    public static Long getCurrentEnterpriseId() {
        return enterpriseIdThreadLocal.get();
    }

    @Autowired(required = false)
    private JWTUtil jwtUtil;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        
        // Truly public paths that never need JWT processing
        boolean isPublic = path.startsWith("/test") ||
                path.startsWith("/auth") ||
                path.startsWith("/api/auth") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/swagger-resources") ||
                path.startsWith("/webjars") ||
                path.startsWith("/v3/api-docs");

        if (isPublic) {
            logger.debug("✅ [JWTFilter] SKIPPING JWT validation for PUBLIC path: {}", path);
            return true;
        }

        logger.debug("🔒 [JWTFilter] VALIDATING JWT for path: {}", path);
        return false;
    }

    /**
     * Extracts the JWT from the {@code access_token} httpOnly cookie first;
     * falls back to the {@code Authorization: Bearer ...} header for
     * backwards-compatibility during the migration window.
     */
    private String extractToken(HttpServletRequest request) {
        // 1. Prefer httpOnly cookie
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("access_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        // 2. Fallback: Authorization header
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        logger.info("🔍 [JWTFilter] Processing protected endpoint: {}", path);

        try {
            String token = extractToken(request);

            if (token != null) {
                String username = jwtUtil.extractUsername(token);
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    if (jwtUtil.validateToken(token, username)) {
                        List<String> roles = jwtUtil.extractRoles(token);
                        
                        Long enterpriseId = jwtUtil.getEnterpriseIdFromToken(token);
                        logger.info("✅ [JWTFilter] Enterprise ID from token: {}", enterpriseId);
                        
                        if (enterpriseId != null) {
                            enterpriseIdThreadLocal.set(enterpriseId);
                            request.setAttribute("enterpriseId", enterpriseId);
                            logger.info("✅ [JWTFilter] Set ThreadLocal enterprise ID: {}", enterpriseId);
                        } else {
                            logger.warn("⚠️ [JWTFilter] No enterprise ID found in token for user: {}", username);
                        }
                        
                        List<SimpleGrantedAuthority> authorities = roles.stream()
                                .map(r -> {
                                    String upper = r.toUpperCase();
                                    return new SimpleGrantedAuthority(upper.startsWith("ROLE_") ? upper : "ROLE_" + upper);
                                })
                                .collect(Collectors.toList());
                        
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(username, null, authorities);
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        logger.info("✅ [JWTFilter] Authentication set for user: {} with roles: {}", username, roles);
                    } else {
                        logger.warn("⚠️ [JWTFilter] Invalid token for path: {}", path);
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
                        return;
                    }
                }
            } else {
                // No token provided – continue without authentication.
                // URL-level security (permitAll / authenticated) will decide access.
                logger.debug("⚠️ [JWTFilter] No token for path: {} – proceeding unauthenticated", path);
            }

            filterChain.doFilter(request, response);
        } catch (JWTVerificationException e) {
            logger.error("❌ [JWTFilter] JWT verification failed: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT verification failed");
        } catch (Exception e) {
            logger.error("❌ [JWTFilter] Unexpected error: {}", e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Authentication error");
        } finally {
            // Always clear ThreadLocal to prevent memory leaks in thread pools
            enterpriseIdThreadLocal.remove();
        }
    }
}
