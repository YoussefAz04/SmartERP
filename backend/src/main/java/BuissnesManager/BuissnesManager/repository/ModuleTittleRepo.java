package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.Module;
import BuissnesManager.BuissnesManager.entity.ModuleTittle;
import BuissnesManager.BuissnesManager.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModuleTittleRepo extends JpaRepository<ModuleTittle,Long> {


    List<ModuleTittle> findAllByModulesPermissionsRoleId(Long idRole);

    ModuleTittle findByModules(Module modules);

}
