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
    private UserRepo userRepo ;

    private final String secret;
    private final Algorithm algorithm;

    private final long expirationTime;
    private final String issuer;

    public JWTUtil(
            @Value("${jwt_secret}") String secret,
            @Value("${jwt.expiration-time}") long expirationTime,
            @Value("${jwt.issuer}") String issuer) {

        this.secret = secret;
        this.algorithm = Algorithm.HMAC256(secret);
        this.expirationTime = expirationTime;
        this.issuer = issuer;
    }

    public String generateToken(String username) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));


        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());
        return JWT.create()
                .withSubject(username)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + expirationTime))
                .withIssuer(issuer)
                .withClaim("roles", roleNames)
                .sign(algorithm);
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
            // logger.error("Token invalide", e);
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
