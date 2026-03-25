package BuissnesManager.BuissnesManager.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired(required = false)
    private JWTFilter jwtFilter;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Bean
    @Order(0)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
<<<<<<< Updated upstream
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
=======
                .cors(cors -> cors.disable())
>>>>>>> Stashed changes
                .authorizeHttpRequests(auth -> auth
                        // ✅ Allow ALL HTTP methods for public endpoints
                        .requestMatchers(HttpMethod.GET, "/entreprise/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/entreprise/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/entreprise/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/entreprise/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/entreprise/**").permitAll()
                        
                        // Other public endpoints
                        .requestMatchers(
                                "/auth/**",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/v3/api-docs/**",
                                "/api/**",
                                "/module/**",
<<<<<<< Updated upstream
                                "/v3/api-docs/**",
=======
>>>>>>> Stashed changes
                                "/departement/**",
                                "/permission/**",
                                "/clients/**",
                                "/document/**",
                                "/facture/**",
                                "/factures-vente/**",
                                "/test/**"
                        ).permitAll()
<<<<<<< Updated upstream
                        .anyRequest().authenticated())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
=======
                        
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        if (jwtFilter != null) {
            http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        }
>>>>>>> Stashed changes

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder.userDetailsService(customUserDetailsService).passwordEncoder(passwordEncoder);
        return authBuilder.build();
    }
}