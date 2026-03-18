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


        Module module = moduleRepo.findById(idModule).get();
        Role role = roleRepo.findById(idRole).get();
        permission.setModule(module);
        permission.setRole(role);
        roleRepo.save(role);
        return permissionRepo.save(permission);
    }

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





}
