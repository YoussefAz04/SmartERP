package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.dto.CreateEmployeeRequest;
import BuissnesManager.BuissnesManager.dto.UserRequest;
import BuissnesManager.BuissnesManager.entity.User;
import BuissnesManager.BuissnesManager.dto.UserDTO;
import BuissnesManager.BuissnesManager.security.AuthorizationUtil;
import BuissnesManager.BuissnesManager.security.JWTUtil;
import BuissnesManager.BuissnesManager.service.ImageService;
import BuissnesManager.BuissnesManager.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping("/api/users")
@RestController
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;
    @Autowired
    private ImageService imageService;
    @Autowired
    private JWTUtil jwtUtil;
    @Autowired
    private AuthorizationUtil authorizationUtil;

    @GetMapping("/test")
    public ResponseEntity<?> testToken() {
        return ResponseEntity.ok("Token valide");
    }

    /**
     * Create a user.
     * SUPER_ADMIN: can create any user for any enterprise.
     * ADMIN: can only create users for their own enterprise.
     */
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @RequestBody UserRequest request,
            @RequestParam Long idEntreprise,
            @RequestParam Long idEquipe,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authorizationUtil.isAdmin() && !authorizationUtil.isSuperAdmin()) {
                Long tokenEnterpriseId = extractEnterpriseId(authHeader);
                if (tokenEnterpriseId == null) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("Enterprise context not found in token");
                }
                if (!tokenEnterpriseId.equals(idEntreprise)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("ADMIN can only create users for their own enterprise");
                }
            }
            User createdUser = userService.createUserWithEntrepriseAndEquipe(request, idEntreprise, idEquipe);
            return ResponseEntity.ok(createdUser);
        } catch (IllegalArgumentException e) {
            logger.error("Validation error creating user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (AccessDeniedException e) {
            logger.error("Access denied creating user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error creating user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur: " + e.getMessage());
        }
    }

    /**
     * Create an employee user.
     * ADMIN: can only create employees for their own enterprise (enterpriseId from token overrides request).
     * SUPER_ADMIN: can create employees for any enterprise (uses enterpriseId from request body).
     */
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    @PostMapping("/create-employee")
    public ResponseEntity<?> createEmployeeUser(
            @RequestBody CreateEmployeeRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authorizationUtil.isAdmin() && !authorizationUtil.isSuperAdmin()) {
                Long tokenEnterpriseId = extractEnterpriseId(authHeader);
                if (tokenEnterpriseId == null) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("Enterprise context not found in token");
                }
                request.setEnterpriseId(tokenEnterpriseId);
            }
            UserDTO createdUser = userService.createEmployeeWithModules(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (IllegalArgumentException e) {
            logger.error("Validation error creating employee: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error creating employee: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Une erreur inattendue s'est produite lors de la création de l'employé");
        }
    }

    /**
     * Returns users visible to the current user.
     * SUPER_ADMIN: all users. ADMIN: only users in their enterprise.
     */
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    @GetMapping("/getAllUsers")
    public ResponseEntity<?> getAllUsers(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authorizationUtil.isSuperAdmin()) {
                return ResponseEntity.ok(userService.getAllUsers());
            }
            Long enterpriseId = extractEnterpriseId(authHeader);
            if (enterpriseId == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Enterprise context not found");
            }
            return ResponseEntity.ok(userService.getUsersByEnterprise(enterpriseId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * Returns users by enterprise.
     * SUPER_ADMIN: can access any enterprise.
     * ADMIN: can only access their own enterprise.
     */
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    @GetMapping("/byEntreprise/{entrepriseId}")
    public ResponseEntity<?> getUsersByEntreprise(
            @PathVariable Long entrepriseId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (!authorizationUtil.canAccessEnterprise(entrepriseId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
            }
            return ResponseEntity.ok(userService.getUsersByEnterprise(entrepriseId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * Returns user DTOs visible to the current user.
     * SUPER_ADMIN: all users. ADMIN: only users in their enterprise.
     */
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    @GetMapping("/getAllUsersDTo")
    public ResponseEntity<?> getAllUsersDTO(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authorizationUtil.isSuperAdmin()) {
                return ResponseEntity.ok(userService.getAllUsersDTO());
            }
            Long enterpriseId = extractEnterpriseId(authHeader);
            if (enterpriseId == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Enterprise context not found");
            }
            return ResponseEntity.ok(userService.getAllUsersDTOByEnterprise(enterpriseId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/getUsersById/{id}")
    public ResponseEntity<?> getAllUserById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(userService.getUserById(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

  
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    @PutMapping("/updateUser/{id}")
    public ResponseEntity<?> updateUser(
            @RequestBody UserDTO userDTO,
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authorizationUtil.isAdmin() && !authorizationUtil.isSuperAdmin()) {
                Long tokenEnterpriseId = extractEnterpriseId(authHeader);
                if (!userService.isUserInEnterprise(id, tokenEnterpriseId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("ADMIN can only update users in their own enterprise");
                }
            }
            userService.updateUser(userDTO, id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erreur : " + e.getMessage());
        }
    }

    /**
     * Delete a user.
     * ADMIN can only delete users in their own enterprise.
     */
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authorizationUtil.isAdmin() && !authorizationUtil.isSuperAdmin()) {
                Long tokenEnterpriseId = extractEnterpriseId(authHeader);
                if (!userService.isUserInEnterprise(id, tokenEnterpriseId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("error", "ADMIN can only delete users in their own enterprise",
                                    "status", "error"));
                }
            }
            userService.deleteUser(id);
            return ResponseEntity.ok(Map.of("message", "User deleted successfully", "status", "success"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage(), "status", "error"));
        }
    }

    /**
     * Activate or deactivate a user account.
     * ADMIN can only toggle users in their own enterprise.
     * A deactivated user is rejected at login.
     */
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> setUserStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Boolean enabled = body == null ? null : body.get("enabled");
            if (enabled == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Le champ 'enabled' est requis", "status", "error"));
            }
            if (authorizationUtil.isAdmin() && !authorizationUtil.isSuperAdmin()) {
                Long tokenEnterpriseId = extractEnterpriseId(authHeader);
                if (tokenEnterpriseId == null || !userService.isUserInEnterprise(id, tokenEnterpriseId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("error", "ADMIN can only manage users in their own enterprise",
                                    "status", "error"));
                }
            }
            UserDTO updated = userService.setUserEnabled(id, enabled);
            return ResponseEntity.ok(Map.of(
                    "message", enabled ? "Utilisateur activé avec succès" : "Utilisateur désactivé avec succès",
                    "status", "success",
                    "user", updated));
        } catch (Exception e) {
            logger.error("Error toggling user status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage(), "status", "error"));
        }
    }

    // ----------------------------------------------------------------
    // Helper
    // ----------------------------------------------------------------

    private Long extractEnterpriseId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return jwtUtil.getEntrepriseIdFromToken(authHeader.substring(7));
    }
}
