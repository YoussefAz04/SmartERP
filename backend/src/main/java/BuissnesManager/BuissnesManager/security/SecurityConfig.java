package BuissnesManager.BuissnesManager.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)  // ✅ ADD THIS for @PreAuthorize support
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired(required = false)
    private JWTFilter jwtFilter;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Allow frontend origin
        config.setAllowedOrigins(Collections.singletonList("http://localhost:4200"));

        // Allow all HTTP methods
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Allow all headers including Authorization
        config.setAllowedHeaders(Collections.singletonList("*"));

        // Expose Authorization header in response
        config.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));

        // Allow credentials (cookies, auth headers)
        config.setAllowCredentials(true);

        // Cache CORS preflight for 1 hour
        config.setMaxAge(3600L);

        logger.info("✅ CORS Configuration:");
        logger.info("   - Allowed Origins: http://localhost:4200");
        logger.info("   - Allowed Methods: GET, POST, PUT, DELETE, OPTIONS, PATCH");
        logger.info("   - Allowed Headers: *");
        logger.info("   - Allow Credentials: true");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("═══════════════════════════════════════════════════");
        logger.info("🔐 Configuring Spring Security Filter Chain");
        logger.info("═══════════════════════════════════════════════════");

        http
                // Disable CSRF for stateless API
                .csrf(csrf -> csrf.disable())

                // Enable CORS with our configuration
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - NO authentication required
                        .requestMatchers(
                                "/test/**",
                                "/auth/**",
                                "/entreprise/**",           // ✅ Allow all /entreprise endpoints
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/v3/api-docs/**",
                                "/api/**",
                                "/module/**",
                                "/departement/**",
                                "/permission/**",
                                "/clients/**",
                                "/document/**",
                                "/facture/**",
                                "/factures-vente/**"
                        ).permitAll()
                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )

                // Use stateless sessions (no cookies)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        // Add JWT filter BEFORE the UsernamePasswordAuthenticationFilter
        if (jwtFilter != null) {
            logger.info("✅ JWT Filter added to filter chain");
            http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        } else {
            logger.warn("⚠️  JWT Filter NOT found!");
        }

        logger.info("═══════════════════════════════════════════════════");
        logger.info("✅ Security Configuration Complete");
        logger.info("═══════════════════════════════════════════════════");

        return http.build();
    }

    @Bean
    AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder
                .userDetailsService(customUserDetailsService)
                .passwordEncoder(passwordEncoder);
        return authBuilder.build();
    }
}