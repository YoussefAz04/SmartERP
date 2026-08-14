package BuissnesManager.BuissnesManager.security;

import BuissnesManager.BuissnesManager.entity.RoleType;
import BuissnesManager.BuissnesManager.repository.UserRepo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Spring-managed component used in @PreAuthorize SpEL expressions for
 * enterprise-scoped and role-based authorization checks.
 *
 * Usage in controllers:
 *   @PreAuthorize("@authorizationUtil.isSuperAdmin() or @authorizationUtil.isAdminOfEnterprise(#entrepriseId)")
 */
@Component("authorizationUtil")
public class AuthorizationUtil {

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private UserRepo userRepo;

    // ----------------------------------------------------------------
    // Role checks (operate on the current Spring Security context)
    // ----------------------------------------------------------------

    public boolean isSuperAdmin() {
        return hasAuthority("ROLE_" + RoleType.SUPER_ADMIN.name());
    }

    public boolean isAdmin() {
        return hasAuthority("ROLE_" + RoleType.ADMIN.name());
    }

    public boolean isEmployee() {
        return hasAuthority("ROLE_" + RoleType.EMPLOYEE.name());
    }

    /** True for SUPER_ADMIN or ADMIN. */
    public boolean isAdminOrAbove() {
        return isSuperAdmin() || isAdmin();
    }

    // ----------------------------------------------------------------
    // Enterprise-scoped checks
    // ----------------------------------------------------------------

    /**
     * True if the authenticated user is a SUPER_ADMIN or belongs to the given enterprise.
     */
    public boolean canAccessEnterprise(Long enterpriseId) {
        if (isSuperAdmin()) {
            return true;
        }
        Long tokenEnterpriseId = getCurrentEnterpriseId();
        return tokenEnterpriseId != null && tokenEnterpriseId.equals(enterpriseId);
    }

    /**
     * True if the authenticated user is a SUPER_ADMIN or is an ADMIN of the given enterprise.
     */
    public boolean isAdminOfEnterprise(Long enterpriseId) {
        if (isSuperAdmin()) {
            return true;
        }
        if (!isAdmin()) {
            return false;
        }
        Long tokenEnterpriseId = getCurrentEnterpriseId();
        return tokenEnterpriseId != null && tokenEnterpriseId.equals(enterpriseId);
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    private boolean hasAuthority(String authority) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals(authority));
    }

    public Long getCurrentEnterpriseId() {
        try {
            Long contextEnterpriseId = JWTFilter.getCurrentEnterpriseId();
            if (contextEnterpriseId != null) {
                return contextEnterpriseId;
            }

            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    Long tokenEnterpriseId = jwtUtil.getEntrepriseIdFromToken(authHeader.substring(7));
                    if (tokenEnterpriseId != null) {
                        return tokenEnterpriseId;
                    }
                }
            }

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                return null;
            }
            return userRepo.findByUsername(auth.getName())
                    .map(user -> user.getEntreprise() != null ? user.getEntreprise().getId() : null)
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
}
