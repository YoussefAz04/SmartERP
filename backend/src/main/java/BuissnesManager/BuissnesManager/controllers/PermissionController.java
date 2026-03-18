package BuissnesManager.BuissnesManager.controllers;


import BuissnesManager.BuissnesManager.entity.Permission;
import BuissnesManager.BuissnesManager.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.function.EntityResponse;

@RestController
@RequestMapping("permission")
public class PermissionController {
    @Autowired
    private PermissionService permissionService ;



    @PostMapping("add/{idRole}/{idModule}")
    private ResponseEntity<?> createPermission(@RequestBody Permission permission, @PathVariable Long idRole , @PathVariable Long idModule){
        try {
            return ResponseEntity.ok().body(permissionService.addPermission(permission, idRole, idModule));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    @GetMapping("getPermissionByRole/{idRole}")
    public ResponseEntity<?> getPermissionByRole(@PathVariable Long idRole){
        try {
            return ResponseEntity.status(HttpStatus.OK).body(permissionService.getPermissionsByRole(idRole));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
