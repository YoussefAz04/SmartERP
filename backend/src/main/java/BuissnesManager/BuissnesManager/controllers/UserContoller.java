package BuissnesManager.BuissnesManager.controllers;

import BuissnesManager.BuissnesManager.dto.UserRequest;
import BuissnesManager.BuissnesManager.entity.User;
import BuissnesManager.BuissnesManager.entity.UserDTO;
import BuissnesManager.BuissnesManager.service.ImageService;
import BuissnesManager.BuissnesManager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/api/users")
@RestController
public class UserContoller {


    @Autowired
    private UserService userService ;
    @Autowired
    private ImageService imageService;
    @GetMapping("/test")
    public ResponseEntity<?> testToken() {
        return ResponseEntity.ok("Token valide");
    }
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


    @GetMapping("/getAllUsers")
    public  ResponseEntity<?> getAllUsers(){
        try {
            return ResponseEntity.status(HttpStatus.OK).body(userService.getAllUsers());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
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

//    @GetMapping("/getAllUsersNotAffectedToMembre")
//    public  ResponseEntity<?> getAllUsersNotAffectedToMembre(){
//        try {
//            return ResponseEntity.status(HttpStatus.OK).body(userService.getAllUsersNotAffectedToMember());
//        }catch (Exception e){
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
//        }
//    }


}
