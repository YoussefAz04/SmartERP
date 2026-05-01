package BuissnesManager.BuissnesManager.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Synchronizes the Hibernate-managed permission_seq sequence with the actual
 * maximum id in the permission table.
 *
 * The sequence can fall behind the real data when the schema is recreated
 * (ddl-auto=update) while rows already exist, or when rows are inserted via
 * other means.  If the sequence returns a value that is already taken, every
 * INSERT into the permission table will fail with a PK violation, which is the
 * bug that appears as "Cannot insert duplicate key in object 'dbo.permission'".
 *
 * This runner resets the sequence on every startup so it always starts above
 * the current maximum id, preventing the collision.
 */
@Component
@Order(20)
public class PermissionSequenceSynchronizer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        syncPermissionSequence();
    }

    private void syncPermissionSequence() {
        try {
            Long maxId = jdbcTemplate.queryForObject(
                    "SELECT COALESCE(MAX(id), 0) FROM [permission]", Long.class);
            if (maxId == null) maxId = 0L;
            long nextVal = maxId + 1;
            // nextVal is a primitive long, so concatenation here is safe from injection
            jdbcTemplate.execute("ALTER SEQUENCE permission_seq RESTART WITH " + nextVal);
            System.out.println("PermissionSequenceSynchronizer: permission_seq reset to " + nextVal);
        } catch (Exception e) {
            System.out.println("PermissionSequenceSynchronizer: could not synchronize permission_seq - " + e.getMessage());
        }
    }
}
