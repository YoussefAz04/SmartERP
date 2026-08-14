package BuissnesManager.BuissnesManager.config;

import BuissnesManager.BuissnesManager.entity.SavHistoryType;
import BuissnesManager.BuissnesManager.entity.SavStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@Order(5)
public class SavStatusCheckMigration implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(SavStatusCheckMigration.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        refreshSafely(
                "sav_ticket",
                "status",
                "CHK_sav_ticket_status",
                SavStatus.values());
        refreshSafely(
                "sav_ticket_history",
                "type",
                "CHK_sav_ticket_history_type",
                SavHistoryType.values());
    }

    private void refreshSafely(
            String table,
            String column,
            String constraintName,
            Enum<?>[] enumValues) {
        try {
            refreshEnumConstraint(table, column, constraintName, enumValues);
            logger.info("SAV CHECK constraint {} refreshed with {} values", constraintName, enumValues.length);
        } catch (Exception e) {
            logger.warn("Could not refresh SAV CHECK constraint {}: {}", constraintName, e.getMessage());
        }
    }

    private void refreshEnumConstraint(
            String table,
            String column,
            String constraintName,
            Enum<?>[] enumValues) {
        validateIdentifier(table);
        validateIdentifier(column);
        validateIdentifier(constraintName);

        Integer tableExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys.tables WHERE object_id = OBJECT_ID(N'dbo." + table + "')",
                Integer.class);
        if (tableExists == null || tableExists == 0) {
            logger.info("Skipping SAV CHECK constraint {} because table {} does not exist", constraintName, table);
            return;
        }

        var constraints = jdbcTemplate.queryForList(
                "SELECT cc.name, cc.definition, " +
                "COL_NAME(cc.parent_object_id, cc.parent_column_id) AS column_name " +
                "FROM sys.check_constraints cc " +
                "WHERE cc.parent_object_id = OBJECT_ID(N'dbo." + table + "')");
        Pattern columnReference = Pattern.compile(
                "(?i)(?<![A-Za-z0-9_])\\[?" + Pattern.quote(column) + "\\]?(?![A-Za-z0-9_])");

        for (var row : constraints) {
            String name = (String) row.get("name");
            String definition = (String) row.get("definition");
            String constrainedColumn = (String) row.get("column_name");
            boolean checksColumn = column.equalsIgnoreCase(constrainedColumn)
                    || definition != null && columnReference.matcher(definition).find();
            if (checksColumn && name != null && name.matches("[A-Za-z_][A-Za-z0-9_]*")) {
                jdbcTemplate.execute("ALTER TABLE [" + table + "] DROP CONSTRAINT [" + name + "]");
            }
        }

        String values = Arrays.stream(enumValues)
                .map(Enum::name)
                .map(value -> "'" + value + "'")
                .collect(Collectors.joining(","));
        jdbcTemplate.execute("ALTER TABLE [" + table + "] WITH CHECK " +
                "ADD CONSTRAINT [" + constraintName + "] CHECK ([" + column + "] IN (" + values + "))");
    }

    private void validateIdentifier(String identifier) {
        if (identifier == null || !identifier.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            throw new IllegalArgumentException("Invalid SQL identifier");
        }
    }
}
