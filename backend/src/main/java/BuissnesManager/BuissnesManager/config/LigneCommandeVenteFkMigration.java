package BuissnesManager.BuissnesManager.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Fixes the foreign key constraint on ligne_commande_vente.article_id.
 * Previously the FK referenced the article table; it must reference the article_vente table
 * because LigneCommandeVente.articleVente is of type ArticleVente.
 * Runs at startup after Hibernate DDL update so it can safely modify existing constraints.
 */
@Component
@Order(2)
public class LigneCommandeVenteFkMigration implements CommandLineRunner {

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
            "WHERE fk.parent_object_id = OBJECT_ID('ligne_commande_vente') " +
            "AND c.name = 'article_id' " +
            "AND fk.referenced_object_id = OBJECT_ID('article')";

        try {
            List<String> badConstraints = jdbcTemplate.queryForList(findBadFkSql, String.class);
            for (String constraintName : badConstraints) {
                if (!constraintName.matches("[\\w]+")) {
                    System.out.println("LigneCommandeVenteFkMigration: skipping constraint with unexpected name: " + constraintName);
                    continue;
                }
                jdbcTemplate.execute("ALTER TABLE ligne_commande_vente DROP CONSTRAINT [" + constraintName + "]");
                System.out.println("LigneCommandeVenteFkMigration: dropped incorrect FK constraint '" + constraintName + "' (was referencing article instead of article_vente)");
            }
        } catch (Exception e) {
            System.out.println("LigneCommandeVenteFkMigration: could not check/drop old FK - " + e.getMessage());
        }
    }

    private void ensureCorrectForeignKey() {
        String findGoodFkSql =
            "SELECT COUNT(*) " +
            "FROM sys.foreign_keys fk " +
            "INNER JOIN sys.foreign_key_columns fkc ON fk.object_id = fkc.constraint_object_id " +
            "INNER JOIN sys.columns c ON fkc.parent_object_id = c.object_id AND fkc.parent_column_id = c.column_id " +
            "WHERE fk.parent_object_id = OBJECT_ID('ligne_commande_vente') " +
            "AND c.name = 'article_id' " +
            "AND fk.referenced_object_id = OBJECT_ID('article_vente')";

        try {
            Integer count = jdbcTemplate.queryForObject(findGoodFkSql, Integer.class);
            if (count == null || count == 0) {
                jdbcTemplate.execute(
                    "ALTER TABLE ligne_commande_vente ADD CONSTRAINT FK_lcv_article " +
                    "FOREIGN KEY (article_id) REFERENCES article_vente(id)"
                );
                System.out.println("LigneCommandeVenteFkMigration: added correct FK constraint FK_lcv_article -> article_vente(id)");
            }
        } catch (Exception e) {
            System.out.println("LigneCommandeVenteFkMigration: could not add correct FK - " + e.getMessage());
        }
    }
}
