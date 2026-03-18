package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.entity.Role;
import BuissnesManager.BuissnesManager.repository.RoleRepo;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class RoleService {

    private final RoleRepo roleRepository;

    public RoleService(RoleRepo roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Role create(Role role) {
        role.setCreatedOn(LocalDate.now());
        return roleRepository.save(role);
    }

    public List<Role> getAll() {
        return roleRepository.findAll();
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

    public void delete(Long id) {
        roleRepository.deleteById(id);
    }
}