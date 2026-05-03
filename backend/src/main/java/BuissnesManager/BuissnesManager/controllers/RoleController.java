package BuissnesManager.BuissnesManager.controllers;


import BuissnesManager.BuissnesManager.entity.Entreprise;
import BuissnesManager.BuissnesManager.entity.Role;
import BuissnesManager.BuissnesManager.repository.EntrepriseRepo;
import BuissnesManager.BuissnesManager.security.AuthorizationUtil;
import BuissnesManager.BuissnesManager.security.EnterpriseSecurityUtil;
import BuissnesManager.BuissnesManager.service.RoleService;
import BuissnesManager.BuissnesManager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/roles")

public class RoleController {

    @Autowired
    private  RoleService roleService;
    @Autowired
    private  UserService userService ;
    @Autowired
    private AuthorizationUtil authorizationUtil;
    @Autowired
    private EnterpriseSecurityUtil enterpriseSecurityUtil;
    @Autowired
    private EntrepriseRepo entrepriseRepo;


    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    @PostMapping("create")
    public ResponseEntity<?> create(@RequestBody Role role) {
        try {
            if (authorizationUtil.isAdmin() && !authorizationUtil.isSuperAdmin()) {
                Long enterpriseId = enterpriseSecurityUtil.getContextEnterpriseId();
                if (enterpriseId != null) {
                    Entreprise entreprise = entrepriseRepo.findById(enterpriseId)
                            .orElseThrow(() -> new RuntimeException("Entreprise non trouvée"));
                    role.setEntreprise(entreprise);
                }
            }
            return ResponseEntity.ok(roleService.create(role));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Erreur : " + e.getMessage());
        }
    }


    @GetMapping("getAllRole")
    public ResponseEntity<List<Role>> getAll() {
        if (authorizationUtil.isAdmin() && !authorizationUtil.isSuperAdmin()) {
            Long enterpriseId = enterpriseSecurityUtil.getContextEnterpriseId();
            if (enterpriseId != null) {
                List<Role> combined = new ArrayList<>();
                combined.addAll(roleService.getAllGlobal());
                combined.addAll(roleService.getAllByEnterprise(enterpriseId));
                return ResponseEntity.ok(combined);
            }
        }
        return ResponseEntity.ok(roleService.getAll());
    }

    @GetMapping("getById/{id}")
    public ResponseEntity<Role> getById(@PathVariable Long id) {
        return roleService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    @PutMapping("updateRole/{id}")
    public ResponseEntity<Role> update(@PathVariable Long id, @RequestBody Role role) {
        return ResponseEntity.ok(roleService.update(id, role));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    @DeleteMapping("delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    @PostMapping("/assign-role/{idUser}/{idRole}")
    public ResponseEntity<?> assignRoleToUser(@PathVariable Long idUser,@PathVariable Long idRole) {
        try {
            userService.assignRoleToUser(idUser, idRole);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    @DeleteMapping("/remove-role/{idUser}/{idRole}")
    public ResponseEntity<?> removeRoleFromUser(@PathVariable Long idUser, @PathVariable Long idRole) {
        try {
            userService.removeRoleFromUser(idUser, idRole);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

}