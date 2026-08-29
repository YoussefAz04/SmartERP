package BuissnesManager.BuissnesManager.config;

import BuissnesManager.BuissnesManager.entity.Role;
import BuissnesManager.BuissnesManager.entity.RoleType;
import BuissnesManager.BuissnesManager.entity.User;
import BuissnesManager.BuissnesManager.repository.RoleRepo;
import BuissnesManager.BuissnesManager.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private RoleRepo roleRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    private DataInitializer initializer;

    @BeforeEach
    void setUp() {
        initializer = new DataInitializer(userRepo, roleRepo, passwordEncoder);
        ReflectionTestUtils.setField(initializer, "adminUsername", "admin");
        ReflectionTestUtils.setField(initializer, "adminEmail", "admin@gmail.com");
        ReflectionTestUtils.setField(initializer, "adminPassword", "admin123");
    }

    @Test
    void repairsExistingAdminWithoutRole() {
        Role superAdmin = superAdminRole();
        User admin = new User();
        admin.setUsername("admin");
        admin.setRoles(new ArrayList<>());

        when(roleRepo.findByName("SUPER_ADMIN")).thenReturn(Optional.of(superAdmin));
        when(userRepo.findByUsername("admin")).thenReturn(Optional.of(admin));

        initializer.run();

        assertEquals(1, admin.getRoles().size());
        assertEquals(RoleType.SUPER_ADMIN, admin.getRoles().get(0).getRoleType());
        verify(userRepo).save(admin);
    }

    @Test
    void createsUsableBootstrapAdminWhenDatabaseIsEmpty() {
        Role superAdmin = superAdminRole();
        when(roleRepo.findByName("SUPER_ADMIN")).thenReturn(Optional.of(superAdmin));
        when(userRepo.findByUsername("admin")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("admin123")).thenReturn("bcrypt-password");
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        initializer.run();

        var userCaptor = org.mockito.ArgumentCaptor.forClass(User.class);
        verify(userRepo, org.mockito.Mockito.times(2)).save(userCaptor.capture());
        User created = userCaptor.getValue();
        assertEquals("admin", created.getUsername());
        assertEquals("bcrypt-password", created.getPassword());
        assertTrue(created.isEnabled());
        assertEquals(1, created.getRoles().size());
    }

    private Role superAdminRole() {
        Role role = new Role();
        role.setName("SUPER_ADMIN");
        role.setRoleType(RoleType.SUPER_ADMIN);
        return role;
    }
}
