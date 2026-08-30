package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.Module;
import BuissnesManager.BuissnesManager.entity.ModuleTittle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ModuleTittleRepo extends JpaRepository<ModuleTittle,Long> {

    @Query("SELECT DISTINCT mt FROM ModuleTittle mt LEFT JOIN FETCH mt.modules")
    List<ModuleTittle> findAllWithModules();

    List<ModuleTittle> findAllByModulesPermissionsRoleId(Long idRole);

    ModuleTittle findByModules(Module modules);

}
