package BuissnesManager.BuissnesManager.security;

import BuissnesManager.BuissnesManager.entity.Entreprise;
import BuissnesManager.BuissnesManager.entity.User;
import BuissnesManager.BuissnesManager.repository.UserRepo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationUtilTest {

    @Mock private JWTUtil jwtUtil;
    @Mock private UserRepo userRepo;

    @InjectMocks
    private AuthorizationUtil authorizationUtil;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void adminCanOnlyAccessOwnEnterpriseUsingAuthenticatedUserFallback() {
        authenticate("company.admin", "ROLE_ADMIN");
        User user = new User();
        Entreprise entreprise = new Entreprise();
        entreprise.setId(10L);
        user.setEntreprise(entreprise);
        when(userRepo.findByUsername("company.admin")).thenReturn(Optional.of(user));

        assertTrue(authorizationUtil.canAccessEnterprise(10L));
        assertFalse(authorizationUtil.canAccessEnterprise(11L));
    }

    @Test
    void superAdminCanAccessAnyEnterpriseWithoutTenantMembership() {
        authenticate("platform.admin", "ROLE_SUPER_ADMIN");

        assertTrue(authorizationUtil.canAccessEnterprise(999L));
        assertTrue(authorizationUtil.isAdminOfEnterprise(999L));
    }

    @Test
    void unauthenticatedUserCannotAccessEnterprise() {
        assertFalse(authorizationUtil.canAccessEnterprise(10L));
        assertFalse(authorizationUtil.isAdminOfEnterprise(10L));
    }

    private void authenticate(String username, String authority) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        username,
                        "n/a",
                        List.of(new SimpleGrantedAuthority(authority))));
    }
}
