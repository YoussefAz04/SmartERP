package BuissnesManager.BuissnesManager.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * One-time migration that repairs the montant columns on {@code commande_vente} and
 * {@code commande_achat}.
 *
 * <p>Historically these entities declared {@code BigDecimal montantHT/montantTVA/montantTTC}
 * without explicit {@code @Column(name=...)} annotations. Spring's default
 * {@code CamelCaseToUnderscoresNamingStrategy} maps those fields to
 * {@code montantht/montanttva/montantttc} (no underscore between consecutive uppercase
 * letters) while the authoritative schema uses {@code montant_ht/montant_tva/montant_ttc}.
 * With {@code ddl-auto=update}, Hibernate silently created the lowercase-no-underscore
 * columns alongside the proper ones, causing seeded amounts (written to the schema
 * columns) to be invisible to the application and causing UI-created commandes to be
 * written only to the orphan columns.</p>
 *
 * <p>After adding the explicit {@code @Column(name="montant_ht")} etc. annotations, this
 * migration:</p>
 * <ul>
 *     <li>copies any non-null values from the orphan columns into the proper columns
 *         when the proper columns are NULL (preserves previously-created data);</li>
 *     <li>drops the orphan columns so Hibernate cannot silently recreate them.</li>
 * </ul>
 *
 * <p>The migration is idempotent: once the orphan columns have been dropped the
 * subsequent runs are no-ops.</p>
 */
@Component
@Order(3)
public class CommandeMontantColumnMigration implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(CommandeMontantColumnMigration.class);

    private static final String[] TABLES = { "commande_vente", "commande_achat" };
    private static final String[][] COLUMN_PAIRS = {
        // { orphan (no underscore), canonical (with underscore) }
        { "montantht",  "montant_ht"  },
        { "montanttva", "montant_tva" },
        { "montantttc", "montant_ttc" }
    };

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        for (String table : TABLES) {
            for (String[] pair : COLUMN_PAIRS) {
                repairColumn(table, pair[0], pair[1]);
            }
        }
    }

    private void repairColumn(String table, String orphanColumn, String canonicalColumn) {
        // Defense in depth: even though the names come from private constants, validate
        // them against a strict identifier whitelist before interpolating into DDL/DML.
        if (!isSafeIdentifier(table) || !isSafeIdentifier(orphanColumn) || !isSafeIdentifier(canonicalColumn)) {
            logger.warn("CommandeMontantColumnMigration: refusing unsafe identifier ({} / {} / {})",
                    table, orphanColumn, canonicalColumn);
            return;
        }

        try {
            boolean orphanExists = columnExists(table, orphanColumn);
            boolean canonicalExists = columnExists(table, canonicalColumn);

            if (!orphanExists) {
                // Nothing to do - either the migration already ran or Hibernate never
                // created the orphan column (happens on fresh databases).
                return;
            }

            if (!canonicalExists) {
                // The canonical column does not exist yet. With ddl-auto=update Hibernate
                // will create it on startup thanks to the @Column(name=...) annotations,
                // so our CommandLineRunner (@Order(3)) will run after it. If it is still
                // missing here we bail out safely.
                logger.warn("CommandeMontantColumnMigration: canonical column {}.{} missing, skipping repair",
                        table, canonicalColumn);
                return;
            }

            // Backfill: copy values from orphan → canonical only when canonical is NULL
            // and orphan holds a real value. This preserves any pre-existing good data.
            int updated = jdbcTemplate.update(
                "UPDATE [" + table + "] SET [" + canonicalColumn + "] = [" + orphanColumn + "] " +
                "WHERE [" + canonicalColumn + "] IS NULL AND [" + orphanColumn + "] IS NOT NULL"
            );
            if (updated > 0) {
                logger.info("CommandeMontantColumnMigration: copied {} value(s) from {}.{} to {}.{}",
                        updated, table, orphanColumn, table, canonicalColumn);
            }

            // Drop the orphan column so Hibernate cannot drift back to it.
            jdbcTemplate.execute("ALTER TABLE [" + table + "] DROP COLUMN [" + orphanColumn + "]");
            logger.info("CommandeMontantColumnMigration: dropped orphan column {}.{}", table, orphanColumn);
        } catch (Exception e) {
            logger.warn("CommandeMontantColumnMigration: could not repair {}.{} ({}): {}",
                    table, orphanColumn, canonicalColumn, e.getMessage());
        }
    }

    private boolean columnExists(String table, String column) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM sys.columns " +
            "WHERE object_id = OBJECT_ID(?) AND name = ?",
            Integer.class, table, column
        );
        return count != null && count > 0;
    }

    private static boolean isSafeIdentifier(String identifier) {
        // Plain SQL identifier: letters, digits and underscore only, not starting with a digit.
        return identifier != null && identifier.matches("[A-Za-z_][A-Za-z0-9_]*");
    }
}
