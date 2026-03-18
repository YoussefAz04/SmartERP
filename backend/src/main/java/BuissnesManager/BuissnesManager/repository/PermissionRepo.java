package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.Module;
import BuissnesManager.BuissnesManager.entity.Permission;
import jakarta.persistence.Persistence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PermissionRepo extends JpaRepository<Permission,Long> {
    List<Permission> findAllByRoleId(Long idRole);


    @Query("select p.module from Permission p where p.role.id = :idRole ")
    List<Module> getModuleByIdRole(@Param("idRole") Long idRole);


    Permission findByModuleIdAndRoleId(Long idModule,Long idRole);
}
