package BuissnesManager.BuissnesManager.security;

import BuissnesManager.BuissnesManager.entity.Role;
import BuissnesManager.BuissnesManager.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JWTUtil {

    private static final Logger logger = LoggerFactory.getLogger(JWTUtil.class);

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.expiration:86400000}")  // 24 hours in milliseconds
    private long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Generate a minimal JWT token: {sub, enterpriseId, roles, exp}.
     * Sensitive fields (email, username copy, id) are intentionally excluded
     * to limit exposure if the token is ever decoded by a third party.
     */
    public String generateToken(User user) {
        logger.info("🔐 Generating JWT token for user: {}", user.getUsername());

        Map<String, Object> claims = new HashMap<>();

        // Minimal payload: only enterpriseId and roles beyond the standard sub/exp
        if (user.getEntreprise() != null) {
            claims.put("enterpriseId", user.getEntreprise().getId());
            logger.info("   ✅ Enterprise added to JWT: {} (ID: {})",
                user.getEntreprise().getNom(), user.getEntreprise().getId());
        } else {
            logger.warn("   ⚠️ WARNING: User has NO enterprise assigned!");
        }

        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            List<String> roleNames = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());
            claims.put("roles", roleNames);
            logger.info("   ✅ Roles added to JWT: {}", roleNames);
        }

        long now = System.currentTimeMillis();
        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();

        logger.info("✅ JWT token generated successfully (expires in {} ms)", expiration);
        return token;
    }

    /**
     * Validates JWT token and extracts claims
     */
    public Claims extractClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            logger.error("❌ Invalid JWT token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get username from token
     */
    public String getUsernameFromToken(String token) {
        Claims claims = extractClaims(token);
        return claims != null ? claims.getSubject() : null;
    }

    /**
     * Get enterprise ID from token
     */
    public Long getEnterpriseIdFromToken(String token) {
        Claims claims = extractClaims(token);
        if (claims != null && claims.containsKey("enterpriseId")) {
            Object id = claims.get("enterpriseId");
            if (id instanceof Long) {
                return (Long) id;
            } else if (id instanceof Integer) {
                return ((Integer) id).longValue();
            }
        }
        return null;
    }

    /**
     * Extract roles from token
     */
    public List<String> extractRoles(String token) {
        Claims claims = extractClaims(token);
        if (claims != null && claims.containsKey("roles")) {
            Object rolesObj = claims.get("roles");
            if (rolesObj instanceof List) {
                return (List<String>) rolesObj;
            }
        }
        return new ArrayList<>();
    }

    /**
     * Extract username from token (alias for getUsernameFromToken)
     */
    public String extractUsername(String token) {
        return getUsernameFromToken(token);
    }

    /**
     * Validate token against a given username
     */
    public boolean validateToken(String token, String username) {
        String tokenUsername = getUsernameFromToken(token);
        return tokenUsername != null && tokenUsername.equals(username) && !isTokenExpired(token);
    }

    /**
     * Get enterprise ID from token (French spelling alias)
     */
    public Long getEntrepriseIdFromToken(String token) {
        return getEnterpriseIdFromToken(token);
    }

    /**
     * Generate a minimal JWT token for a user with an overridden enterprise context.
     * Used exclusively by the super-admin enterprise-switch endpoint.
     */
    public String generateTokenWithEnterprise(User user, Long enterpriseId, String enterpriseName) {
        Map<String, Object> claims = new HashMap<>();

        claims.put("enterpriseId", enterpriseId);

        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            List<String> roleNames = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());
            claims.put("roles", roleNames);
        }

        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Check if token is valid
     */
    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            logger.warn("⚠️ Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        Claims claims = extractClaims(token);
        return claims != null && claims.getExpiration().before(new Date());
    }
}
