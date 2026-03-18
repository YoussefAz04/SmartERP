package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.dto.AuthRequest;
import BuissnesManager.BuissnesManager.dto.AuthResponse;
import BuissnesManager.BuissnesManager.dto.PermissionDto;
import BuissnesManager.BuissnesManager.dto.Response;
import BuissnesManager.BuissnesManager.entity.Role;
import BuissnesManager.BuissnesManager.entity.User;
import BuissnesManager.BuissnesManager.repository.PermissionRepo;
import BuissnesManager.BuissnesManager.repository.UserRepo;
import BuissnesManager.BuissnesManager.security.JWTFilter;
import BuissnesManager.BuissnesManager.security.JWTUtil;
import BuissnesManager.BuissnesManager.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:4200")

public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JWTUtil jwtUtil;
    @Autowired
    private UserRepo userRepo ;
    @Autowired
    private PermissionService permissionService ;



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
