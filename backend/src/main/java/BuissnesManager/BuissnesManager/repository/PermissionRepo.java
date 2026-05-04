package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.Module;
import BuissnesManager.BuissnesManager.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PermissionRepo extends JpaRepository<Permission,Long> {
    List<Permission> findAllByRoleId(Long idRole);


    @Query("select p.module from Permission p where p.role.id = :idRole ")
    List<Module> getModuleByIdRole(@Param("idRole") Long idRole);


    Permission findByModuleIdAndRoleId(Long idModule,Long idRole);

    List<Permission> findAllByUserId(Long userId);

    Permission findByModuleIdAndUserId(Long moduleId, Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Permission p WHERE p.role.entreprise.id = :entrepriseId")
    void deleteByRoleEntrepriseId(@Param("entrepriseId") Long entrepriseId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Permission p WHERE p.user.entreprise.id = :entrepriseId")
    void deleteByUserEntrepriseId(@Param("entrepriseId") Long entrepriseId);
}
