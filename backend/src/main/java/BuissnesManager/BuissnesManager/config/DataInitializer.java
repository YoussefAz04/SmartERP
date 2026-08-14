package BuissnesManager.BuissnesManager.config;

import BuissnesManager.BuissnesManager.dto.UserRequest;
import BuissnesManager.BuissnesManager.entity.Role;
import BuissnesManager.BuissnesManager.entity.User;
import BuissnesManager.BuissnesManager.repository.UserRepo;
import BuissnesManager.BuissnesManager.service.RoleService;
import BuissnesManager.BuissnesManager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private UserRepo userRepo;


    @Override
    public void run(String... args) throws Exception {
        UserRequest userRequest = new UserRequest("admin", "admin@gmail.com","admin123","54575533","");
        try {
            userService.createUser(userRequest);
            System.out.println("Admin user created successfully.");
            Role role = new Role();
            role.setName("Super_admin");
            roleService.create(role);
            System.out.println("Role created successfully.");
            User user =userService.findByUsername("admin").orElse(null);
            if (user != null){
                userService.assignRoleToUser(user.getId(),
                        role.getId());
            }

        } catch (Exception e) {
            System.out.println("L'utilisateur admin existe déjà.");
        }


    }
}
