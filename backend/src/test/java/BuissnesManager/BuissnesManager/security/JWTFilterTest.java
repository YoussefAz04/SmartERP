package BuissnesManager.BuissnesManager.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JWTFilterTest {

    private final JWTUtil jwtUtil = mock(JWTUtil.class);
    private final JWTFilter filter = new JWTFilter(jwtUtil);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void healthEndpointSkipsJwtProcessing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isSameAs(request);
        verifyNoInteractions(jwtUtil);
    }

    @Test
    void invalidTokenReturnsJsonUnauthorizedResponse() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/clients/getAllClients");
        request.addHeader("Authorization", "Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(jwtUtil.validateAndExtractClaims("invalid-token"))
                .thenThrow(new MalformedJwtException("invalid token"));

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).startsWith("application/json");
        assertThat(response.getContentAsString()).contains("UNAUTHORIZED");
    }

    @Test
    void validTokenSetsAuthenticationAndEnterpriseContext() throws Exception {
        Claims claims = Jwts.claims();
        claims.setSubject("admin.soutenance");
        claims.put("roles", List.of("ADMIN"));
        claims.put("enterpriseId", 9901);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/clients/getAllClients");
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        when(jwtUtil.validateAndExtractClaims("valid-token")).thenReturn(claims);

        filter.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isSameAs(request);
        assertThat(request.getAttribute("enterpriseId")).isEqualTo(9901L);
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
                .isEqualTo("admin.soutenance");
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
        assertThat(JWTFilter.getCurrentEnterpriseId()).isNull();
    }
}
