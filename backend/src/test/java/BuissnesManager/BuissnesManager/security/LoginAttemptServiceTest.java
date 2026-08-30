package BuissnesManager.BuissnesManager.security;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class LoginAttemptServiceTest {

    private static final String USERNAME = "Admin.Soutenance";
    private static final String ADDRESS = "127.0.0.1";

    @Test
    void blocksAfterConfiguredNumberOfFailures() {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-30T10:00:00Z"));
        LoginAttemptService service = new LoginAttemptService(3, Duration.ofMinutes(15), clock);

        service.recordFailure(USERNAME, ADDRESS);
        service.recordFailure(USERNAME, ADDRESS);
        assertThat(service.isBlocked(USERNAME, ADDRESS)).isFalse();

        service.recordFailure(USERNAME, ADDRESS);

        assertThat(service.isBlocked(" admin.soutenance ", ADDRESS)).isTrue();
        assertThat(service.retryAfterSeconds(USERNAME, ADDRESS)).isEqualTo(900);
    }

    @Test
    void successfulLoginClearsPreviousFailures() {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-30T10:00:00Z"));
        LoginAttemptService service = new LoginAttemptService(2, Duration.ofMinutes(15), clock);

        service.recordFailure(USERNAME, ADDRESS);
        service.recordSuccess(USERNAME, ADDRESS);
        service.recordFailure(USERNAME, ADDRESS);

        assertThat(service.isBlocked(USERNAME, ADDRESS)).isFalse();
    }

    @Test
    void lockExpiresAndAllowsACompletelyNewWindow() {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-30T10:00:00Z"));
        LoginAttemptService service = new LoginAttemptService(2, Duration.ofMinutes(15), clock);

        service.recordFailure(USERNAME, ADDRESS);
        service.recordFailure(USERNAME, ADDRESS);
        clock.advance(Duration.ofMinutes(15));

        assertThat(service.isBlocked(USERNAME, ADDRESS)).isFalse();
        service.recordFailure(USERNAME, ADDRESS);
        assertThat(service.isBlocked(USERNAME, ADDRESS)).isFalse();
    }

    @Test
    void attemptsFromDifferentAddressesAreIndependent() {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-30T10:00:00Z"));
        LoginAttemptService service = new LoginAttemptService(2, Duration.ofMinutes(15), clock);

        service.recordFailure(USERNAME, ADDRESS);
        service.recordFailure(USERNAME, ADDRESS);

        assertThat(service.isBlocked(USERNAME, ADDRESS)).isTrue();
        assertThat(service.isBlocked(USERNAME, "10.0.0.2")).isFalse();
    }

    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
