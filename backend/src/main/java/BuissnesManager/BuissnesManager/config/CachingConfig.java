package BuissnesManager.BuissnesManager.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Enables Spring's declarative caching infrastructure. {@code @Cacheable}
 * annotations on the AI services are keyed by {@code (entrepriseId,
 * dayOfYear)} so heavy ML computations only run once per enterprise per day
 * — training data changes slowly on business timescales and the repeated
 * back-end invocations the Angular dashboard triggers on every tab switch
 * would otherwise retrain the same models dozens of times per day.
 *
 * <p>Uses the default {@link org.springframework.cache.concurrent.ConcurrentMapCache}
 * implementation (provided by {@code spring-boot-starter-cache} when no
 * dedicated cache provider is on the classpath). That's the right choice
 * here: the entries are tiny (one DTO per day per enterprise), the process
 * is the only cache consumer, and a simple HashMap-backed cache avoids any
 * external dependency like Redis or Caffeine.
 */
@Configuration
@EnableCaching
public class CachingConfig {
}
