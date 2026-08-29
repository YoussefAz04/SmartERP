package BuissnesManager.BuissnesManager.config;

import BuissnesManager.BuissnesManager.entity.Role;
import BuissnesManager.BuissnesManager.entity.RoleType;
import BuissnesManager.BuissnesManager.entity.User;
import BuissnesManager.BuissnesManager.repository.RoleRepo;
import BuissnesManager.BuissnesManager.repository.UserRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;

@Component
@Order(2)
public class DataInitializer implements CommandLineRunner {

    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap-admin.username:admin}")
    private String adminUsername;

    @Value("${app.bootstrap-admin.email:admin@gmail.com}")
    private String adminEmail;

    @Value("${app.bootstrap-admin.password:admin123}")
    private String adminPassword;

    public DataInitializer(UserRepo userRepo, RoleRepo roleRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Role superAdminRole = roleRepo.findByName("SUPER_ADMIN")
                .orElseGet(this::createSuperAdminRole);
        User admin = userRepo.findByUsername(adminUsername)
                .orElseGet(this::createBootstrapAdmin);

        if (admin.getRoles() == null) {
            admin.setRoles(new ArrayList<>());
        }

        boolean hasSuperAdminRole = admin.getRoles().stream()
                .anyMatch(role -> role.getRoleType() == RoleType.SUPER_ADMIN);
        if (!hasSuperAdminRole) {
            admin.getRoles().add(superAdminRole);
            userRepo.save(admin);
            System.out.println("Bootstrap admin repaired: SUPER_ADMIN role assigned to " + adminUsername);
        }
    }

    private Role createSuperAdminRole() {
        Role role = new Role();
        role.setName("SUPER_ADMIN");
        role.setRoleType(RoleType.SUPER_ADMIN);
        role.setCreatedOn(LocalDate.now());
        return roleRepo.save(role);
    }

    private User createBootstrapAdmin() {
        User admin = new User();
        admin.setUsername(adminUsername);
        admin.setEmail(adminEmail);
        admin.setPhone("");
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setEnabled(true);
        admin.setRoles(new ArrayList<>());
        User saved = userRepo.save(admin);
        System.out.println("Bootstrap admin created: " + adminUsername);
        return saved;
    }
}
