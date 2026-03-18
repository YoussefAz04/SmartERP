package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.Module;
import BuissnesManager.BuissnesManager.entity.ModuleTittle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModuleRepo extends JpaRepository<Module,Long> {


}
