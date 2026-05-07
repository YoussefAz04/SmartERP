package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.dto.AuthRequest;
import BuissnesManager.BuissnesManager.dto.PermissionDto;
import BuissnesManager.BuissnesManager.dto.RegisterRequest;
import BuissnesManager.BuissnesManager.dto.RegisterResponse;
import BuissnesManager.BuissnesManager.dto.Response;
import BuissnesManager.BuissnesManager.entity.Entreprise;
import BuissnesManager.BuissnesManager.entity.RefreshToken;
import BuissnesManager.BuissnesManager.entity.Role;
import BuissnesManager.BuissnesManager.entity.RoleType;
import BuissnesManager.BuissnesManager.entity.User;
import BuissnesManager.BuissnesManager.repository.EntrepriseRepo;
import BuissnesManager.BuissnesManager.repository.RefreshTokenRepo;
import BuissnesManager.BuissnesManager.repository.RoleRepo;
import BuissnesManager.BuissnesManager.repository.UserRepo;
import BuissnesManager.BuissnesManager.security.JWTUtil;
import BuissnesManager.BuissnesManager.service.AuditLogService;
import BuissnesManager.BuissnesManager.service.PermissionService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.security.SecureRandom;
import java.util.Base64;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class AuthController {

    /** Access-token cookie TTL in seconds (matches JWT expiration: 15 minutes). */
    private static final int ACCESS_COOKIE_MAX_AGE = 15 * 60;

    /** Refresh-token cookie TTL in seconds (7 days). */
    private static final int REFRESH_COOKIE_MAX_AGE = 7 * 24 * 60 * 60;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    private EntrepriseRepo entrepriseRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private RefreshTokenRepo refreshTokenRepo;

    @Value("${cookie.secure:false}")
    private boolean cookieSecure;

    // ───────────────────────────────────────────────────────────
    // Cookie helpers
    // ───────────────────────────────────────────────────────────

    private Cookie buildAccessCookie(String token) {
        Cookie cookie = new Cookie("access_token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/");
        cookie.setMaxAge(ACCESS_COOKIE_MAX_AGE);
        // SameSite=Strict via response header — Cookie API does not expose it directly
        return cookie;
    }

    private Cookie buildRefreshCookie(String token) {
        Cookie cookie = new Cookie("refresh_token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/api/auth/refresh");
        cookie.setMaxAge(REFRESH_COOKIE_MAX_AGE);
        return cookie;
    }

    private Cookie buildClearCookie(String name, String path) {
        Cookie cookie = new Cookie(name, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath(path);
        cookie.setMaxAge(0);
        return cookie;
    }

    private void addSameSiteStrict(HttpServletResponse response) {
        // Append SameSite=Strict to every Set-Cookie header issued by this response.
        // The Servlet API does not natively expose SameSite, so we rewrite the headers.
        java.util.Collection<String> setCookieHeaders = response.getHeaders("Set-Cookie");
        boolean first = true;
        for (String header : setCookieHeaders) {
            if (!header.contains("SameSite")) {
                header = header + "; SameSite=Strict";
            }
            if (first) {
                response.setHeader("Set-Cookie", header);
                first = false;
            } else {
                response.addHeader("Set-Cookie", header);
            }
        }
    }

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private RefreshToken createRefreshToken(User user) {
        refreshTokenRepo.revokeAllByUser(user);
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        RefreshToken rt = new RefreshToken(rawToken, user, Instant.now().plusSeconds(REFRESH_COOKIE_MAX_AGE));
        return refreshTokenRepo.save(rt);
    }

    /**
     * Register a new user
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        System.out.println("✅ Register endpoint hit for username: " + request.getUsername());

        // Validate required fields
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username is required");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Password must be at least 6 characters");
        }
        if (!request.getEmail().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid email format");
        }

        // Check for duplicate username
        if (userRepo.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
        }

        // Check for duplicate email
        if (userRepo.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
        }

        try {
            User newUser = new User();
            newUser.setUsername(request.getUsername());
            newUser.setEmail(request.getEmail());
            newUser.setPassword(passwordEncoder.encode(request.getPassword()));
            newUser.setEnabled(true);

            // Assign enterprise if provided
            if (request.getEnterpriseId() != null) {
                Entreprise entreprise = entrepriseRepo.findById(request.getEnterpriseId())
                        .orElse(null);
                if (entreprise != null) {
                    newUser.setEntreprise(entreprise);
                    System.out.println("✅ User assigned to enterprise: " + entreprise.getNom());
                } else {
                    System.out.println("⚠️ Enterprise not found with id: " + request.getEnterpriseId());
                }
            }

            // Assign default USER role
            Role userRole = roleRepo.findByName("USER")
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setName("USER");
                        return roleRepo.save(role);
                    });
            newUser.setRoles(Collections.singletonList(userRole));

            User savedUser = userRepo.save(newUser);
            System.out.println("✅ User registered successfully: " + savedUser.getUsername());

            Long entrepriseId = savedUser.getEntreprise() != null ? savedUser.getEntreprise().getId() : null;
            auditLogService.log("USER_CREATED",
                    "Nouvel utilisateur créé via register: " + savedUser.getUsername(),
                    savedUser.getUsername(), savedUser.getId(), entrepriseId);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new RegisterResponse("User registered successfully", true, savedUser.getUsername(), savedUser.getId()));
        } catch (Exception e) {
            System.err.println("❌ Registration error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Registration failed: " + e.getMessage());
        }
    }

    /**
     * Login user — sets httpOnly access_token + refresh_token cookies and returns
     * user info (without the raw token) in the JSON body.
     * POST /api/auth/login
     */
    @PostMapping("/login")
    @Transactional
    public ResponseEntity<?> login(@RequestBody AuthRequest request, HttpServletResponse httpResponse) {
        System.out.println("✅ Login endpoint hit for username: " + request.getUsername());
        
        List<PermissionDto> permissionDtoList = new ArrayList<>();

        try {
            Optional<User> userOptional = userRepo.findByUsername(request.getUsername());
            if (userOptional.isEmpty()) {
                System.out.println("❌ User not found in database: " + request.getUsername());
                Map<String, String> body = new HashMap<>();
                body.put("error", "INVALID_USERNAME");
                body.put("message", "Nom d'utilisateur invalide");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
            }

            User dbUser = userOptional.get();

            // Pre-check the account status so we can return a specific "compte inactif"
            // message even when the AuthenticationManager would otherwise raise a generic
            // BadCredentialsException for the same situation.
            if (!dbUser.isEnabled()) {
                System.err.println("❌ Login refused (account disabled): " + request.getUsername());
                Map<String, String> body = new HashMap<>();
                body.put("error", "ACCOUNT_DISABLED");
                body.put("message", "Compte inactif. Veuillez contacter votre administrateur.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            User user = userRepo.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found after authentication"));

            // Transparently upgrade legacy plaintext passwords to BCrypt now that
            // we have verified the raw password is correct.
            if (passwordEncoder.upgradeEncoding(user.getPassword())) {
                user.setPassword(passwordEncoder.encode(request.getPassword()));
                user = userRepo.save(user);
                System.out.println("✅ Password rehashed (BCrypt upgrade) for user: " + user.getUsername());
            }

            String accessToken = jwtUtil.generateToken(user);
            RefreshToken refreshToken = createRefreshToken(user);
            
            System.out.println("✅ User connected: " + user.getUsername());

            // Get permissions for user's roles
            for (Role role : user.getRoles()) {
                permissionDtoList.addAll(permissionService.getPermissionsByRole(role.getId()));
            }

            // User-specific permissions (set when creating an employee) take priority over
            // role-level defaults. When present they completely replace the role-level list
            // because per-employee grants are intentionally more restrictive than the role
            // defaults, and mixing both would incorrectly re-grant access the admin chose
            // to withhold (e.g. role has full access but this employee has read-only).
            List<PermissionDto> userPermissions = permissionService.getPermissionsByUser(user.getId());
            if (!userPermissions.isEmpty()) {
                permissionDtoList = userPermissions;
            }

            // Build response — include token in body for backwards compatibility
            // (the Angular app still reads it from the JSON response to populate
            //  the in-memory BehaviorSubject / localStorage during migration).
            Response response = new Response();
            response.setId(user.getId());
            response.setUsername(user.getUsername());
            response.setEmail(user.getEmail());
            response.setName(user.getUsername());
            response.setToken(accessToken);
            response.setEnabled(user.isEnabled());
            
            List<String> roleNames = new ArrayList<>();
            if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                for (Role role : user.getRoles()) {
                    roleNames.add(role.getName().toUpperCase());
                }
            }
            response.setRoles(roleNames);
            response.setPermission(permissionDtoList);

            // COMPUTE NUMERIC roleId: SUPER_ADMIN=0, ADMIN=1, EMPLOYEE=2
            final int ROLE_SUPER_ADMIN = 0;
            final int ROLE_ADMIN = 1;
            final int ROLE_EMPLOYEE = 2;
            int computedRoleId = ROLE_EMPLOYEE;
            if (user.getRoles() != null) {
                for (Role role : user.getRoles()) {
                    if (role.getRoleType() == RoleType.SUPER_ADMIN) {
                        computedRoleId = ROLE_SUPER_ADMIN;
                        break;
                    } else if (role.getRoleType() == RoleType.ADMIN && computedRoleId > ROLE_ADMIN) {
                        computedRoleId = ROLE_ADMIN;
                    }
                }
            }
            response.setRoleId(computedRoleId);

            Long entrepriseId = null;
            String entrepriseName = null;
            if (user.getEntreprise() != null) {
                entrepriseId = user.getEntreprise().getId();
                entrepriseName = user.getEntreprise().getNom();
                System.out.println("✅ Enterprise from user: " + entrepriseId + " - " + entrepriseName);
            } else {
                System.out.println("⚠️ User has no enterprise assigned");
            }

            response.setEntrepriseId(entrepriseId);
            response.setEntrepriseName(entrepriseName);

            System.out.println("✅ Login successful for: " + user.getUsername());

            // Set httpOnly cookies
            httpResponse.addCookie(buildAccessCookie(accessToken));
            httpResponse.addCookie(buildRefreshCookie(refreshToken.getToken()));
            addSameSiteStrict(httpResponse);

            auditLogService.log("LOGIN",
                    "Connexion réussie pour l'utilisateur: " + user.getUsername(),
                    user.getUsername(), user.getId(), entrepriseId);

            return ResponseEntity.ok(response);
        } catch (org.springframework.security.authentication.DisabledException e) {
            System.err.println("❌ Login refused (account disabled): " + request.getUsername());
            Map<String, String> body = new HashMap<>();
            body.put("error", "ACCOUNT_DISABLED");
            body.put("message", "Compte inactif. Veuillez contacter votre administrateur.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            System.err.println("❌ Login refused (bad password): " + request.getUsername());
            Map<String, String> body = new HashMap<>();
            body.put("error", "INVALID_PASSWORD");
            body.put("message", "Mot de passe invalide");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
            System.err.println("❌ Login refused (username not found): " + request.getUsername());
            Map<String, String> body = new HashMap<>();
            body.put("error", "INVALID_USERNAME");
            body.put("message", "Nom d'utilisateur invalide");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        } catch (Exception e) {
            System.err.println("❌ Login error: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> body = new HashMap<>();
            body.put("error", "AUTH_ERROR");
            body.put("message", "Identifiants invalides. Veuillez réessayer.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        }
    }

    /**
     * Refresh access token using the refresh_token httpOnly cookie.
     * POST /api/auth/refresh
     * Returns a new access_token cookie and updated JSON body.
     */
    @PostMapping("/refresh")
    @Transactional
    public ResponseEntity<?> refresh(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String rawRefreshToken = null;
        if (httpRequest.getCookies() != null) {
            for (Cookie cookie : httpRequest.getCookies()) {
                if ("refresh_token".equals(cookie.getName())) {
                    rawRefreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "AUTHENTICATION_FAILED"));
        }

        Optional<RefreshToken> rtOpt = refreshTokenRepo.findByToken(rawRefreshToken);
        if (rtOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "AUTHENTICATION_FAILED"));
        }

        RefreshToken rt = rtOpt.get();
        if (rt.isRevoked() || rt.isExpired()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "AUTHENTICATION_FAILED"));
        }

        User user = rt.getUser();
        if (!user.isEnabled()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "AUTHENTICATION_FAILED"));
        }

        // Issue new access token; rotate refresh token
        String newAccessToken = jwtUtil.generateToken(user);
        RefreshToken newRefreshToken = createRefreshToken(user);

        httpResponse.addCookie(buildAccessCookie(newAccessToken));
        httpResponse.addCookie(buildRefreshCookie(newRefreshToken.getToken()));
        addSameSiteStrict(httpResponse);

        Map<String, Object> body = new HashMap<>();
        body.put("token", newAccessToken);
        body.put("username", user.getUsername());
        return ResponseEntity.ok(body);
    }

    /**
     * Logout — revokes the refresh token and clears auth cookies.
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    @Transactional
    public ResponseEntity<?> logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        if (httpRequest.getCookies() != null) {
            for (Cookie cookie : httpRequest.getCookies()) {
                if ("refresh_token".equals(cookie.getName())) {
                    refreshTokenRepo.findByToken(cookie.getValue())
                            .ifPresent(rt -> {
                                rt.setRevoked(true);
                                refreshTokenRepo.save(rt);
                            });
                    break;
                }
            }
        }
        httpResponse.addCookie(buildClearCookie("access_token", "/"));
        httpResponse.addCookie(buildClearCookie("refresh_token", "/api/auth/refresh"));
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    /**
     * Request a password reset.
     * POST /api/auth/forgot-password  body: { "email": "..." }
     *
     * Generates a one-time token (valid 30 minutes) and stores it on the user.
     * Because no SMTP server is configured in this project, the token and a
     * ready-to-use reset link are returned directly in the response so the UI
     * can display them. To avoid leaking which emails are registered, the
     * endpoint always returns 200 OK with a generic message; the token/link
     * fields are only populated when the email actually matches an account.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request != null ? request.get("email") : null;
        System.out.println("✅ Forgot-password endpoint hit for email: " + email);

        Map<String, Object> body = new HashMap<>();
        body.put("message", "If an account exists for this email, a password reset link has been generated.");

        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email is required");
        }

        try {
            Optional<User> userOpt = userRepo.findByEmail(email.trim());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                String token = UUID.randomUUID().toString().replace("-", "");
                user.setResetPasswordToken(token);
                user.setResetPasswordTokenExpiry(LocalDateTime.now().plusMinutes(30));
                userRepo.save(user);

                // No SMTP configured: return the token/link directly so the UI
                // (or a developer/admin) can use it. In a production deployment
                // with email enabled, these fields should be removed and the
                // link emailed to the user instead.
                String resetLink = "/auth/reset-password?token=" + token;
                body.put("token", token);
                body.put("resetLink", resetLink);
                body.put("expiresAt", user.getResetPasswordTokenExpiry().toString());
                System.out.println("✅ Password reset token generated for user: " + user.getUsername());
            } else {
                System.out.println("ℹ️ Forgot-password requested for unknown email: " + email);
            }
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            System.err.println("❌ Forgot-password error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Could not process password reset request: " + e.getMessage());
        }
    }

    /**
     * Reset a password using a token previously issued by /forgot-password.
     * POST /api/auth/reset-password  body: { "token": "...", "newPassword": "..." }
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request != null ? request.get("token") : null;
        String newPassword = request != null ? request.get("newPassword") : null;
        System.out.println("✅ Reset-password endpoint hit");

        if (token == null || token.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token is required");
        }
        if (newPassword == null || newPassword.length() < 6) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Password must be at least 6 characters");
        }

        try {
            Optional<User> userOpt = userRepo.findByResetPasswordToken(token.trim());
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired reset token");
            }

            User user = userOpt.get();
            LocalDateTime expiry = user.getResetPasswordTokenExpiry();
            if (expiry == null || expiry.isBefore(LocalDateTime.now())) {
                // Clear the stale token so it cannot be retried
                user.setResetPasswordToken(null);
                user.setResetPasswordTokenExpiry(null);
                userRepo.save(user);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired reset token");
            }

            user.setPassword(passwordEncoder.encode(newPassword));
            // Single-use: invalidate the token
            user.setResetPasswordToken(null);
            user.setResetPasswordTokenExpiry(null);
            userRepo.save(user);

            System.out.println("✅ Password reset successful for user: " + user.getUsername());
            Map<String, Object> body = new HashMap<>();
            body.put("message", "Password updated successfully. You can now sign in with your new password.");
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            System.err.println("❌ Reset-password error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Could not reset password: " + e.getMessage());
        }
    }

    /**
     * Test protected endpoint
     * GET /api/auth/test
     */
    @GetMapping("/test")
    public ResponseEntity<?> testToken() {
        System.out.println("✅ Test token endpoint hit");
        return ResponseEntity.ok("Token is valid! You are authenticated.");
    }

    /**
     * Refresh user permissions
     * POST /api/auth/refreshPermissionDtoList
     */
    @PostMapping("/refreshPermissionDtoList")
    public ResponseEntity<?> refreshPermissionDtoList(@RequestParam Long userId) {
        System.out.println("✅ Refresh permissions for user: " + userId);
        
        try {
            User user = userRepo.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            List<PermissionDto> permissionDtoList = new ArrayList<>();
            
            for (Role role : user.getRoles()) {
                permissionDtoList.addAll(permissionService.getPermissionsByRole(role.getId()));
            }

            // Per-user permissions (set when creating an employee) take priority over
            // role-level defaults — mirrors the login flow. When present they completely
            // replace the role-level list because per-employee grants are intentionally
            // more restrictive than the role defaults.
            List<PermissionDto> userPermissions = permissionService.getPermissionsByUser(userId);
            if (!userPermissions.isEmpty()) {
                permissionDtoList = userPermissions;
            }

            System.out.println("✅ Permissions refreshed for user: " + userId);
            return ResponseEntity.ok(permissionDtoList);
        } catch (Exception e) {
            System.err.println("❌ Error refreshing permissions: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error refreshing permissions: " + e.getMessage());
        }
    }
}