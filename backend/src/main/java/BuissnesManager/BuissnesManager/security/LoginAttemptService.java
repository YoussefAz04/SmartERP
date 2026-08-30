package BuissnesManager.BuissnesManager.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Applies a small, in-memory lockout window to repeated failed logins.
 * Entries are keyed by normalized username and remote address so one user does
 * not lock another user's account from a different location.
 */
@Service
public class LoginAttemptService {

    private final ConcurrentMap<String, Attempt> attempts = new ConcurrentHashMap<>();
    private final int maxAttempts;
    private final Duration lockDuration;
    private final Clock clock;
    private final AtomicInteger operationsUntilCleanup = new AtomicInteger(256);

    @Autowired
    public LoginAttemptService(
            @Value("${security.login.max-attempts:5}") int maxAttempts,
            @Value("${security.login.lock-duration-minutes:15}") long lockDurationMinutes) {
        this(maxAttempts, Duration.ofMinutes(lockDurationMinutes), Clock.systemUTC());
    }

    LoginAttemptService(int maxAttempts, Duration lockDuration, Clock clock) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }
        if (lockDuration.isZero() || lockDuration.isNegative()) {
            throw new IllegalArgumentException("lockDuration must be positive");
        }
        this.maxAttempts = maxAttempts;
        this.lockDuration = lockDuration;
        this.clock = clock;
    }

    public boolean isBlocked(String username, String remoteAddress) {
        String key = key(username, remoteAddress);
        Instant now = clock.instant();
        Attempt attempt = activeAttempt(key, now);
        return attempt != null && attempt.blockedUntil() != null && now.isBefore(attempt.blockedUntil());
    }

    public long retryAfterSeconds(String username, String remoteAddress) {
        Attempt attempt = activeAttempt(key(username, remoteAddress), clock.instant());
        if (attempt == null || attempt.blockedUntil() == null) {
            return 0;
        }
        long seconds = Duration.between(clock.instant(), attempt.blockedUntil()).getSeconds();
        return Math.max(1, seconds);
    }

    public void recordFailure(String username, String remoteAddress) {
        String key = key(username, remoteAddress);
        Instant now = clock.instant();

        if (operationsUntilCleanup.decrementAndGet() <= 0) {
            attempts.entrySet().removeIf(entry -> isExpired(entry.getValue(), now));
            operationsUntilCleanup.set(256);
        }

        attempts.compute(key, (ignored, previous) -> {
            int failures = 1;
            if (previous != null && !isExpired(previous, now)) {
                if (previous.blockedUntil() != null && now.isBefore(previous.blockedUntil())) {
                    return previous;
                }
                failures = previous.failures() + 1;
            }

            Instant blockedUntil = failures >= maxAttempts ? now.plus(lockDuration) : null;
            return new Attempt(failures, now, blockedUntil);
        });
    }

    public void recordSuccess(String username, String remoteAddress) {
        attempts.remove(key(username, remoteAddress));
    }

    private Attempt activeAttempt(String key, Instant now) {
        Attempt attempt = attempts.get(key);
        if (attempt != null && isExpired(attempt, now)) {
            attempts.remove(key, attempt);
            return null;
        }
        return attempt;
    }

    private boolean isExpired(Attempt attempt, Instant now) {
        Instant expiry = attempt.blockedUntil() != null
                ? attempt.blockedUntil()
                : attempt.lastFailure().plus(lockDuration);
        return !now.isBefore(expiry);
    }

    private String key(String username, String remoteAddress) {
        String normalizedUsername = username == null
                ? ""
                : username.trim().toLowerCase(Locale.ROOT);
        String normalizedAddress = remoteAddress == null || remoteAddress.isBlank()
                ? "unknown"
                : remoteAddress.trim();
        return normalizedUsername + '|' + normalizedAddress;
    }

    private record Attempt(int failures, Instant lastFailure, Instant blockedUntil) {
    }
}
