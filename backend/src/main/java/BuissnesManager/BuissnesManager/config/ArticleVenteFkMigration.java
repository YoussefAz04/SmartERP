package BuissnesManager.BuissnesManager.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Fixes the foreign key constraint on article_vente.article_achat_id.
 * Previously the FK referenced the article_achat table; it must reference the article table.
 * Runs at startup after Hibernate DDL update so it can safely modify existing constraints.
 */
@Component
@Order(1)
public class ArticleVenteFkMigration implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        dropIncorrectForeignKey();
        ensureCorrectForeignKey();
    }

    private void dropIncorrectForeignKey() {
        String findBadFkSql =
            "SELECT fk.name " +
            "FROM sys.foreign_keys fk " +
            "INNER JOIN sys.foreign_key_columns fkc ON fk.object_id = fkc.constraint_object_id " +
            "INNER JOIN sys.columns c ON fkc.parent_object_id = c.object_id AND fkc.parent_column_id = c.column_id " +
            "WHERE fk.parent_object_id = OBJECT_ID('article_vente') " +
            "AND c.name = 'article_achat_id' " +
            "AND fk.referenced_object_id = OBJECT_ID('article_achat')";

        try {
            List<String> badConstraints = jdbcTemplate.queryForList(findBadFkSql, String.class);
            for (String constraintName : badConstraints) {
                if (!constraintName.matches("[\\w]+")) {
                    System.out.println("ArticleVenteFkMigration: skipping constraint with unexpected name: " + constraintName);
                    continue;
                }
                jdbcTemplate.execute("ALTER TABLE article_vente DROP CONSTRAINT [" + constraintName + "]");
                System.out.println("ArticleVenteFkMigration: dropped incorrect FK constraint '" + constraintName + "' (was referencing article_achat instead of article)");
            }
        } catch (Exception e) {
            System.out.println("ArticleVenteFkMigration: could not check/drop old FK - " + e.getMessage());
        }
    }

    private void ensureCorrectForeignKey() {
        String findGoodFkSql =
            "SELECT COUNT(*) " +
            "FROM sys.foreign_keys fk " +
            "INNER JOIN sys.foreign_key_columns fkc ON fk.object_id = fkc.constraint_object_id " +
            "INNER JOIN sys.columns c ON fkc.parent_object_id = c.object_id AND fkc.parent_column_id = c.column_id " +
            "WHERE fk.parent_object_id = OBJECT_ID('article_vente') " +
            "AND c.name = 'article_achat_id' " +
            "AND fk.referenced_object_id = OBJECT_ID('article')";

        try {
            Integer count = jdbcTemplate.queryForObject(findGoodFkSql, Integer.class);
            if (count == null || count == 0) {
                jdbcTemplate.execute(
                    "ALTER TABLE article_vente ADD CONSTRAINT FK_article_vente_article " +
                    "FOREIGN KEY (article_achat_id) REFERENCES article(id)"
                );
                System.out.println("ArticleVenteFkMigration: added correct FK constraint FK_article_vente_article -> article(id)");
            }
        } catch (Exception e) {
            System.out.println("ArticleVenteFkMigration: could not add correct FK - " + e.getMessage());
        }
    }
}
