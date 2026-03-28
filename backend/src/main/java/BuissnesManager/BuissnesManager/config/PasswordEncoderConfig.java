package BuissnesManager.BuissnesManager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // ✅ Using NoOp (plain text) for testing
        // Change to BCryptPasswordEncoder() for production
        return NoOpPasswordEncoder.getInstance();
    }
}