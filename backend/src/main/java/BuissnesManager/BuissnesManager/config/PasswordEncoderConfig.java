package BuissnesManager.BuissnesManager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return bcrypt.encode(rawPassword);
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                if (rawPassword == null || encodedPassword == null) {
                    return false;
                }
                if (isBCrypt(encodedPassword)) {
                    return bcrypt.matches(rawPassword, encodedPassword);
                }
        
                byte[] a = rawPassword.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
                byte[] b = encodedPassword.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                return java.security.MessageDigest.isEqual(a, b);
            }

            @Override
            public boolean upgradeEncoding(String encodedPassword) {
                // Anything not already BCrypt-hashed should be upgraded.
     
                return encodedPassword != null && !isBCrypt(encodedPassword);
            }

            private boolean isBCrypt(String encoded) {
                return encoded.startsWith("$2a$")
                        || encoded.startsWith("$2b$")
                        || encoded.startsWith("$2y$");
            }
        };
    }
}