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

    @Value("${security.jwt.secret:mySecretKeyForJWTTokenGenerationAndValidationPurposesOnly1234567890}")
    private String secret;

    @Value("${security.jwt.expiration:86400000}")  // 24 hours in milliseconds
    private long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * ✅ FIXED: Generate JWT token with all required claims including enterpriseId
     */
    public String generateToken(User user) {
        logger.info("🔐 Generating JWT token for user: {} (ID: {})", user.getUsername(), user.getId());

        Map<String, Object> claims = new HashMap<>();

        // ✅ ADD USER CLAIMS
        claims.put("id", user.getId());
        claims.put("username", user.getUsername());
        claims.put("email", user.getEmail());

        // ✅ ADD ENTERPRISE CLAIM (CRITICAL!)
        if (user.getEntreprise() != null) {
            claims.put("enterpriseId", user.getEntreprise().getId());
            claims.put("entrepriseId", user.getEntreprise().getId());  // Also add French version
            claims.put("enterpriseName", user.getEntreprise().getNom());
            logger.info("   ✅ Enterprise added to JWT: {} (ID: {})", 
                user.getEntreprise().getNom(), user.getEnterprise().getId());
        } else {
            logger.warn("   ⚠️ WARNING: User has NO enterprise assigned!");
        }

        // ✅ ADD ROLES/AUTHORITIES
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            List<String> roleNames = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());
            
            claims.put("roles", roleNames);
            logger.info("   ✅ Roles added to JWT: {}", roleNames);
        }

        // ✅ BUILD TOKEN
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
     * Get user ID from token
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = extractClaims(token);
        if (claims != null && claims.containsKey("id")) {
            Object id = claims.get("id");
            return id instanceof Long ? (Long) id : ((Integer) id).longValue();
        }
        return null;
    }

    /**
     * ✅ NEW: Get enterprise ID from token
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