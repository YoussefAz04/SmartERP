package BuissnesManager.BuissnesManager.service;
import BuissnesManager.BuissnesManager.entity.Role;
import BuissnesManager.BuissnesManager.repository.RoleRepo;
import jakarta.transaction.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class RoleService {
    private final RoleRepo roleRepository;
    private final JdbcTemplate jdbcTemplate;

    public RoleService(RoleRepo roleRepository, JdbcTemplate jdbcTemplate) {
        this.roleRepository = roleRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public Role create(Role role) {
        role.setCreatedOn(LocalDate.now());
        return roleRepository.save(role);
    }

    public List<Role> getAll() {
        return roleRepository.findAll();
    }

    public List<Role> getAllGlobal() {
        return roleRepository.findByEntrepriseIsNull();
    }

    public List<Role> getAllByEnterprise(Long enterpriseId) {
        return roleRepository.findByEntrepriseId(enterpriseId);
    }

    public Optional<Role> getById(Long id) {
        return roleRepository.findById(id);
    }

    public Role update(Long id, Role newRole) {
        return roleRepository.findById(id)
                .map(role -> {
                    role.setCreatedOn(LocalDate.now());
                    role.setName(newRole.getName());
                    return roleRepository.save(role);
                }).orElseThrow(() -> new RuntimeException("Role not found"));
    }

    @Transactional

    public void delete(Long id) {
        
        jdbcTemplate.update("DELETE FROM permission WHERE role_id = ?", id);
        
        jdbcTemplate.update("DELETE FROM user_roles WHERE role_id = ?", id);
        
        roleRepository.deleteById(id);
    }
}