package BuissnesManager.BuissnesManager.repository;

import BuissnesManager.BuissnesManager.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface RoleRepo extends JpaRepository<Role,Long> {

    Optional<Role> findByName(String name);

    List<Role> findByEntrepriseIsNull();

    List<Role> findByEntrepriseId(Long entrepriseId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM user_roles WHERE role_id IN (SELECT id FROM roles WHERE entreprise_id = :entrepriseId)", nativeQuery = true)
    void deleteUserRolesByEntrepriseId(@Param("entrepriseId") Long entrepriseId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Role r WHERE r.entreprise.id = :entrepriseId")
    void deleteByEntrepriseId(@Param("entrepriseId") Long entrepriseId);
}
