package BuissnesManager.BuissnesManager.controllers;


import BuissnesManager.BuissnesManager.dto.ModulePermissionRequest;
import BuissnesManager.BuissnesManager.dto.PermissionDto;
import BuissnesManager.BuissnesManager.entity.Permission;
import BuissnesManager.BuissnesManager.service.AuditLogService;
import BuissnesManager.BuissnesManager.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("permission")
public class PermissionController {
    @Autowired
    private PermissionService permissionService ;

    @Autowired
    private AuditLogService auditLogService;



    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    @PostMapping("add/{idRole}/{idModule}")
    public ResponseEntity<?> createPermission(@RequestBody Permission permission, @PathVariable Long idRole , @PathVariable Long idModule){
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

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    @GetMapping("getPermissionByUser/{userId}")
    public ResponseEntity<?> getPermissionByUser(@PathVariable Long userId) {
        try {
            List<PermissionDto> permissions = permissionService.getPermissionsByUser(userId);
            return ResponseEntity.ok(permissions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    @PutMapping("updateUserPermissions/{userId}")
    public ResponseEntity<?> updateUserPermissions(
            @PathVariable Long userId,
            @RequestBody List<ModulePermissionRequest> permissions) {
        try {
            permissionService.updateUserPermissions(userId, permissions);
            auditLogService.log("PERMISSION_MODIFIED",
                    "Permissions mises à jour pour l'utilisateur id=" + userId + " (" + permissions.size() + " module(s))");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
