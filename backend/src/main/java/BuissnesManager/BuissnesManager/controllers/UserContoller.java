package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.dto.UserRequest;
import BuissnesManager.BuissnesManager.entity.User;
import BuissnesManager.BuissnesManager.entity.UserDTO;
import BuissnesManager.BuissnesManager.security.JWTUtil;
import BuissnesManager.BuissnesManager.service.ImageService;
import BuissnesManager.BuissnesManager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/api/users")
@RestController
public class UserContoller {


    @Autowired
    private UserService userService ;
    @Autowired
    private ImageService imageService;
    @Autowired
    private JWTUtil jwtUtil;
    @GetMapping("/test")
    public ResponseEntity<?> testToken() {
        return ResponseEntity.ok("Token valide");
    }
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    @PostMapping(  "/register")
    public ResponseEntity<?> registerUser(
            @RequestBody UserRequest request,
            @RequestParam Long idEntreprise,
            @RequestParam Long idEquipe
             ) {
        try {
           // String url = imageService.upload(file);
            //request.setImageUrl(url);

            User createdUser = userService.createUserWithEntrepriseAndEquipe(request, idEntreprise, idEquipe);
            return ResponseEntity.ok(createdUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    @GetMapping("/getAllUsers")
    public  ResponseEntity<?> getAllUsers(){
        try {
            return ResponseEntity.status(HttpStatus.OK).body(userService.getAllUsers());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/byEntreprise/{entrepriseId}")
    public ResponseEntity<?> getUsersByEntreprise(
            @PathVariable Long entrepriseId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // Verify requesting user belongs to the same enterprise
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                Long tokenEntrepriseId = jwtUtil.getEntrepriseIdFromToken(token);
                if (tokenEntrepriseId != null && !tokenEntrepriseId.equals(entrepriseId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
                }
            }
            return ResponseEntity.status(HttpStatus.OK).body(userService.getUsersByEnterprise(entrepriseId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    @GetMapping("/getAllUsersDTo")
    public  ResponseEntity<?> getAllUsersDTO(){
        try {
            return ResponseEntity.status(HttpStatus.OK).body(userService.getAllUsersDTO());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/getUsersById/{id}")
    public  ResponseEntity<?> getAllUserById(@PathVariable Long id){
        try {
            return ResponseEntity.status(HttpStatus.OK).body(userService.getUserById(id));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
    @PutMapping("/updateUser/{id}")
    public ResponseEntity<?> updateUser(@RequestBody UserDTO userDTO, @PathVariable Long id) {
        try {
            userService.updateUser(userDTO, id);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erreur : " + e.getMessage());
        }
    }
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(new java.util.HashMap<String, String>() {{
                put("message", "User deleted successfully");
                put("status", "success");
            }});
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new java.util.HashMap<String, String>() {{
                    put("error", e.getMessage());
                    put("status", "error");
                }}
            );
        }
    }

}
