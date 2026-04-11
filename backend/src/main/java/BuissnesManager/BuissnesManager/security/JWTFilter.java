package BuissnesManager.BuissnesManager.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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

    @Autowired(required = false)
    private JWTUtil jwtUtil;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        
        // ✅ Only truly public paths skip JWT validation
        boolean isPublic = path.startsWith("/test") ||
                path.startsWith("/auth") ||
                path.startsWith("/entreprise") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/swagger-resources") ||
                path.startsWith("/webjars") ||
                path.startsWith("/v3/api-docs");

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

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            try {
                String username = jwtUtil.extractUsername(token);
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    if (jwtUtil.validateToken(token, username)) {
                        List<String> roles = jwtUtil.extractRoles(token);
                        List<SimpleGrantedAuthority> authorities = roles.stream()
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList());
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(username, null, authorities);
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        logger.info("✅ [JWTFilter] Authentication set for user: " + username);
                    } else {
                        logger.warn("⚠️ [JWTFilter] Invalid token for path: " + path);
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
                        return;
                    }
                }
            } catch (JWTVerificationException e) {
                logger.error("❌ [JWTFilter] JWT verification failed: " + e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT verification failed");
                return;
            }
        } else {
            logger.warn("⚠️ [JWTFilter] No Bearer token found for protected path: " + path);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
            return;
        }

        filterChain.doFilter(request, response);
    }
}