package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.dto.AuthRequest;
import BuissnesManager.BuissnesManager.dto.PermissionDto;
import BuissnesManager.BuissnesManager.dto.RegisterRequest;
import BuissnesManager.BuissnesManager.dto.RegisterResponse;
import BuissnesManager.BuissnesManager.dto.Response;
import BuissnesManager.BuissnesManager.entity.Entreprise;
import BuissnesManager.BuissnesManager.entity.Role;
import BuissnesManager.BuissnesManager.entity.User;
import BuissnesManager.BuissnesManager.repository.EntrepriseRepo;
import BuissnesManager.BuissnesManager.repository.RoleRepo;
import BuissnesManager.BuissnesManager.repository.UserRepo;
import BuissnesManager.BuissnesManager.security.JWTUtil;
import BuissnesManager.BuissnesManager.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

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
     * Login user and return JWT token
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        System.out.println("✅ Login endpoint hit for username: " + request.getUsername());
        
        List<PermissionDto> permissionDtoList = new ArrayList<>();

        try {
            Optional<User> userOptional = userRepo.findByUsername(request.getUsername());
            if (userOptional.isEmpty()) {
                System.out.println("❌ User not found in database: " + request.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid credentials: User not found");
            }

            User dbUser = userOptional.get();

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails.getUsername());

            User user = userRepo.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found after authentication"));
            
            System.out.println("✅ User connected: " + user.getUsername());

            // Get permissions for user's roles
            for (Role role : user.getRoles()) {
                permissionDtoList.addAll(permissionService.getPermissionsByRole(role.getId()));
            }

            // Build response
            Response response = new Response();
            response.setId(user.getId());
            response.setUsername(user.getUsername());
            response.setEmail(user.getEmail());
            response.setName(user.getUsername());
            response.setToken(token);
            response.setEnabled(user.isEnabled());
            
            List<String> roleNames = new ArrayList<>();
            if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                for (Role role : user.getRoles()) {
                    roleNames.add(role.getName());
                }
            }
            response.setRoles(roleNames);
            response.setPermission(permissionDtoList);

            // ✅ SET ENTERPRISE ID - Get from user's assigned enterprise
            Long entrepriseId = null;
            String entrepriseName = null;

            // ✅ GET FROM USER IF AVAILABLE
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
            System.out.println("✅ Enterprise ID in response: " + entrepriseId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ Login error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid credentials: " + e.getMessage());
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
                permissionDtoList = permissionService.getPermissionsByRole(role.getId());
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