package BuissnesManager.BuissnesManager.service;


import BuissnesManager.BuissnesManager.dto.ModulePermissionRequest;
import BuissnesManager.BuissnesManager.dto.PermissionDto;
import BuissnesManager.BuissnesManager.entity.Module;
import BuissnesManager.BuissnesManager.entity.Permission;
import BuissnesManager.BuissnesManager.entity.Role;
import BuissnesManager.BuissnesManager.entity.User;
import BuissnesManager.BuissnesManager.repository.ModuleRepo;
import BuissnesManager.BuissnesManager.repository.PermissionRepo;
import BuissnesManager.BuissnesManager.repository.RoleRepo;
import BuissnesManager.BuissnesManager.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PermissionService {
    @Autowired
    private PermissionRepo permissionRepo;

    @Autowired
    private ModuleRepo moduleRepo ;

    @Autowired
    private RoleRepo roleRepo ;

    @Autowired
    private UserRepo userRepo ;


    @Transactional
    public Permission addPermission(Permission permission,Long idRole ,Long idModule){

        Permission existe = permissionRepo.findByModuleIdAndRoleId(idModule,idRole) ;
        if (existe!=null){
            existe.setCreate(permission.isCreate());
            existe.setUpdate(permission.isUpdate());
            existe.setDelete(permission.isDelete());
            existe.setView(permission.isView());
            return permissionRepo.save(existe);
        }


        Module module = moduleRepo.findById(idModule)
                .orElseThrow(() -> new RuntimeException("Module non trouvé avec l'id : " + idModule));
        Role role = roleRepo.findById(idRole)
                .orElseThrow(() -> new RuntimeException("Role non trouvé avec l'id : " + idRole));
        permission.setModule(module);
        permission.setRole(role);
        roleRepo.save(role);
        return permissionRepo.save(permission);
    }

    private PermissionDto toDto(Permission permission) {
        PermissionDto dto = new PermissionDto();
        dto.setId(permission.getId());
        dto.setCreate(permission.isCreate());
        dto.setDelete(permission.isDelete());
        dto.setUpdate(permission.isUpdate());
        dto.setView(permission.isView());
        if (permission.getModule() != null) {
            dto.setModuleId(permission.getModule().getId());
            dto.setModuleTitle(permission.getModule().getTitle());
        }
        if (permission.getRole() != null) {
            dto.setRoleId(permission.getRole().getId());
        }
        return dto;
    }

    @Transactional(readOnly = true)
    public List<PermissionDto> getPermissionsByUser(Long userId) {
        List<Permission> userPermissions = permissionRepo.findAllByUserId(userId);
        if (!userPermissions.isEmpty()) {
            return userPermissions.stream().map(this::toDto).collect(Collectors.toList());
        }
        // Fall back to role-based permissions, mirroring the login/refresh behaviour.
        // Merge per module so that a user with multiple roles gets the union of their grants.
        User user = userRepo.findById(userId).orElse(null);
        if (user == null || user.getRoles() == null) {
            return new ArrayList<>();
        }
        java.util.Map<Long, PermissionDto> merged = new java.util.LinkedHashMap<>();
        for (Role role : user.getRoles()) {
            for (PermissionDto p : getPermissionsByRole(role.getId())) {
                merged.merge(p.getModuleId(), p, (existing, incoming) -> {
                    existing.setCreate(existing.isCreate() || incoming.isCreate());
                    existing.setUpdate(existing.isUpdate() || incoming.isUpdate());
                    existing.setView(existing.isView() || incoming.isView());
                    existing.setDelete(existing.isDelete() || incoming.isDelete());
                    return existing;
                });
            }
        }
        return new ArrayList<>(merged.values());
    }

    @Transactional(readOnly = true)
    public List<PermissionDto> getPermissionsByRole(Long idRole) {
        return permissionRepo.findAllByRoleId(idRole).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateUserPermissions(Long userId, List<ModulePermissionRequest> permissions) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        if (permissions == null) return;

        // Upsert each requested module permission
        Set<Long> requestedIds = permissions.stream()
                .map(ModulePermissionRequest::getModuleId)
                .collect(Collectors.toSet());

        for (ModulePermissionRequest mp : permissions) {
            Long moduleId = mp.getModuleId();
            Module module = moduleRepo.findById(moduleId)
                    .orElseThrow(() -> new RuntimeException("Module not found: " + moduleId));
            Permission existing = permissionRepo.findByModuleIdAndUserId(moduleId, userId);
            if (existing != null) {
                existing.setCreate(mp.isCreate());
                existing.setUpdate(mp.isUpdate());
                existing.setDelete(mp.isDelete());
                existing.setView(mp.isView());
                permissionRepo.save(existing);
            } else {
                Permission permission = new Permission();
                permission.setCreate(mp.isCreate());
                permission.setUpdate(mp.isUpdate());
                permission.setDelete(mp.isDelete());
                permission.setView(mp.isView());
                permission.setModule(module);
                permission.setUser(user);
                permissionRepo.save(permission);
            }
        }

        // Remove permissions for modules no longer in the list
        List<Permission> existing = permissionRepo.findAllByUserId(userId);
        for (Permission p : existing) {
            if (p.getModule() != null && !requestedIds.contains(p.getModule().getId())) {
                permissionRepo.delete(p);
            }
        }
    }




}
