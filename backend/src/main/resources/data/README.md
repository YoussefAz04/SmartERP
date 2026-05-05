# Testentreprise (id=15) seed data

This folder contains a synthetic but realistic ERP dataset for the enterprise
`testentreprise` (`entreprise_id = 15`), generated to improve sales forecasting
on the existing AI/ML pipeline (`MlForecastEngine`, `AnalyticService`).

## Files

| File | Purpose |
| ---- | ------- |
| `generate_testentreprise_seed.py` | Reproducible Python generator (seeded RNG). |
| `testentreprise_seed.sql`         | Ready-to-import SQL Server INSERTs (uses `SET IDENTITY_INSERT`). |
| `testentreprise_seed.json`        | Same dataset as JSON for inspection / data-science use. |

## Dataset shape

- Currency: **TND**, country context: **Tunisia**, VAT: **19%**.
- Entreprise id: **15** (testentreprise).
- 12 product categories, 6 measures, 12 suppliers, 4 client categories,
  30 clients (12 B2B + 18 B2C), 72 articles (with mirrored `article_vente`).
- ~8 months of data (Sept 2025 → Apr 2026):
  - 49 purchase orders / 187 lines.
  - 150 sales orders / 599 lines.
- Seasonality: summer peak (Jun–Aug), Ramadan boost (mid-Feb → mid-Mar 2026),
  back-to-school (Sep) and end-of-year (Dec) bumps. Purchases lead sales by
  ~1 month. Modest +2%/month growth trend with random spike/drop noise.
- 3 best-selling and 3 low-performing products are tagged in the JSON
  (`performance` field) and appear in the `-- Counts:` SQL footer.

## Usage

### Apply the SQL (SQL Server)

Make sure `entreprise.id = 15` exists, then run:

```sql
-- Optional safety
BEGIN TRANSACTION;
:r backend/src/main/resources/data/testentreprise_seed.sql
COMMIT TRANSACTION;
```

The script uses fixed IDs starting at 1500 (rows) and 5000 (order lines) to
avoid clashing with existing data; adjust the `OFF_*` constants in the
generator and re-run if those ranges are not free.

### Re-generate

```bash
python3 backend/src/main/resources/data/generate_testentreprise_seed.py
```

The RNG seed is fixed (`random.seed(20260505)`), so output is deterministic.

## Schema notes

The generator targets the JPA entities in
`backend/src/main/java/BuissnesManager/BuissnesManager/entity/`:

- `Article` → `article` (purchase side, FK `fournisseur_id`, `categorie_article_id`,
  `mesure_id`, `entreprise_id`).
- `ArticleVente` → `article_vente` (sales side, FK `article_achat_id`).
- Sales lines (`ligne_commande_vente`) reference **`article_vente.id`** via the
  `article_id` column — not the purchase-side article — matching the existing
  mapping in `LigneCommandeVente`.
- Purchase lines (`ligne_commande_achat`) reference `article.id`.
- `commande_achat` / `commande_vente` carry HT, TVA (19%) and TTC totals
  consistent with their lines.
