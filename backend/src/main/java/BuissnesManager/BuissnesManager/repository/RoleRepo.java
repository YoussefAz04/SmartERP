package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface RoleRepo extends JpaRepository<Role,Long> {

    Optional<Role> findByName(String name);

    List<Role> findByEntrepriseIsNull();

    List<Role> findByEntrepriseId(Long entrepriseId);

    @Transactional
    void deleteByEntrepriseId(Long entrepriseId);
}
