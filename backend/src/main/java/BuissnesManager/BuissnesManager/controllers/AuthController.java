package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.dto.AuthRequest;
import BuissnesManager.BuissnesManager.dto.PermissionDto;
import BuissnesManager.BuissnesManager.dto.RegisterRequest;
import BuissnesManager.BuissnesManager.dto.RegisterResponse;
import BuissnesManager.BuissnesManager.dto.Response;
import BuissnesManager.BuissnesManager.entity.Role;
import BuissnesManager.BuissnesManager.entity.User;
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

@RestController
@RequestMapping("/auth")
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
    private PasswordEncoder passwordEncoder;
    @Autowired
    private PermissionService permissionService;



    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        System.out.println("Register endpoint hit for username: " + request.getUsername());

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

            // Assign default USER role
            Role userRole = roleRepo.findByName("USER")
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setName("USER");
                        return roleRepo.save(role);
                    });
            newUser.setRoles(Collections.singletonList(userRole));

            User savedUser = userRepo.save(newUser);
            System.out.println("User registered successfully: " + savedUser.getUsername());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new RegisterResponse("User registered successfully", true, savedUser.getUsername(), savedUser.getId()));
        } catch (Exception e) {
            System.err.println("Registration error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Registration failed: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        List<PermissionDto> permissionDtoList = new ArrayList<>();

                System.out.println("Login endpoint hit");
        try {

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails.getUsername());

            User user = userRepo.findByUsername(userDetails.getUsername()).get();
            System.out.println("user connecte" + user.getUsername());
            for (Role role : user.getRoles()){
                 permissionDtoList =  permissionService.getPermissionsByRole(role.getId());
            }


            Response response = new Response() ;
            response.setUserName(user.getUsername());
            response.setEntreprise(user.getEntreprise());
            response.setPermissionDtos(permissionDtoList);
            response.setIdUsers(user.getId());
            response.setToken(token);
            response.setIdRoleUser(user.getRoles().get(0).getId());
            System.out.println(response.getIdRoleUser());



//            Map<String,Object> response = new HashMap<>();
//            response.put("token",token);
//            response.put("username",user.getUsername());
//            response.put("userId",user.getId());
//            response.put("entreprise",user.getEntreprise());
//            response.put("permission",permissionDtoList);



            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Identifiants invalides");
        }
    }


    // Test d'une route protégée
    @GetMapping("/test")
    public ResponseEntity<?> testToken() {
        return ResponseEntity.ok("Token valide ! Vous êtes authentifié.");
    }

    @PostMapping("/refreshPermissionDtoList")
    public ResponseEntity<?> refreshPermissionDtoList(Long userId) {
        User user = userRepo.findById(userId).get();
        List<PermissionDto> permissionDtoList = new ArrayList<>();
        for (Role role : user.getRoles()){
            permissionDtoList =  permissionService.getPermissionsByRole(role.getId());
        }
        return ResponseEntity.ok(permissionDtoList);

    }

}
