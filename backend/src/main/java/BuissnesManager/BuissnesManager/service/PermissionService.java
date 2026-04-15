package BuissnesManager.BuissnesManager.service;


import BuissnesManager.BuissnesManager.dto.PermissionDto;
import BuissnesManager.BuissnesManager.entity.Module;
import BuissnesManager.BuissnesManager.entity.Permission;
import BuissnesManager.BuissnesManager.entity.Role;
import BuissnesManager.BuissnesManager.repository.ModuleRepo;
import BuissnesManager.BuissnesManager.repository.PermissionRepo;
import BuissnesManager.BuissnesManager.repository.RoleRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PermissionService {
    @Autowired
    private PermissionRepo permissionRepo;

    @Autowired
    private ModuleRepo moduleRepo ;

    @Autowired
    private RoleRepo roleRepo ;


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

    @Transactional(readOnly = true)
    public List<PermissionDto> getPermissionsByRole(Long idRole) {
        List<Permission> permissions = permissionRepo.findAllByRoleId(idRole);

        return permissions.stream().map(permission -> {
            PermissionDto dto = new PermissionDto();
            dto.setId(permission.getId());
            dto.setCreate(permission.isCreate());
            dto.setDelete(permission.isDelete());
            dto.setUpdate(permission.isUpdate());
            dto.setView(permission.isView());

            if (permission.getModule() != null) {
                dto.setModuleId(permission.getModule().getId());
                dto.setModuleTitle(permission.getModule().getTitle()); // Assure-toi que `getTitle()` existe
            }

            if (permission.getRole() != null) {
                dto.setRoleId(permission.getRole().getId());
            }

            return dto;
        }).collect(Collectors.toList());
    }
    @Transactional
    public void assignModulePermissionsToRole(Long roleId, List<Long> moduleIds) {
        Role role = roleRepo.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        for (Long moduleId : moduleIds) {
            Module module = moduleRepo.findById(moduleId)
                    .orElseThrow(() -> new RuntimeException("Module not found: " + moduleId));

            Permission existingPermission = permissionRepo.findByModuleIdAndRoleId(moduleId, roleId);
            if (existingPermission != null) {
                existingPermission.setCreate(true);
                existingPermission.setUpdate(true);
                existingPermission.setDelete(true);
                existingPermission.setView(true);
                permissionRepo.save(existingPermission);
            } else {
                Permission permission = new Permission();
                permission.setCreate(true);
                permission.setUpdate(true);
                permission.setDelete(true);
                permission.setView(true);
                permission.setModule(module);
                permission.setRole(role);
                permissionRepo.save(permission);
            }
        }
    }




}
