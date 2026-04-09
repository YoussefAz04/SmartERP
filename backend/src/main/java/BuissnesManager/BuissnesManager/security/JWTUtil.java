package BuissnesManager.BuissnesManager.security;

import BuissnesManager.BuissnesManager.entity.Role;
import BuissnesManager.BuissnesManager.entity.User;
import BuissnesManager.BuissnesManager.repository.UserRepo;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.JWTVerifier;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JWTUtil {

    @Autowired
    private UserRepo userRepo;

    private final String secret;
    private final Algorithm algorithm;
    private final long expirationTime;
    private final String issuer;

    public JWTUtil(
            @Value("${jwt_secret:sayariMohamed123123}") String secret,
            @Value("${jwt.expiration-time:36000000}") long expirationTime,
            @Value("${jwt.issuer:SymatiqueBusinessManager}") String issuer) {

        this.secret = secret;
        this.algorithm = Algorithm.HMAC256(secret);
        this.expirationTime = expirationTime;
        this.issuer = issuer;
        
        System.out.println("✅ JWTUtil initialized with secret: " + (secret != null ? "***" : "NULL"));
    }

    public String generateToken(String username) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        Long entrepriseId = user.getEntreprise() != null ? user.getEntreprise().getId() : null;

        return JWT.create()
                .withSubject(username)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + expirationTime))
                .withIssuer(issuer)
                .withClaim("roles", roleNames)
                .withClaim("userId", user.getId())
                .withClaim("entrepriseId", entrepriseId)
                .sign(algorithm);
    }

    public Long getEntrepriseIdFromToken(String token) {
        try {
            return getVerifier().verify(token).getClaim("entrepriseId").asLong();
        } catch (Exception e) {
            return null;
        }
    }

    public Long getUserIdFromToken(String token) {
        try {
            return getVerifier().verify(token).getClaim("userId").asLong();
        } catch (Exception e) {
            return null;
        }
    }

    public String extractUsername(String token) {
        return getVerifier().verify(token).getSubject();
    }

    public List<String> extractRoles(String token) {
        return getVerifier().verify(token).getClaim("roles").asList(String.class);
    }

    public boolean validateToken(String token, String username) {
        try {
            String tokenUsername = extractUsername(token);
            return tokenUsername.equals(username) && !isTokenExpired(token);
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        Date expiration = getVerifier().verify(token).getExpiresAt();
        return expiration.before(new Date());
    }

    private JWTVerifier getVerifier() {
        return JWT.require(algorithm)
                .withIssuer(issuer)
                .build();
    }
}