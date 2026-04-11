package BuissnesManager.BuissnesManager.config;

import BuissnesManager.BuissnesManager.entity.Role;
import BuissnesManager.BuissnesManager.entity.RoleType;
import BuissnesManager.BuissnesManager.repository.RoleRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class RoleInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepo roleRepo;

    @Override
    public void run(String... args) {
        ensureRole("SUPER_ADMIN", RoleType.SUPER_ADMIN);
        ensureRole("ADMIN", RoleType.ADMIN);
        ensureRole("EMPLOYEE", RoleType.EMPLOYEE);
    }

    private void ensureRole(String name, RoleType type) {
        roleRepo.findByName(name).ifPresentOrElse(
            role -> {
                if (role.getRoleType() == null) {
                    role.setRoleType(type);
                    roleRepo.save(role);
                }
            },
            () -> {
                Role role = new Role();
                role.setName(name);
                role.setRoleType(type);
                role.setCreatedOn(LocalDate.now());
                roleRepo.save(role);
            }
        );
    }
}
