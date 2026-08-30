package BuissnesManager.BuissnesManager.config;

import BuissnesManager.BuissnesManager.entity.TypeMouvement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** Keeps SQL Server CHECK constraints aligned with finance and stock workflow values. */
@Component
@Order(6)
public class FinancialCheckConstraintMigration implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(FinancialCheckConstraintMigration.class);
    private static final List<String> INVOICE_STATUSES = List.of(
            "EN_ATTENTE", "PARTIELLEMENT_PAYEE", "PAYEE", "CREDITEE", "ANNULEE");

    private final JdbcTemplate jdbcTemplate;

    public FinancialCheckConstraintMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        refresh("facture_de_vente", "statut", "CHK_facture_de_vente_statut", INVOICE_STATUSES);
        refresh("facture_achat", "statut", "CHK_facture_achat_statut", INVOICE_STATUSES);
        refresh("stock_mouvement", "type_mouvement", "CHK_stock_mouvement_type",
                Arrays.stream(TypeMouvement.values()).map(Enum::name).toList());
        backfillInvoiceFinanceFields();
    }

    private void backfillInvoiceFinanceFields() {
        try {
            jdbcTemplate.update("""
                    UPDATE facture_de_vente
                    SET date_echeance = DATEADD(day, 30, TRY_CONVERT(date, date_achat))
                    WHERE date_echeance IS NULL AND TRY_CONVERT(date, date_achat) IS NOT NULL
                    """);
            jdbcTemplate.update("""
                    UPDATE facture_achat
                    SET date_echeance = DATEADD(day, 30, TRY_CONVERT(date, date_achat))
                    WHERE date_echeance IS NULL AND TRY_CONVERT(date, date_achat) IS NOT NULL
                    """);
            jdbcTemplate.update("""
                    UPDATE facture_de_vente
                    SET montant_paye = CASE WHEN statut = 'PAYEE' THEN COALESCE(montant_ttc, 0) ELSE 0 END,
                        solde_restant = CASE WHEN statut = 'PAYEE' THEN 0 ELSE COALESCE(montant_ttc, 0) END
                    WHERE montant_paye IS NULL OR solde_restant IS NULL
                    """);
            jdbcTemplate.update("""
                    UPDATE facture_achat
                    SET montant_paye = CASE WHEN statut = 'PAYEE' THEN COALESCE(montantttc, 0) ELSE 0 END,
                        solde_restant = CASE WHEN statut = 'PAYEE' THEN 0 ELSE COALESCE(montantttc, 0) END
                    WHERE montant_paye IS NULL OR solde_restant IS NULL
                    """);
            logger.info("Existing invoice due dates and balances backfilled where missing");
        } catch (Exception e) {
            logger.warn("Could not backfill invoice finance fields: {}", e.getMessage());
        }
    }

    private void refresh(String table, String column, String constraintName, List<String> values) {
        validateIdentifier(table);
        validateIdentifier(column);
        validateIdentifier(constraintName);
        try {
            Integer exists = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM sys.tables WHERE object_id = OBJECT_ID(N'dbo." + table + "')",
                    Integer.class);
            if (exists == null || exists == 0) return;

            Pattern reference = Pattern.compile(
                    "(?i)(?<![A-Za-z0-9_])\\[?" + Pattern.quote(column) + "\\]?(?![A-Za-z0-9_])");
            for (var row : jdbcTemplate.queryForList(
                    "SELECT cc.name, cc.definition, "
                            + "COL_NAME(cc.parent_object_id, cc.parent_column_id) AS column_name "
                            + "FROM sys.check_constraints cc "
                            + "WHERE cc.parent_object_id = OBJECT_ID(N'dbo." + table + "')")) {
                String name = (String) row.get("name");
                String definition = (String) row.get("definition");
                String constrainedColumn = (String) row.get("column_name");
                if ((column.equalsIgnoreCase(constrainedColumn)
                        || definition != null && reference.matcher(definition).find())
                        && isSafeIdentifier(name)) {
                    jdbcTemplate.execute("ALTER TABLE [" + table + "] DROP CONSTRAINT [" + name + "]");
                }
            }

            String allowed = values.stream()
                    .filter(FinancialCheckConstraintMigration::isSafeValue)
                    .map(value -> "'" + value + "'")
                    .collect(Collectors.joining(","));
            jdbcTemplate.execute("ALTER TABLE [" + table + "] WITH CHECK ADD CONSTRAINT ["
                    + constraintName + "] CHECK ([" + column + "] IN (" + allowed + "))");
            logger.info("Financial CHECK constraint {} refreshed", constraintName);
        } catch (Exception e) {
            logger.warn("Could not refresh financial CHECK constraint {}: {}", constraintName, e.getMessage());
        }
    }

    private void validateIdentifier(String value) {
        if (!isSafeIdentifier(value)) throw new IllegalArgumentException("Invalid SQL identifier");
    }

    private static boolean isSafeIdentifier(String value) {
        return value != null && value.matches("[A-Za-z_][A-Za-z0-9_]*");
    }

    private static boolean isSafeValue(String value) {
        return value != null && value.matches("[A-Z_]+") && !value.isBlank();
    }
}
