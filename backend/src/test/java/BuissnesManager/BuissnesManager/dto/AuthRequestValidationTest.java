package BuissnesManager.BuissnesManager.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void blankCredentialsAreRejected() {
        AuthRequest request = new AuthRequest();
        request.setUsername(" ");
        request.setPassword("");

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactlyInAnyOrder("username", "password");
    }

    @Test
    void populatedCredentialsAreAccepted() {
        AuthRequest request = new AuthRequest();
        request.setUsername("admin.soutenance");
        request.setPassword("1234");

        assertThat(validator.validate(request)).isEmpty();
    }
}
