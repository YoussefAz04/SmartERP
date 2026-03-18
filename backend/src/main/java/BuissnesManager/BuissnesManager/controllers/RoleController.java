package BuissnesManager.BuissnesManager.controllers;


import BuissnesManager.BuissnesManager.dto.AssignRoleRequest;
import BuissnesManager.BuissnesManager.entity.Role;
import BuissnesManager.BuissnesManager.service.RoleService;
import BuissnesManager.BuissnesManager.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")

public class RoleController {

    @Autowired
    private  RoleService roleService;
    @Autowired
    private  UserService userService ;


    @PostMapping("create")
    public ResponseEntity<?> create(@RequestBody Role role) {
        try {
            return ResponseEntity.ok(roleService.create(role));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Erreur : " + e.getMessage());
        }
    }


    @GetMapping("getAllRole")
    public ResponseEntity<List<Role>> getAll() {
        return ResponseEntity.ok(roleService.getAll());
    }

    @GetMapping("getById/{id}")
    public ResponseEntity<Role> getById(@PathVariable Long id) {
        return roleService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("updateRole/{id}")
    public ResponseEntity<Role> update(@PathVariable Long id, @RequestBody Role role) {
        return ResponseEntity.ok(roleService.update(id, role));
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/assign-role/{idUser}/{idRole}")
    public ResponseEntity<?> assignRoleToUser(@PathVariable Long idUser,@PathVariable Long idRole) {
        try {
            userService.assignRoleToUser(idUser, idRole);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }


}