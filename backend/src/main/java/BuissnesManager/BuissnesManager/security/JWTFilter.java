package BuissnesManager.BuissnesManager.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@Order(1)  // Process AFTER SecurityFilterChain
public class JWTFilter extends OncePerRequestFilter {

    @Autowired
    private JWTUtil jwtUtil;

    private static final String[] PUBLIC_PATHS = {
            "/auth/",
            "/entreprise/",
            "/swagger",
            "/webjars/",
            "/api/",
            "/module/",
            "/v3/api-docs",
            "/departement/",
            "/permission/",
            "/clients/",
            "/document/",
            "/facture/"
    };

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        for (String publicPath : PUBLIC_PATHS) {
            if (path.startsWith(publicPath)) {
                System.out.println("✅ Skipping JWT validation for public path: " + path);
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.startsWith("/auth/") ||
               path.startsWith("/entreprise/") ||
               path.startsWith("/swagger") ||
               path.startsWith("/webjars/") ||
               path.startsWith("/api/") ||
               path.startsWith("/module/") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/departement/") ||
               path.startsWith("/permission/") ||
               path.startsWith("/clients/") ||
               path.startsWith("/document/") ||
               path.startsWith("/facture/") ||
               path.startsWith("/test/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        System.out.println("🔒 JWTFilter - validating protected endpoint: " + path);

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("⚠️ No Authorization header for: " + path);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = authHeader.substring(7);
            String username = jwtUtil.extractUsername(jwt);

            if (username != null && jwtUtil.validateToken(jwt, username)) {
                List<String> roles = jwtUtil.extractRoles(jwt);
                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(role -> "ROLE_" + role)
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println("✅ JWT validated for: " + username);
            }
        } catch (JWTVerificationException e) {
            System.out.println("❌ JWT error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Filter error: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}