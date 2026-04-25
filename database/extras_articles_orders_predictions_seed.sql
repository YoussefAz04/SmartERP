-- ============================================================
-- EXTRAS SEED – Articles, Achats, Ventes & AI Prediction History
-- ============================================================
-- Purpose : Two complementary fixes for the AI Intelligence dashboard.
--   1. The "Historique : Prédictions vs CA Réel" panel was empty for
--      enterprises other than FirstTechnology (id 14). This script seeds
--      a 12-row prediction_log history (Feb–May 2026 issuances × 3
--      horizons) for Noxus (id 7) and Eranow (id 12). actual_value is
--      left NULL on purpose: PredictionLogService.reconcile() runs on
--      every GET /api/analytics/predictions and fills in the real CA
--      from commande_vente automatically – so the table will display
--      real settled errors, not hand-tuned numbers.
--   2. Add 5 more articles per enterprise (with article_vente special
--      pricing + stock), plus a small batch of additional commande_achat
--      and commande_vente referencing those articles. This makes the
--      product catalog richer and the Achats / Ventes lists less sparse.
--
-- Run AFTER the per-enterprise base seeds (noxus_enterprise_7_seed.sql,
-- eranow_enterprise_12_seed.sql, firsttechnology_enterprise_14_seed.sql).
--
-- SAFE TO RE-RUN: every section is guarded by IF NOT EXISTS or scoped
-- DELETE on the same ID range used here.
-- ============================================================
USE [BusinessManager]
GO

-- ============================================================
-- 0. ENSURE prediction_log TABLE EXISTS (mirror of PredictionLog entity)
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'prediction_log' AND schema_id = SCHEMA_ID('dbo'))
BEGIN
    CREATE TABLE [dbo].[prediction_log] (
        [id]                BIGINT IDENTITY(1,1) NOT NULL,
        [entreprise_id]     BIGINT          NOT NULL,
        [prediction_date]   DATE            NOT NULL,
        [prediction_month]  DATE            NOT NULL,
        [target_month]      DATE            NOT NULL,
        [horizon_months]    INT             NOT NULL,
        [predicted_value]   DECIMAL(18,4)   NULL,
        [lower_bound]       DECIMAL(18,4)   NULL,
        [upper_bound]       DECIMAL(18,4)   NULL,
        [confidence_score]  FLOAT           NULL,
        [methodology]       NVARCHAR(512)   NULL,
        [model_name]        NVARCHAR(128)   NULL,
        [actual_value]      DECIMAL(18,4)   NULL,
        [absolute_error]    DECIMAL(18,4)   NULL,
        [percent_error]     FLOAT           NULL,
        [within_interval]   BIT             NULL,
        [settled_at]        DATETIME2(6)    NULL,
        [created_at]        DATETIME2(6)    NOT NULL DEFAULT GETDATE(),
        [updated_at]        DATETIME2(6)    NOT NULL DEFAULT GETDATE(),
        CONSTRAINT [PK_prediction_log] PRIMARY KEY CLUSTERED ([id] ASC),
        CONSTRAINT [uk_prediction_log_entreprise_target_horizon] UNIQUE
            ([entreprise_id],[prediction_month],[target_month],[horizon_months]),
        CONSTRAINT [FK_prediction_log_entreprise] FOREIGN KEY ([entreprise_id])
            REFERENCES [dbo].[entreprise]([id])
    )
    CREATE NONCLUSTERED INDEX [idx_prediction_log_entreprise]   ON [dbo].[prediction_log] ([entreprise_id])
    CREATE NONCLUSTERED INDEX [idx_prediction_log_target_month] ON [dbo].[prediction_log] ([target_month])
END
GO

-- ============================================================
-- 1. CLEAN UP ANY PREVIOUS RUN OF THIS EXTRAS SEED
-- ============================================================
DELETE FROM [dbo].[ligne_commande_achat] WHERE [commande_achat_id] BETWEEN 7041  AND 7050
DELETE FROM [dbo].[commande_achat]        WHERE [id]                BETWEEN 7041  AND 7050
DELETE FROM [dbo].[ligne_commande_vente] WHERE [commande_vente_id] BETWEEN 7076  AND 7090
DELETE FROM [dbo].[commande_vente]        WHERE [id]                BETWEEN 7076  AND 7090

DELETE FROM [dbo].[ligne_commande_achat] WHERE [commande_achat_id] BETWEEN 12041 AND 12050
DELETE FROM [dbo].[commande_achat]        WHERE [id]                BETWEEN 12041 AND 12050
DELETE FROM [dbo].[ligne_commande_vente] WHERE [commande_vente_id] BETWEEN 12076 AND 12090
DELETE FROM [dbo].[commande_vente]        WHERE [id]                BETWEEN 12076 AND 12090

DELETE FROM [dbo].[ligne_commande_achat] WHERE [commande_achat_id] BETWEEN 14041 AND 14050
DELETE FROM [dbo].[commande_achat]        WHERE [id]                BETWEEN 14041 AND 14050
DELETE FROM [dbo].[ligne_commande_vente] WHERE [commande_vente_id] BETWEEN 14076 AND 14090
DELETE FROM [dbo].[commande_vente]        WHERE [id]                BETWEEN 14076 AND 14090

-- Additional FirstTechnology (id 14) data introduced in section 11
DELETE FROM [dbo].[ligne_commande_achat] WHERE [commande_achat_id] BETWEEN 14051 AND 14060
DELETE FROM [dbo].[commande_achat]        WHERE [id]                BETWEEN 14051 AND 14060
DELETE FROM [dbo].[ligne_commande_vente] WHERE [commande_vente_id] BETWEEN 14091 AND 14110
DELETE FROM [dbo].[commande_vente]        WHERE [id]                BETWEEN 14091 AND 14110

DELETE FROM [dbo].[stock]         WHERE [id] BETWEEN 706  AND 710
DELETE FROM [dbo].[stock]         WHERE [id] BETWEEN 1206 AND 1210
DELETE FROM [dbo].[stock]         WHERE [id] BETWEEN 1406 AND 1410
DELETE FROM [dbo].[stock]         WHERE [id] BETWEEN 1411 AND 1415

DELETE FROM [dbo].[article_vente] WHERE [id] BETWEEN 706  AND 710
DELETE FROM [dbo].[article_vente] WHERE [id] BETWEEN 1206 AND 1210
DELETE FROM [dbo].[article_vente] WHERE [id] BETWEEN 1406 AND 1410
DELETE FROM [dbo].[article_vente] WHERE [id] BETWEEN 1411 AND 1415

DELETE FROM [dbo].[article]       WHERE [id] BETWEEN 706  AND 710
DELETE FROM [dbo].[article]       WHERE [id] BETWEEN 1206 AND 1210
DELETE FROM [dbo].[article]       WHERE [id] BETWEEN 1406 AND 1410
DELETE FROM [dbo].[article]       WHERE [id] BETWEEN 1411 AND 1415

DELETE FROM [dbo].[prediction_log]
 WHERE [entreprise_id] IN (7,12)
   AND [prediction_month] BETWEEN '2026-02-01' AND '2026-05-01'
DELETE FROM [dbo].[prediction_log]
 WHERE [entreprise_id] = 14
   AND [prediction_month] BETWEEN '2026-05-01' AND '2026-08-01'
GO

-- ============================================================
-- 2. NEW ARTICLES (5 per enterprise) – Noxus / Eranow / FirstTechnology
--    prix_achat / prix_vente are HT (TVA 19 %)
-- ============================================================
SET IDENTITY_INSERT [dbo].[article] ON
-- Noxus (id 7)
INSERT [dbo].[article] ([id],[reference],[nom],[description],[prix_achat],[prix_vente],[actif],[categorie_article_id],[mesure_id],[tva_id],[entreprise_id],[created_at],[updated_at]) VALUES
(706,N'NOX-006',N'Casque Audio Sans Fil ANC',N'Casque circum-aural Bluetooth 5.3 réduction active de bruit, 35h d''autonomie',180.00,290.00,1,1,1,2,7,GETDATE(),GETDATE()),
(707,N'NOX-007',N'Souris Ergonomique Verticale',N'Souris ergonomique verticale sans fil, capteur 4000 DPI, recharge USB-C',45.00,80.00,1,1,1,2,7,GETDATE(),GETDATE()),
(708,N'NOX-008',N'Webcam 4K Streaming',N'Webcam 4K UHD avec micro stéréo intégré, autofocus AI et cache de confidentialité',95.00,160.00,1,1,1,2,7,GETDATE(),GETDATE()),
(709,N'NOX-009',N'Disque SSD Externe 1To',N'Disque SSD portable USB-C 3.2 Gen2, 1050 Mo/s, étanche IP55',120.00,200.00,1,1,1,2,7,GETDATE(),GETDATE()),
(710,N'NOX-010',N'Hub USB-C 8 en 1',N'Hub multiport USB-C : HDMI 4K, 2x USB 3.0, lecteur SD/microSD, RJ45, PD 100W',55.00,95.00,1,1,1,2,7,GETDATE(),GETDATE())

-- Eranow (id 12)
INSERT [dbo].[article] ([id],[reference],[nom],[description],[prix_achat],[prix_vente],[actif],[categorie_article_id],[mesure_id],[tva_id],[entreprise_id],[created_at],[updated_at]) VALUES
(1206,N'ERA-006',N'Lotion Tonique Hydratante 250ml',N'Lotion tonique sans alcool aux extraits floraux, prépare la peau aux soins',32.00,55.00,1,12,1,2,12,GETDATE(),GETDATE()),
(1207,N'ERA-007',N'Masque Visage Argile 75ml',N'Masque purifiant à l''argile blanche et zinc, peaux mixtes à grasses',28.00,48.00,1,12,1,2,12,GETDATE(),GETDATE()),
(1208,N'ERA-008',N'Rouge à Lèvres Mat Longue Tenue',N'Rouge à lèvres liquide mat 16h, formule confort enrichie en vitamine E',18.00,35.00,1,12,1,2,12,GETDATE(),GETDATE()),
(1209,N'ERA-009',N'Brume Hydratante Visage 150ml',N'Brume rafraîchissante à l''eau de rose, hydrate et fixe le maquillage',22.00,40.00,1,12,1,2,12,GETDATE(),GETDATE()),
(1210,N'ERA-010',N'Pinceau Maquillage Pro Lot 12',N'Set 12 pinceaux professionnels poils synthétiques, étui voyage inclus',60.00,110.00,1,12,1,2,12,GETDATE(),GETDATE())

-- FirstTechnology (id 14)
INSERT [dbo].[article] ([id],[reference],[nom],[description],[prix_achat],[prix_vente],[actif],[categorie_article_id],[mesure_id],[tva_id],[entreprise_id],[created_at],[updated_at]) VALUES
(1406,N'FT-006',N'Imprimante Multifonction Laser',N'Imprimante laser couleur multifonction (impression/scan/copie), Wi-Fi, recto-verso auto',520.00,780.00,1,14,1,2,14,GETDATE(),GETDATE()),
(1407,N'FT-007',N'Tablette Graphique 10"',N'Tablette graphique 10 pouces, 8192 niveaux de pression, stylet sans pile',230.00,360.00,1,14,1,2,14,GETDATE(),GETDATE()),
(1408,N'FT-008',N'Onduleur 1500VA',N'Onduleur en ligne 1500VA / 900W, 8 prises, gestion automatique réseau',310.00,490.00,1,14,1,2,14,GETDATE(),GETDATE()),
(1409,N'FT-009',N'Switch Ethernet 24 Ports',N'Switch managé 24 ports Gigabit + 4 SFP, alimentation PoE+ 370W',420.00,650.00,1,14,1,2,14,GETDATE(),GETDATE()),
(1410,N'FT-010',N'Caméra IP Surveillance 4MP',N'Caméra IP dôme 4MP, vision nocturne IR 30m, IP67, ONVIF, audio bidirectionnel',140.00,230.00,1,14,1,2,14,GETDATE(),GETDATE())
SET IDENTITY_INSERT [dbo].[article] OFF
GO

-- ============================================================
-- 3. ARTICLE_VENTE rows for the 15 new articles
-- ============================================================
SET IDENTITY_INSERT [dbo].[article_vente] ON
INSERT [dbo].[article_vente] ([id],[article_id],[prix_vente_special],[description_vente],[created_at],[updated_at]) VALUES
(706, 706, 270.00,N'Tarif revendeurs Casque ANC commandes >= 5',GETDATE(),GETDATE()),
(707, 707,  72.00,N'Remise Souris Verticale lot >= 10',GETDATE(),GETDATE()),
(708, 708, 145.00,N'Promotion Webcam 4K offre lancement',GETDATE(),GETDATE()),
(709, 709, 185.00,N'Prix préférentiel SSD Externe 1To grands comptes',GETDATE(),GETDATE()),
(710, 710,  85.00,N'Tarif spécial Hub USB-C lot revendeurs',GETDATE(),GETDATE()),
(1206,1206, 50.00,N'Offre fidélité Lotion Tonique abonnés',GETDATE(),GETDATE()),
(1207,1207, 44.00,N'Tarif spas partenaires Masque Argile',GETDATE(),GETDATE()),
(1208,1208, 32.00,N'Promotion Rouge à Lèvres Mat lot >= 12',GETDATE(),GETDATE()),
(1209,1209, 36.00,N'Remise Brume Hydratante revendeurs',GETDATE(),GETDATE()),
(1210,1210,100.00,N'Pack Pinceau Maquillage Pro grossistes',GETDATE(),GETDATE()),
(1406,1406,750.00,N'Tarif spécial Imprimante Laser parc IT',GETDATE(),GETDATE()),
(1407,1407,340.00,N'Remise Tablette Graphique éducation/écoles',GETDATE(),GETDATE()),
(1408,1408,470.00,N'Prix grands comptes Onduleur 1500VA',GETDATE(),GETDATE()),
(1409,1409,620.00,N'Tarif Switch 24 ports déploiements > 5',GETDATE(),GETDATE()),
(1410,1410,215.00,N'Promotion Caméra IP installations volumiques',GETDATE(),GETDATE())
SET IDENTITY_INSERT [dbo].[article_vente] OFF
GO

-- ============================================================
-- 4. STOCK rows for the 15 new articles
-- ============================================================
SET IDENTITY_INSERT [dbo].[stock] ON
INSERT [dbo].[stock] ([id],[article_id],[entreprise_id],[quantite_disponible],[quantite_minimale],[quantite_maximale],[emplacement],[created_at],[updated_at]) VALUES
(706, 706, 7,  120.000,15.000, 400.000,N'Entrepôt Principal – Étagère NOX-C1',GETDATE(),GETDATE()),
(707, 707, 7,  240.000,30.000, 800.000,N'Entrepôt Principal – Étagère NOX-C2',GETDATE(),GETDATE()),
(708, 708, 7,  180.000,20.000, 500.000,N'Entrepôt Principal – Étagère NOX-D1',GETDATE(),GETDATE()),
(709, 709, 7,  140.000,18.000, 450.000,N'Entrepôt Secondaire – Bac NOX-D2',GETDATE(),GETDATE()),
(710, 710, 7,  300.000,40.000,1000.000,N'Entrepôt Principal – Étagère NOX-E1',GETDATE(),GETDATE()),
(1206,1206,12, 220.000,30.000, 600.000,N'Entrepôt Principal – Étagère ERA-C1',GETDATE(),GETDATE()),
(1207,1207,12, 260.000,40.000, 700.000,N'Entrepôt Principal – Étagère ERA-C2',GETDATE(),GETDATE()),
(1208,1208,12, 380.000,60.000,1200.000,N'Entrepôt Secondaire – Bac ERA-D1',GETDATE(),GETDATE()),
(1209,1209,12, 200.000,25.000, 600.000,N'Entrepôt Principal – Étagère ERA-D2',GETDATE(),GETDATE()),
(1210,1210,12,  90.000,12.000, 250.000,N'Entrepôt Secondaire – Armoire ERA-E1',GETDATE(),GETDATE()),
(1406,1406,14,  35.000, 5.000, 100.000,N'Entrepôt Principal – Étagère IT-C1',GETDATE(),GETDATE()),
(1407,1407,14,  60.000, 8.000, 200.000,N'Entrepôt Principal – Étagère IT-C2',GETDATE(),GETDATE()),
(1408,1408,14,  45.000, 6.000, 150.000,N'Entrepôt Principal – Étagère IT-D1',GETDATE(),GETDATE()),
(1409,1409,14,  25.000, 4.000,  80.000,N'Entrepôt Principal – Étagère IT-D2',GETDATE(),GETDATE()),
(1410,1410,14, 110.000,15.000, 350.000,N'Entrepôt Secondaire – Bac IT-E1',GETDATE(),GETDATE())
SET IDENTITY_INSERT [dbo].[stock] OFF
GO

-- ============================================================
-- 5. EXTRA PURCHASE ORDERS (10 per enterprise, Apr 2026)
--    Header montants computed below from the line items in section 6.
-- ============================================================
SET IDENTITY_INSERT [dbo].[commande_achat] ON

-- Noxus (id 7) -- fournisseurs 71-74, articles 706-710
-- Templates HT (clean integer quantities × HT unit price):
--   NA: 10×706(180) + 8×708(95)            =      2560.00  TVA  486.40  TTC  3046.40
--   NB: 20×707(45)                         =       900.00  TVA  171.00  TTC  1071.00
--   NC: 10×709(120) + 10×710(55)           =      1750.00  TVA  332.50  TTC  2082.50
--   ND:  8×706(180) +  8×708(95)           =      2200.00  TVA  418.00  TTC  2618.00
--   NE: 20×710(55)                         =      1100.00  TVA  209.00  TTC  1309.00
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES
(7041,N'CA-NOX-2026-041','2026-04-01','2026-04-09',N'RECUE',     2560.00,486.40,3046.40,71,7,GETDATE(),GETDATE()),
(7042,N'CA-NOX-2026-042','2026-04-03','2026-04-11',N'RECUE',      900.00,171.00,1071.00,72,7,GETDATE(),GETDATE()),
(7043,N'CA-NOX-2026-043','2026-04-05','2026-04-13',N'RECUE',     1750.00,332.50,2082.50,73,7,GETDATE(),GETDATE()),
(7044,N'CA-NOX-2026-044','2026-04-07','2026-04-15',N'RECUE',     2200.00,418.00,2618.00,74,7,GETDATE(),GETDATE()),
(7045,N'CA-NOX-2026-045','2026-04-09','2026-04-17',N'RECUE',     1100.00,209.00,1309.00,71,7,GETDATE(),GETDATE()),
(7046,N'CA-NOX-2026-046','2026-04-11','2026-04-19',N'CONFIRMEE', 2560.00,486.40,3046.40,72,7,GETDATE(),GETDATE()),
(7047,N'CA-NOX-2026-047','2026-04-13','2026-04-21',N'CONFIRMEE',  900.00,171.00,1071.00,73,7,GETDATE(),GETDATE()),
(7048,N'CA-NOX-2026-048','2026-04-15','2026-04-23',N'CONFIRMEE', 1750.00,332.50,2082.50,74,7,GETDATE(),GETDATE()),
(7049,N'CA-NOX-2026-049','2026-04-17',NULL,        N'EN_ATTENTE',2200.00,418.00,2618.00,71,7,GETDATE(),GETDATE()),
(7050,N'CA-NOX-2026-050','2026-04-19',NULL,        N'BROUILLON', 1100.00,209.00,1309.00,72,7,GETDATE(),GETDATE())

-- Eranow (id 12) -- fournisseurs 121-124, articles 1206-1210
-- Templates HT:
--   EA: 15×1206(32) + 5×1210(60)           =       780.00  TVA  148.20  TTC   928.20
--   EB: 15×1207(28) + 15×1208(18)          =       690.00  TVA  131.10  TTC   821.10
--   EC: 30×1208(18)                        =       540.00  TVA  102.60  TTC   642.60
--   ED: 25×1209(22) + 9×1210(60)           =      1090.00  TVA  207.10  TTC  1297.10
--   EE: 20×1206(32) + 6×1207(28)           =       808.00  TVA  153.52  TTC   961.52
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES
(12041,N'CA-ERA-2026-041','2026-04-01','2026-04-09',N'RECUE',      780.00,148.20, 928.20,121,12,GETDATE(),GETDATE()),
(12042,N'CA-ERA-2026-042','2026-04-03','2026-04-11',N'RECUE',      690.00,131.10, 821.10,122,12,GETDATE(),GETDATE()),
(12043,N'CA-ERA-2026-043','2026-04-05','2026-04-13',N'RECUE',      540.00,102.60, 642.60,123,12,GETDATE(),GETDATE()),
(12044,N'CA-ERA-2026-044','2026-04-07','2026-04-15',N'RECUE',     1090.00,207.10,1297.10,124,12,GETDATE(),GETDATE()),
(12045,N'CA-ERA-2026-045','2026-04-09','2026-04-17',N'RECUE',      808.00,153.52, 961.52,121,12,GETDATE(),GETDATE()),
(12046,N'CA-ERA-2026-046','2026-04-11','2026-04-19',N'CONFIRMEE',  780.00,148.20, 928.20,122,12,GETDATE(),GETDATE()),
(12047,N'CA-ERA-2026-047','2026-04-13','2026-04-21',N'CONFIRMEE',  690.00,131.10, 821.10,123,12,GETDATE(),GETDATE()),
(12048,N'CA-ERA-2026-048','2026-04-15','2026-04-23',N'CONFIRMEE',  540.00,102.60, 642.60,124,12,GETDATE(),GETDATE()),
(12049,N'CA-ERA-2026-049','2026-04-17',NULL,        N'EN_ATTENTE',1090.00,207.10,1297.10,121,12,GETDATE(),GETDATE()),
(12050,N'CA-ERA-2026-050','2026-04-19',NULL,        N'BROUILLON',  808.00,153.52, 961.52,122,12,GETDATE(),GETDATE())

-- FirstTechnology (id 14) -- fournisseurs 141-144, articles 1406-1410
-- Templates HT:
--   FA: 2×1406(520) + 3×1408(310)          =      1970.00  TVA  374.30  TTC  2344.30
--   FB: 4×1407(230)                        =       920.00  TVA  174.80  TTC  1094.80
--   FC: 2×1409(420) + 6×1410(140)          =      1680.00  TVA  319.20  TTC  1999.20
--   FD: 3×1406(520) + 3×1408(310)          =      2490.00  TVA  473.10  TTC  2963.10
--   FE: 2×1407(230) + 4×1410(140)          =      1020.00  TVA  193.80  TTC  1213.80
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES
(14041,N'CA-FT-2026-041','2026-04-01','2026-04-09',N'RECUE',     1970.00,374.30,2344.30,141,14,GETDATE(),GETDATE()),
(14042,N'CA-FT-2026-042','2026-04-03','2026-04-11',N'RECUE',      920.00,174.80,1094.80,142,14,GETDATE(),GETDATE()),
(14043,N'CA-FT-2026-043','2026-04-05','2026-04-13',N'RECUE',     1680.00,319.20,1999.20,143,14,GETDATE(),GETDATE()),
(14044,N'CA-FT-2026-044','2026-04-07','2026-04-15',N'RECUE',     2490.00,473.10,2963.10,144,14,GETDATE(),GETDATE()),
(14045,N'CA-FT-2026-045','2026-04-09','2026-04-17',N'RECUE',     1020.00,193.80,1213.80,141,14,GETDATE(),GETDATE()),
(14046,N'CA-FT-2026-046','2026-04-11','2026-04-19',N'CONFIRMEE', 1970.00,374.30,2344.30,142,14,GETDATE(),GETDATE()),
(14047,N'CA-FT-2026-047','2026-04-13','2026-04-21',N'CONFIRMEE',  920.00,174.80,1094.80,143,14,GETDATE(),GETDATE()),
(14048,N'CA-FT-2026-048','2026-04-15','2026-04-23',N'CONFIRMEE', 1680.00,319.20,1999.20,144,14,GETDATE(),GETDATE()),
(14049,N'CA-FT-2026-049','2026-04-17',NULL,        N'EN_ATTENTE',2490.00,473.10,2963.10,141,14,GETDATE(),GETDATE()),
(14050,N'CA-FT-2026-050','2026-04-19',NULL,        N'BROUILLON', 1020.00,193.80,1213.80,142,14,GETDATE(),GETDATE())

SET IDENTITY_INSERT [dbo].[commande_achat] OFF
GO

-- ============================================================
-- 6. PURCHASE ORDER LINE ITEMS (ligne_commande_achat)
--    montant_ht  = quantite * prix_unitaire_ht
--    montant_ttc = montant_ht * 1.19
-- ============================================================
INSERT [dbo].[ligne_commande_achat] ([commande_achat_id],[article_id],[quantite],[prix_unitaire_ht],[taux_tva],[montant_ht],[montant_ttc],[created_at],[updated_at]) VALUES
-- Noxus 7041..7050 (templates NA / NB / NC / ND / NE / NA / NB / NC / ND / NE)
(7041, 706,10.000,180.00,19.00,1800.00,2142.00,GETDATE(),GETDATE()),
(7041, 708, 8.000, 95.00,19.00, 760.00, 904.40,GETDATE(),GETDATE()),
(7042, 707,20.000, 45.00,19.00, 900.00,1071.00,GETDATE(),GETDATE()),
(7043, 709,10.000,120.00,19.00,1200.00,1428.00,GETDATE(),GETDATE()),
(7043, 710,10.000, 55.00,19.00, 550.00, 654.50,GETDATE(),GETDATE()),
(7044, 706, 8.000,180.00,19.00,1440.00,1713.60,GETDATE(),GETDATE()),
(7044, 708, 8.000, 95.00,19.00, 760.00, 904.40,GETDATE(),GETDATE()),
(7045, 710,20.000, 55.00,19.00,1100.00,1309.00,GETDATE(),GETDATE()),
(7046, 706,10.000,180.00,19.00,1800.00,2142.00,GETDATE(),GETDATE()),
(7046, 708, 8.000, 95.00,19.00, 760.00, 904.40,GETDATE(),GETDATE()),
(7047, 707,20.000, 45.00,19.00, 900.00,1071.00,GETDATE(),GETDATE()),
(7048, 709,10.000,120.00,19.00,1200.00,1428.00,GETDATE(),GETDATE()),
(7048, 710,10.000, 55.00,19.00, 550.00, 654.50,GETDATE(),GETDATE()),
(7049, 706, 8.000,180.00,19.00,1440.00,1713.60,GETDATE(),GETDATE()),
(7049, 708, 8.000, 95.00,19.00, 760.00, 904.40,GETDATE(),GETDATE()),
(7050, 710,20.000, 55.00,19.00,1100.00,1309.00,GETDATE(),GETDATE()),

-- Eranow 12041..12050 (templates EA / EB / EC / ED / EE / EA / EB / EC / ED / EE)
(12041,1206,15.000,32.00,19.00, 480.00, 571.20,GETDATE(),GETDATE()),
(12041,1210, 5.000,60.00,19.00, 300.00, 357.00,GETDATE(),GETDATE()),
(12042,1207,15.000,28.00,19.00, 420.00, 499.80,GETDATE(),GETDATE()),
(12042,1208,15.000,18.00,19.00, 270.00, 321.30,GETDATE(),GETDATE()),
(12043,1208,30.000,18.00,19.00, 540.00, 642.60,GETDATE(),GETDATE()),
(12044,1209,25.000,22.00,19.00, 550.00, 654.50,GETDATE(),GETDATE()),
(12044,1210, 9.000,60.00,19.00, 540.00, 642.60,GETDATE(),GETDATE()),
(12045,1206,20.000,32.00,19.00, 640.00, 761.60,GETDATE(),GETDATE()),
(12045,1207, 6.000,28.00,19.00, 168.00, 199.92,GETDATE(),GETDATE()),
(12046,1206,15.000,32.00,19.00, 480.00, 571.20,GETDATE(),GETDATE()),
(12046,1210, 5.000,60.00,19.00, 300.00, 357.00,GETDATE(),GETDATE()),
(12047,1207,15.000,28.00,19.00, 420.00, 499.80,GETDATE(),GETDATE()),
(12047,1208,15.000,18.00,19.00, 270.00, 321.30,GETDATE(),GETDATE()),
(12048,1208,30.000,18.00,19.00, 540.00, 642.60,GETDATE(),GETDATE()),
(12049,1209,25.000,22.00,19.00, 550.00, 654.50,GETDATE(),GETDATE()),
(12049,1210, 9.000,60.00,19.00, 540.00, 642.60,GETDATE(),GETDATE()),
(12050,1206,20.000,32.00,19.00, 640.00, 761.60,GETDATE(),GETDATE()),
(12050,1207, 6.000,28.00,19.00, 168.00, 199.92,GETDATE(),GETDATE()),

-- FirstTechnology 14041..14050 (templates FA / FB / FC / FD / FE / FA / FB / FC / FD / FE)
(14041,1406, 2.000,520.00,19.00,1040.00,1237.60,GETDATE(),GETDATE()),
(14041,1408, 3.000,310.00,19.00, 930.00,1106.70,GETDATE(),GETDATE()),
(14042,1407, 4.000,230.00,19.00, 920.00,1094.80,GETDATE(),GETDATE()),
(14043,1409, 2.000,420.00,19.00, 840.00, 999.60,GETDATE(),GETDATE()),
(14043,1410, 6.000,140.00,19.00, 840.00, 999.60,GETDATE(),GETDATE()),
(14044,1406, 3.000,520.00,19.00,1560.00,1856.40,GETDATE(),GETDATE()),
(14044,1408, 3.000,310.00,19.00, 930.00,1106.70,GETDATE(),GETDATE()),
(14045,1407, 2.000,230.00,19.00, 460.00, 547.40,GETDATE(),GETDATE()),
(14045,1410, 4.000,140.00,19.00, 560.00, 666.40,GETDATE(),GETDATE()),
(14046,1406, 2.000,520.00,19.00,1040.00,1237.60,GETDATE(),GETDATE()),
(14046,1408, 3.000,310.00,19.00, 930.00,1106.70,GETDATE(),GETDATE()),
(14047,1407, 4.000,230.00,19.00, 920.00,1094.80,GETDATE(),GETDATE()),
(14048,1409, 2.000,420.00,19.00, 840.00, 999.60,GETDATE(),GETDATE()),
(14048,1410, 6.000,140.00,19.00, 840.00, 999.60,GETDATE(),GETDATE()),
(14049,1406, 3.000,520.00,19.00,1560.00,1856.40,GETDATE(),GETDATE()),
(14049,1408, 3.000,310.00,19.00, 930.00,1106.70,GETDATE(),GETDATE()),
(14050,1407, 2.000,230.00,19.00, 460.00, 547.40,GETDATE(),GETDATE()),
(14050,1410, 4.000,140.00,19.00, 560.00, 666.40,GETDATE(),GETDATE())
GO

-- ============================================================
-- 7. EXTRA SALES ORDERS (15 per enterprise, Apr 2026)
--    Header montants computed below from the line items in section 8.
-- ============================================================
SET IDENTITY_INSERT [dbo].[commande_vente] ON

-- Noxus (id 7) -- clients 71-74, articles 706-710 (HT: 290/80/160/200/95)
-- Templates HT:
--   N1: 8×706(290) + 7×708(160)            =      3440.00  TVA  653.60  TTC  4093.60
--   N2: 15×707(80) + 7×710(95)             =      1865.00  TVA  354.35  TTC  2219.35
--   N3: 10×709(200) + 5×710(95)            =      2475.00  TVA  470.25  TTC  2945.25
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES
(7076,N'CV-NOX-2026-076','2026-04-02','2026-04-08',N'LIVREE',    3440.00,653.60,4093.60,71,7,GETDATE(),GETDATE()),
(7077,N'CV-NOX-2026-077','2026-04-03','2026-04-09',N'LIVREE',    1865.00,354.35,2219.35,72,7,GETDATE(),GETDATE()),
(7078,N'CV-NOX-2026-078','2026-04-05','2026-04-11',N'LIVREE',    2475.00,470.25,2945.25,73,7,GETDATE(),GETDATE()),
(7079,N'CV-NOX-2026-079','2026-04-06','2026-04-12',N'LIVREE',    3440.00,653.60,4093.60,74,7,GETDATE(),GETDATE()),
(7080,N'CV-NOX-2026-080','2026-04-08','2026-04-14',N'LIVREE',    1865.00,354.35,2219.35,71,7,GETDATE(),GETDATE()),
(7081,N'CV-NOX-2026-081','2026-04-10','2026-04-16',N'LIVREE',    2475.00,470.25,2945.25,72,7,GETDATE(),GETDATE()),
(7082,N'CV-NOX-2026-082','2026-04-12','2026-04-18',N'LIVREE',    3440.00,653.60,4093.60,73,7,GETDATE(),GETDATE()),
(7083,N'CV-NOX-2026-083','2026-04-14','2026-04-20',N'LIVREE',    1865.00,354.35,2219.35,74,7,GETDATE(),GETDATE()),
(7084,N'CV-NOX-2026-084','2026-04-15','2026-04-21',N'LIVREE',    2475.00,470.25,2945.25,71,7,GETDATE(),GETDATE()),
(7085,N'CV-NOX-2026-085','2026-04-17','2026-04-23',N'LIVREE',    3440.00,653.60,4093.60,72,7,GETDATE(),GETDATE()),
(7086,N'CV-NOX-2026-086','2026-04-18','2026-04-24',N'CONFIRMEE', 1865.00,354.35,2219.35,73,7,GETDATE(),GETDATE()),
(7087,N'CV-NOX-2026-087','2026-04-19','2026-04-25',N'CONFIRMEE', 2475.00,470.25,2945.25,74,7,GETDATE(),GETDATE()),
(7088,N'CV-NOX-2026-088','2026-04-20','2026-04-26',N'CONFIRMEE', 3440.00,653.60,4093.60,71,7,GETDATE(),GETDATE()),
(7089,N'CV-NOX-2026-089','2026-04-21',NULL,        N'EN_ATTENTE',1865.00,354.35,2219.35,72,7,GETDATE(),GETDATE()),
(7090,N'CV-NOX-2026-090','2026-04-22',NULL,        N'BROUILLON', 2475.00,470.25,2945.25,73,7,GETDATE(),GETDATE())

-- Eranow (id 12) -- clients 121-124, articles 1206-1210 (HT: 55/48/35/40/110)
-- Templates HT:
--   E1: 12×1206(55) + 6×1210(110)          =      1320.00  TVA  250.80  TTC  1570.80
--   E2: 10×1207(48) + 12×1208(35)          =       900.00  TVA  171.00  TTC  1071.00
--   E3: 12×1209(40) + 3×1210(110)          =       810.00  TVA  153.90  TTC   963.90
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES
(12076,N'CV-ERA-2026-076','2026-04-02','2026-04-08',N'LIVREE',    1320.00,250.80,1570.80,121,12,GETDATE(),GETDATE()),
(12077,N'CV-ERA-2026-077','2026-04-03','2026-04-09',N'LIVREE',     900.00,171.00,1071.00,122,12,GETDATE(),GETDATE()),
(12078,N'CV-ERA-2026-078','2026-04-05','2026-04-11',N'LIVREE',     810.00,153.90, 963.90,123,12,GETDATE(),GETDATE()),
(12079,N'CV-ERA-2026-079','2026-04-06','2026-04-12',N'LIVREE',    1320.00,250.80,1570.80,124,12,GETDATE(),GETDATE()),
(12080,N'CV-ERA-2026-080','2026-04-08','2026-04-14',N'LIVREE',     900.00,171.00,1071.00,121,12,GETDATE(),GETDATE()),
(12081,N'CV-ERA-2026-081','2026-04-10','2026-04-16',N'LIVREE',     810.00,153.90, 963.90,122,12,GETDATE(),GETDATE()),
(12082,N'CV-ERA-2026-082','2026-04-12','2026-04-18',N'LIVREE',    1320.00,250.80,1570.80,123,12,GETDATE(),GETDATE()),
(12083,N'CV-ERA-2026-083','2026-04-14','2026-04-20',N'LIVREE',     900.00,171.00,1071.00,124,12,GETDATE(),GETDATE()),
(12084,N'CV-ERA-2026-084','2026-04-15','2026-04-21',N'LIVREE',     810.00,153.90, 963.90,121,12,GETDATE(),GETDATE()),
(12085,N'CV-ERA-2026-085','2026-04-17','2026-04-23',N'LIVREE',    1320.00,250.80,1570.80,122,12,GETDATE(),GETDATE()),
(12086,N'CV-ERA-2026-086','2026-04-18','2026-04-24',N'CONFIRMEE',  900.00,171.00,1071.00,123,12,GETDATE(),GETDATE()),
(12087,N'CV-ERA-2026-087','2026-04-19','2026-04-25',N'CONFIRMEE',  810.00,153.90, 963.90,124,12,GETDATE(),GETDATE()),
(12088,N'CV-ERA-2026-088','2026-04-20','2026-04-26',N'CONFIRMEE', 1320.00,250.80,1570.80,121,12,GETDATE(),GETDATE()),
(12089,N'CV-ERA-2026-089','2026-04-21',NULL,        N'EN_ATTENTE', 900.00,171.00,1071.00,122,12,GETDATE(),GETDATE()),
(12090,N'CV-ERA-2026-090','2026-04-22',NULL,        N'BROUILLON',  810.00,153.90, 963.90,123,12,GETDATE(),GETDATE())

-- FirstTechnology (id 14) -- clients 141-144, articles 1406-1410 (HT: 780/360/490/650/230)
-- Templates HT:
--   F1: 3×1406(780) + 3×1410(230)          =      3030.00  TVA  575.70  TTC  3605.70
--   F2: 4×1407(360) + 2×1408(490)          =      2420.00  TVA  459.80  TTC  2879.80
--   F3: 4×1409(650) + 3×1410(230)          =      3290.00  TVA  625.10  TTC  3915.10
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES
(14076,N'CV-FT-2026-076','2026-04-02','2026-04-08',N'LIVREE',    3030.00,575.70,3605.70,141,14,GETDATE(),GETDATE()),
(14077,N'CV-FT-2026-077','2026-04-03','2026-04-09',N'LIVREE',    2420.00,459.80,2879.80,142,14,GETDATE(),GETDATE()),
(14078,N'CV-FT-2026-078','2026-04-05','2026-04-11',N'LIVREE',    3290.00,625.10,3915.10,143,14,GETDATE(),GETDATE()),
(14079,N'CV-FT-2026-079','2026-04-06','2026-04-12',N'LIVREE',    3030.00,575.70,3605.70,144,14,GETDATE(),GETDATE()),
(14080,N'CV-FT-2026-080','2026-04-08','2026-04-14',N'LIVREE',    2420.00,459.80,2879.80,141,14,GETDATE(),GETDATE()),
(14081,N'CV-FT-2026-081','2026-04-10','2026-04-16',N'LIVREE',    3290.00,625.10,3915.10,142,14,GETDATE(),GETDATE()),
(14082,N'CV-FT-2026-082','2026-04-12','2026-04-18',N'LIVREE',    3030.00,575.70,3605.70,143,14,GETDATE(),GETDATE()),
(14083,N'CV-FT-2026-083','2026-04-14','2026-04-20',N'LIVREE',    2420.00,459.80,2879.80,144,14,GETDATE(),GETDATE()),
(14084,N'CV-FT-2026-084','2026-04-15','2026-04-21',N'LIVREE',    3290.00,625.10,3915.10,141,14,GETDATE(),GETDATE()),
(14085,N'CV-FT-2026-085','2026-04-17','2026-04-23',N'LIVREE',    3030.00,575.70,3605.70,142,14,GETDATE(),GETDATE()),
(14086,N'CV-FT-2026-086','2026-04-18','2026-04-24',N'CONFIRMEE', 2420.00,459.80,2879.80,143,14,GETDATE(),GETDATE()),
(14087,N'CV-FT-2026-087','2026-04-19','2026-04-25',N'CONFIRMEE', 3290.00,625.10,3915.10,144,14,GETDATE(),GETDATE()),
(14088,N'CV-FT-2026-088','2026-04-20','2026-04-26',N'CONFIRMEE', 3030.00,575.70,3605.70,141,14,GETDATE(),GETDATE()),
(14089,N'CV-FT-2026-089','2026-04-21',NULL,        N'EN_ATTENTE',2420.00,459.80,2879.80,142,14,GETDATE(),GETDATE()),
(14090,N'CV-FT-2026-090','2026-04-22',NULL,        N'BROUILLON', 3290.00,625.10,3915.10,143,14,GETDATE(),GETDATE())

SET IDENTITY_INSERT [dbo].[commande_vente] OFF
GO

-- ============================================================
-- 8. SALES ORDER LINE ITEMS (ligne_commande_vente)
-- ============================================================
INSERT [dbo].[ligne_commande_vente] ([commande_vente_id],[article_id],[quantite],[prix_unitaire_ht],[taux_tva],[montant_ht],[montant_ttc],[created_at],[updated_at]) VALUES
-- Noxus 7076..7090 (templates N1, N2, N3, N1, N2, N3, ...)
(7076, 706, 8.000,290.00,19.00,2320.00,2760.80,GETDATE(),GETDATE()),
(7076, 708, 7.000,160.00,19.00,1120.00,1332.80,GETDATE(),GETDATE()),
(7077, 707,15.000, 80.00,19.00,1200.00,1428.00,GETDATE(),GETDATE()),
(7077, 710, 7.000, 95.00,19.00, 665.00, 791.35,GETDATE(),GETDATE()),
(7078, 709,10.000,200.00,19.00,2000.00,2380.00,GETDATE(),GETDATE()),
(7078, 710, 5.000, 95.00,19.00, 475.00, 565.25,GETDATE(),GETDATE()),
(7079, 706, 8.000,290.00,19.00,2320.00,2760.80,GETDATE(),GETDATE()),
(7079, 708, 7.000,160.00,19.00,1120.00,1332.80,GETDATE(),GETDATE()),
(7080, 707,15.000, 80.00,19.00,1200.00,1428.00,GETDATE(),GETDATE()),
(7080, 710, 7.000, 95.00,19.00, 665.00, 791.35,GETDATE(),GETDATE()),
(7081, 709,10.000,200.00,19.00,2000.00,2380.00,GETDATE(),GETDATE()),
(7081, 710, 5.000, 95.00,19.00, 475.00, 565.25,GETDATE(),GETDATE()),
(7082, 706, 8.000,290.00,19.00,2320.00,2760.80,GETDATE(),GETDATE()),
(7082, 708, 7.000,160.00,19.00,1120.00,1332.80,GETDATE(),GETDATE()),
(7083, 707,15.000, 80.00,19.00,1200.00,1428.00,GETDATE(),GETDATE()),
(7083, 710, 7.000, 95.00,19.00, 665.00, 791.35,GETDATE(),GETDATE()),
(7084, 709,10.000,200.00,19.00,2000.00,2380.00,GETDATE(),GETDATE()),
(7084, 710, 5.000, 95.00,19.00, 475.00, 565.25,GETDATE(),GETDATE()),
(7085, 706, 8.000,290.00,19.00,2320.00,2760.80,GETDATE(),GETDATE()),
(7085, 708, 7.000,160.00,19.00,1120.00,1332.80,GETDATE(),GETDATE()),
(7086, 707,15.000, 80.00,19.00,1200.00,1428.00,GETDATE(),GETDATE()),
(7086, 710, 7.000, 95.00,19.00, 665.00, 791.35,GETDATE(),GETDATE()),
(7087, 709,10.000,200.00,19.00,2000.00,2380.00,GETDATE(),GETDATE()),
(7087, 710, 5.000, 95.00,19.00, 475.00, 565.25,GETDATE(),GETDATE()),
(7088, 706, 8.000,290.00,19.00,2320.00,2760.80,GETDATE(),GETDATE()),
(7088, 708, 7.000,160.00,19.00,1120.00,1332.80,GETDATE(),GETDATE()),
(7089, 707,15.000, 80.00,19.00,1200.00,1428.00,GETDATE(),GETDATE()),
(7089, 710, 7.000, 95.00,19.00, 665.00, 791.35,GETDATE(),GETDATE()),
(7090, 709,10.000,200.00,19.00,2000.00,2380.00,GETDATE(),GETDATE()),
(7090, 710, 5.000, 95.00,19.00, 475.00, 565.25,GETDATE(),GETDATE()),

-- Eranow 12076..12090 (templates E1, E2, E3, E1, E2, E3, ...)
(12076,1206,12.000, 55.00,19.00, 660.00, 785.40,GETDATE(),GETDATE()),
(12076,1210, 6.000,110.00,19.00, 660.00, 785.40,GETDATE(),GETDATE()),
(12077,1207,10.000, 48.00,19.00, 480.00, 571.20,GETDATE(),GETDATE()),
(12077,1208,12.000, 35.00,19.00, 420.00, 499.80,GETDATE(),GETDATE()),
(12078,1209,12.000, 40.00,19.00, 480.00, 571.20,GETDATE(),GETDATE()),
(12078,1210, 3.000,110.00,19.00, 330.00, 392.70,GETDATE(),GETDATE()),
(12079,1206,12.000, 55.00,19.00, 660.00, 785.40,GETDATE(),GETDATE()),
(12079,1210, 6.000,110.00,19.00, 660.00, 785.40,GETDATE(),GETDATE()),
(12080,1207,10.000, 48.00,19.00, 480.00, 571.20,GETDATE(),GETDATE()),
(12080,1208,12.000, 35.00,19.00, 420.00, 499.80,GETDATE(),GETDATE()),
(12081,1209,12.000, 40.00,19.00, 480.00, 571.20,GETDATE(),GETDATE()),
(12081,1210, 3.000,110.00,19.00, 330.00, 392.70,GETDATE(),GETDATE()),
(12082,1206,12.000, 55.00,19.00, 660.00, 785.40,GETDATE(),GETDATE()),
(12082,1210, 6.000,110.00,19.00, 660.00, 785.40,GETDATE(),GETDATE()),
(12083,1207,10.000, 48.00,19.00, 480.00, 571.20,GETDATE(),GETDATE()),
(12083,1208,12.000, 35.00,19.00, 420.00, 499.80,GETDATE(),GETDATE()),
(12084,1209,12.000, 40.00,19.00, 480.00, 571.20,GETDATE(),GETDATE()),
(12084,1210, 3.000,110.00,19.00, 330.00, 392.70,GETDATE(),GETDATE()),
(12085,1206,12.000, 55.00,19.00, 660.00, 785.40,GETDATE(),GETDATE()),
(12085,1210, 6.000,110.00,19.00, 660.00, 785.40,GETDATE(),GETDATE()),
(12086,1207,10.000, 48.00,19.00, 480.00, 571.20,GETDATE(),GETDATE()),
(12086,1208,12.000, 35.00,19.00, 420.00, 499.80,GETDATE(),GETDATE()),
(12087,1209,12.000, 40.00,19.00, 480.00, 571.20,GETDATE(),GETDATE()),
(12087,1210, 3.000,110.00,19.00, 330.00, 392.70,GETDATE(),GETDATE()),
(12088,1206,12.000, 55.00,19.00, 660.00, 785.40,GETDATE(),GETDATE()),
(12088,1210, 6.000,110.00,19.00, 660.00, 785.40,GETDATE(),GETDATE()),
(12089,1207,10.000, 48.00,19.00, 480.00, 571.20,GETDATE(),GETDATE()),
(12089,1208,12.000, 35.00,19.00, 420.00, 499.80,GETDATE(),GETDATE()),
(12090,1209,12.000, 40.00,19.00, 480.00, 571.20,GETDATE(),GETDATE()),
(12090,1210, 3.000,110.00,19.00, 330.00, 392.70,GETDATE(),GETDATE()),

-- FirstTechnology 14076..14090 (templates F1, F2, F3, F1, F2, F3, ...)
(14076,1406,3.000,780.00,19.00,2340.00,2784.60,GETDATE(),GETDATE()),
(14076,1410,3.000,230.00,19.00, 690.00, 821.10,GETDATE(),GETDATE()),
(14077,1407,4.000,360.00,19.00,1440.00,1713.60,GETDATE(),GETDATE()),
(14077,1408,2.000,490.00,19.00, 980.00,1166.20,GETDATE(),GETDATE()),
(14078,1409,4.000,650.00,19.00,2600.00,3094.00,GETDATE(),GETDATE()),
(14078,1410,3.000,230.00,19.00, 690.00, 821.10,GETDATE(),GETDATE()),
(14079,1406,3.000,780.00,19.00,2340.00,2784.60,GETDATE(),GETDATE()),
(14079,1410,3.000,230.00,19.00, 690.00, 821.10,GETDATE(),GETDATE()),
(14080,1407,4.000,360.00,19.00,1440.00,1713.60,GETDATE(),GETDATE()),
(14080,1408,2.000,490.00,19.00, 980.00,1166.20,GETDATE(),GETDATE()),
(14081,1409,4.000,650.00,19.00,2600.00,3094.00,GETDATE(),GETDATE()),
(14081,1410,3.000,230.00,19.00, 690.00, 821.10,GETDATE(),GETDATE()),
(14082,1406,3.000,780.00,19.00,2340.00,2784.60,GETDATE(),GETDATE()),
(14082,1410,3.000,230.00,19.00, 690.00, 821.10,GETDATE(),GETDATE()),
(14083,1407,4.000,360.00,19.00,1440.00,1713.60,GETDATE(),GETDATE()),
(14083,1408,2.000,490.00,19.00, 980.00,1166.20,GETDATE(),GETDATE()),
(14084,1409,4.000,650.00,19.00,2600.00,3094.00,GETDATE(),GETDATE()),
(14084,1410,3.000,230.00,19.00, 690.00, 821.10,GETDATE(),GETDATE()),
(14085,1406,3.000,780.00,19.00,2340.00,2784.60,GETDATE(),GETDATE()),
(14085,1410,3.000,230.00,19.00, 690.00, 821.10,GETDATE(),GETDATE()),
(14086,1407,4.000,360.00,19.00,1440.00,1713.60,GETDATE(),GETDATE()),
(14086,1408,2.000,490.00,19.00, 980.00,1166.20,GETDATE(),GETDATE()),
(14087,1409,4.000,650.00,19.00,2600.00,3094.00,GETDATE(),GETDATE()),
(14087,1410,3.000,230.00,19.00, 690.00, 821.10,GETDATE(),GETDATE()),
(14088,1406,3.000,780.00,19.00,2340.00,2784.60,GETDATE(),GETDATE()),
(14088,1410,3.000,230.00,19.00, 690.00, 821.10,GETDATE(),GETDATE()),
(14089,1407,4.000,360.00,19.00,1440.00,1713.60,GETDATE(),GETDATE()),
(14089,1408,2.000,490.00,19.00, 980.00,1166.20,GETDATE(),GETDATE()),
(14090,1409,4.000,650.00,19.00,2600.00,3094.00,GETDATE(),GETDATE()),
(14090,1410,3.000,230.00,19.00, 690.00, 821.10,GETDATE(),GETDATE())
GO

-- ============================================================
-- 9. AI PREDICTION LOG SEED – Noxus (7) and Eranow (12)
--
-- 12 rows per enterprise: 4 monthly issuances (Feb / Mar / Apr / May 2026)
-- × 3 horizons (1 / 2 / 3 months ahead). actual_value, absolute_error,
-- percent_error, within_interval, settled_at are deliberately left NULL.
-- PredictionLogService.reconcile() is invoked on every GET /predictions
-- and will fill them in from real commande_vente data – producing real
-- backtest metrics in the dashboard instead of hand-tuned numbers.
-- ============================================================
INSERT [dbo].[prediction_log]
    ([entreprise_id],[prediction_date],[prediction_month],[target_month],[horizon_months],
     [predicted_value],[lower_bound],[upper_bound],[confidence_score],
     [methodology],[model_name],
     [actual_value],[absolute_error],[percent_error],[within_interval],[settled_at],
     [created_at],[updated_at])
VALUES
-- Noxus (id 7) – 4 issuances × 3 horizons = 12 rows
( 7,'2026-02-02','2026-02-01','2026-03-01',1,118000.00,107000.00,129000.00,0.91,N'OLS regression sur historique CA (lag-1, tendance, saison sin/cos), intervalles à 90 % via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',NULL,NULL,NULL,NULL,NULL,GETDATE(),GETDATE()),
( 7,'2026-02-02','2026-02-01','2026-04-01',2,124000.00,109000.00,139000.00,0.84,N'OLS regression sur historique CA (lag-1, tendance, saison sin/cos), intervalles à 90 % via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',NULL,NULL,NULL,NULL,NULL,GETDATE(),GETDATE()),
( 7,'2026-02-02','2026-02-01','2026-05-01',3,128000.00,108000.00,148000.00,0.74,N'OLS regression sur historique CA (lag-1, tendance, saison sin/cos), intervalles à 90 % via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',NULL,NULL,NULL,NULL,NULL,GETDATE(),GETDATE()),
( 7,'2026-03-02','2026-03-01','2026-04-01',1,123000.00,112000.00,134000.00,0.91,N'OLS regression sur historique CA (lag-1, tendance, saison sin/cos), intervalles à 90 % via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',NULL,NULL,NULL,NULL,NULL,GETDATE(),GETDATE()),
( 7,'2026-03-02','2026-03-01','2026-05-01',2,131000.00,116000.00,146000.00,0.84,N'OLS regression sur historique CA (lag-1, tendance, saison sin/cos), intervalles à 90 % via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',NULL,NULL,NULL,NULL,NULL,GETDATE(),GETDATE()),
( 7,'2026-03-02','2026-03-01','2026-06-01',3,135000.00,114000.00,156000.00,0.74,N'OLS regression sur historique CA (lag-1, tendance, saison sin/cos), intervalles à 90 % via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',NULL,NULL,NULL,NULL,NULL,GETDATE(),GETDATE()),
( 7,'2026-04-02','2026-04-01','2026-05-01',1,128000.00,116000.00,140000.00,0.91,N'OLS regression sur historique CA (lag-1, tendance, saison sin/cos), intervalles à 90 % via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',NULL,NULL,NULL,NULL,NULL,GETDATE(),GETDATE()),
( 7,'2026-04-02','2026-04-01','2026-06-01',2,134000.00,118000.00,150000.00,0.84,N'OLS regression sur historique CA (lag-1, tendance, saison sin/cos), intervalles à 90 % via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',NULL,NULL,NULL,NULL,NULL,GETDATE(),GETDATE()),
( 7,'2026-04-02','2026-04-01','2026-07-01',3,138000.00,116000.00,160000.00,0.74,N'OLS regression sur historique CA (lag-1, tendance, saison sin/cos), intervalles à 90 % via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',NULL,NULL,NULL,NULL,NULL,GETDATE(),GETDATE()),
( 7,'2026-05-02','2026-05-01','2026-06-01',1,132000.00,120000.00,144000.00,0.91,N'OLS regression sur historique CA (lag-1, tendance, saison sin/cos), intervalles à 90 % via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',NULL,NULL,NULL,NULL,NULL,GETDATE(),GETDATE()),
( 7,'2026-05-02','2026-05-01','2026-07-01',2,138000.00,122000.00,154000.00,0.84,N'OLS regression sur historique CA (lag-1, tendance, saison sin/cos), intervalles à 90 % via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',NULL,NULL,NULL,NULL,NULL,GETDATE(),GETDATE()),
( 7,'2026-05-02','2026-05-01','2026-08-01',3,142000.00,120000.00,164000.00,0.74,N'OLS regression sur historique CA (lag-1, tendance, saison sin/cos), intervalles à 90 % via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',NULL,NULL,NULL,NULL,NULL,GETDATE(),GETDATE()),

-- Eranow (id 12) – 4 issuances × 3 horizons = 12 rows
(12,'2026-02-02','2026-02-01','2026-03-01',1,135000.00,123000.00,147000.00,0.91,N'OLS regression sur historique CA (lag-1, tendance, saison sin/cos), intervalles à 90 % via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',NULL,NULL,NULL,NULL,NULL,GETDATE(),GETDATE()),
(12,'2026-02-02','2026-02-01','2026-04-01',2,141000.00,124000.00,158000.00,0.84,N'OLS regression sur historique CA (lag-1, tendance, saison sin/cos), intervalles à 90 % via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',NULL,NULL,NULL,NULL,NULL,GETDATE(),GETDATE()),
(12,'2026-02-02','2026-02-01','2026-05-01',3,146000.00,123000.00,169000.00,0.74,N'OLS regression sur historique CA (lag-1, tendance, saison sin/cos), intervalles à 90 % via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',NULL,NULL,NULL,NULL,NULL,GETDATE(),GETDATE()),
(12,'2026-03-02','2026-03-01','2026-04-01',1,140000.00,128000.00,152000.00,0.91,N'OLS regression sur historique CA (lag-1, tendance, saison sin/cos), intervalles à 90 % via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',NULL,NULL,NULL,NULL,NULL,GETDATE(),GETDATE()),
(12,'2026-03-02','2026-03-01','2026-05-01',2,148000.00,130000.00,166000.00,0.84,N'OLS regression sur historique CA (lag-1, tendance, saison sin/cos), intervalles à 90 % via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',NULL,NULL,NULL,NULL,NULL,GETDATE(),GETDATE()),
(12,'2026-03-02','2026-03-01','2026-06-01',3,154000.00,130000.00,178000.00,0.74,N'OLS regression sur historique CA (lag-1, tendance, saison sin/cos), intervalles à 90 % via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',NULL,NULL,NULL,NULL,NULL,GETDATE(),GETDATE()),
(12,'2026-04-02','2026-04-01','2026-05-01',1,146000.00,133000.00,159000.00,0.91,N'OLS regression sur historique CA (lag-1, tendance, saison sin/cos), intervalles à 90 % via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',NULL,NULL,NULL,NULL,NULL,GETDATE(),GETDATE()),
(12,'2026-04-02','2026-04-01','2026-06-01',2,153000.00,135000.00,171000.00,0.84,N'OLS regression sur historique CA (lag-1, tendance, saison sin/cos), intervalles à 90 % via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',NULL,NULL,NULL,NULL,NULL,GETDATE(),GETDATE()),
(12,'2026-04-02','2026-04-01','2026-07-01',3,159000.00,134000.00,184000.00,0.74,N'OLS regression sur historique CA (lag-1, tendance, saison sin/cos), intervalles à 90 % via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',NULL,NULL,NULL,NULL,NULL,GETDATE(),GETDATE()),
(12,'2026-05-02','2026-05-01','2026-06-01',1,151000.00,138000.00,164000.00,0.91,N'OLS regression sur historique CA (lag-1, tendance, saison sin/cos), intervalles à 90 % via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',NULL,NULL,NULL,NULL,NULL,GETDATE(),GETDATE()),
(12,'2026-05-02','2026-05-01','2026-07-01',2,158000.00,140000.00,176000.00,0.84,N'OLS regression sur historique CA (lag-1, tendance, saison sin/cos), intervalles à 90 % via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',NULL,NULL,NULL,NULL,NULL,GETDATE(),GETDATE()),
(12,'2026-05-02','2026-05-01','2026-08-01',3,164000.00,138000.00,190000.00,0.74,N'OLS regression sur historique CA (lag-1, tendance, saison sin/cos), intervalles à 90 % via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',NULL,NULL,NULL,NULL,NULL,GETDATE(),GETDATE())
GO

-- ============================================================
-- 10. SUMMARY
-- ============================================================
PRINT N'Extras seed loaded:'
PRINT N'  - 20 new articles  (5 per enterprise + 5 extra for FirstTechnology)'
PRINT N'  - 20 new article_vente rows + 20 new stock rows'
PRINT N'  - 40 extra commande_achat (10 per enterprise + 10 extra for FirstTechnology)'
PRINT N'  - 65 extra commande_vente (15 per enterprise + 20 extra for FirstTechnology)'
PRINT N'  - 36 prediction_log rows (12 each for Noxus, Eranow, FirstTechnology), actuals filled at runtime'
GO

-- ============================================================
-- 11. ADDITIONAL DATA – FirstTechnology (id 14) ONLY
--
-- Adds 5 more articles (FT-011..FT-015) with article_vente + stock,
-- 10 more commande_achat and 20 more commande_vente referencing them,
-- plus 12 prediction_log rows so the "Historique : Prédictions vs CA Réel"
-- panel is no longer empty for enterprise 14.
--
-- prediction_month range here (2026-05-01 .. 2026-08-01) is intentionally
-- outside the firsttechnology_enterprise_14_ai_seed.sql range
-- (2025-05-01 .. 2026-04-01), so this section is safe whether or not the
-- per-enterprise AI seed has been applied.
-- ============================================================

-- 11.a Articles 1411..1415
SET IDENTITY_INSERT [dbo].[article] ON
INSERT [dbo].[article] ([id],[reference],[nom],[description],[prix_achat],[prix_vente],[actif],[categorie_article_id],[mesure_id],[tva_id],[entreprise_id],[created_at],[updated_at]) VALUES
(1411,N'FT-011',N'Routeur Wi-Fi 6E Pro',N'Routeur tri-bande Wi-Fi 6E AX11000, 8 antennes, QoS, VPN serveur intégré',240.00,380.00,1,14,1,2,14,GETDATE(),GETDATE()),
(1412,N'FT-012',N'NAS 4 Baies Réseau',N'Serveur NAS 4 baies 2.5GbE, RAID 0/1/5/6/10, snapshots, sauvegarde cloud',480.00,720.00,1,14,1,2,14,GETDATE(),GETDATE()),
(1413,N'FT-013',N'Vidéoprojecteur Full HD',N'Vidéoprojecteur Full HD 4500 lumens, HDMI/USB-C, correction trapèze auto',360.00,540.00,1,14,1,2,14,GETDATE(),GETDATE()),
(1414,N'FT-014',N'Scanner Documentaire ADF',N'Scanner recto-verso 60ppm, chargeur ADF 80 feuilles, OCR multilingue',290.00,450.00,1,14,1,2,14,GETDATE(),GETDATE()),
(1415,N'FT-015',N'Casque Réunion VoIP USB',N'Casque stéréo USB-A/USB-C avec micro antibruit, certifié Teams/Zoom',80.00,130.00,1,14,1,2,14,GETDATE(),GETDATE())
SET IDENTITY_INSERT [dbo].[article] OFF
GO

-- 11.b article_vente
SET IDENTITY_INSERT [dbo].[article_vente] ON
INSERT [dbo].[article_vente] ([id],[article_id],[prix_vente_special],[description_vente],[created_at],[updated_at]) VALUES
(1411,1411,360.00,N'Tarif Routeur Wi-Fi 6E intégrateurs réseau',GETDATE(),GETDATE()),
(1412,1412,690.00,N'Remise NAS 4 baies parc serveurs',GETDATE(),GETDATE()),
(1413,1413,510.00,N'Tarif Vidéoprojecteur salles formation',GETDATE(),GETDATE()),
(1414,1414,420.00,N'Promotion Scanner ADF parcs bureautiques',GETDATE(),GETDATE()),
(1415,1415,118.00,N'Tarif Casque VoIP centres d''appel lot >= 10',GETDATE(),GETDATE())
SET IDENTITY_INSERT [dbo].[article_vente] OFF
GO

-- 11.c stock
SET IDENTITY_INSERT [dbo].[stock] ON
INSERT [dbo].[stock] ([id],[article_id],[entreprise_id],[quantite_disponible],[quantite_minimale],[quantite_maximale],[emplacement],[created_at],[updated_at]) VALUES
(1411,1411,14, 55.000, 8.000, 180.000,N'Entrepôt Principal – Étagère IT-F1',GETDATE(),GETDATE()),
(1412,1412,14, 30.000, 5.000, 100.000,N'Entrepôt Principal – Étagère IT-F2',GETDATE(),GETDATE()),
(1413,1413,14, 40.000, 6.000, 130.000,N'Entrepôt Principal – Étagère IT-G1',GETDATE(),GETDATE()),
(1414,1414,14, 50.000, 8.000, 160.000,N'Entrepôt Secondaire – Bac IT-G2',GETDATE(),GETDATE()),
(1415,1415,14,180.000,25.000, 600.000,N'Entrepôt Principal – Étagère IT-H1',GETDATE(),GETDATE())
SET IDENTITY_INSERT [dbo].[stock] OFF
GO

-- 11.d Extra commande_achat 14051..14060 (April 2026)
-- Templates HT:
--   GA: 3×1411(240) + 2×1413(360)         = 1440.00  TVA  273.60  TTC 1713.60
--   GB: 2×1412(480) + 4×1415( 80)         = 1280.00  TVA  243.20  TTC 1523.20
--   GC: 4×1414(290) + 5×1415( 80)         = 1560.00  TVA  296.40  TTC 1856.40
--   GD: 2×1411(240) + 1×1412(480)         =  960.00  TVA  182.40  TTC 1142.40
--   GE: 3×1413(360) + 3×1414(290)         = 1950.00  TVA  370.50  TTC 2320.50
SET IDENTITY_INSERT [dbo].[commande_achat] ON
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES
(14051,N'CA-FT-2026-051','2026-04-02','2026-04-10',N'RECUE',     1440.00,273.60,1713.60,141,14,GETDATE(),GETDATE()),
(14052,N'CA-FT-2026-052','2026-04-04','2026-04-12',N'RECUE',     1280.00,243.20,1523.20,142,14,GETDATE(),GETDATE()),
(14053,N'CA-FT-2026-053','2026-04-06','2026-04-14',N'RECUE',     1560.00,296.40,1856.40,143,14,GETDATE(),GETDATE()),
(14054,N'CA-FT-2026-054','2026-04-08','2026-04-16',N'RECUE',      960.00,182.40,1142.40,144,14,GETDATE(),GETDATE()),
(14055,N'CA-FT-2026-055','2026-04-10','2026-04-18',N'RECUE',     1950.00,370.50,2320.50,141,14,GETDATE(),GETDATE()),
(14056,N'CA-FT-2026-056','2026-04-12','2026-04-20',N'CONFIRMEE', 1440.00,273.60,1713.60,142,14,GETDATE(),GETDATE()),
(14057,N'CA-FT-2026-057','2026-04-14','2026-04-22',N'CONFIRMEE', 1280.00,243.20,1523.20,143,14,GETDATE(),GETDATE()),
(14058,N'CA-FT-2026-058','2026-04-16','2026-04-24',N'CONFIRMEE', 1560.00,296.40,1856.40,144,14,GETDATE(),GETDATE()),
(14059,N'CA-FT-2026-059','2026-04-18',NULL,        N'EN_ATTENTE', 960.00,182.40,1142.40,141,14,GETDATE(),GETDATE()),
(14060,N'CA-FT-2026-060','2026-04-20',NULL,        N'BROUILLON', 1950.00,370.50,2320.50,142,14,GETDATE(),GETDATE())
SET IDENTITY_INSERT [dbo].[commande_achat] OFF
GO

-- 11.e ligne_commande_achat for 14051..14060
INSERT [dbo].[ligne_commande_achat] ([commande_achat_id],[article_id],[quantite],[prix_unitaire_ht],[taux_tva],[montant_ht],[montant_ttc],[created_at],[updated_at]) VALUES
(14051,1411,3.000,240.00,19.00, 720.00, 856.80,GETDATE(),GETDATE()),
(14051,1413,2.000,360.00,19.00, 720.00, 856.80,GETDATE(),GETDATE()),
(14052,1412,2.000,480.00,19.00, 960.00,1142.40,GETDATE(),GETDATE()),
(14052,1415,4.000, 80.00,19.00, 320.00, 380.80,GETDATE(),GETDATE()),
(14053,1414,4.000,290.00,19.00,1160.00,1380.40,GETDATE(),GETDATE()),
(14053,1415,5.000, 80.00,19.00, 400.00, 476.00,GETDATE(),GETDATE()),
(14054,1411,2.000,240.00,19.00, 480.00, 571.20,GETDATE(),GETDATE()),
(14054,1412,1.000,480.00,19.00, 480.00, 571.20,GETDATE(),GETDATE()),
(14055,1413,3.000,360.00,19.00,1080.00,1285.20,GETDATE(),GETDATE()),
(14055,1414,3.000,290.00,19.00, 870.00,1035.30,GETDATE(),GETDATE()),
(14056,1411,3.000,240.00,19.00, 720.00, 856.80,GETDATE(),GETDATE()),
(14056,1413,2.000,360.00,19.00, 720.00, 856.80,GETDATE(),GETDATE()),
(14057,1412,2.000,480.00,19.00, 960.00,1142.40,GETDATE(),GETDATE()),
(14057,1415,4.000, 80.00,19.00, 320.00, 380.80,GETDATE(),GETDATE()),
(14058,1414,4.000,290.00,19.00,1160.00,1380.40,GETDATE(),GETDATE()),
(14058,1415,5.000, 80.00,19.00, 400.00, 476.00,GETDATE(),GETDATE()),
(14059,1411,2.000,240.00,19.00, 480.00, 571.20,GETDATE(),GETDATE()),
(14059,1412,1.000,480.00,19.00, 480.00, 571.20,GETDATE(),GETDATE()),
(14060,1413,3.000,360.00,19.00,1080.00,1285.20,GETDATE(),GETDATE()),
(14060,1414,3.000,290.00,19.00, 870.00,1035.30,GETDATE(),GETDATE())
GO

-- 11.f Extra commande_vente 14091..14110 (Apr–May 2026)
-- Templates HT (vente prices 380/720/540/450/130):
--   H1: 4×1411(380) + 2×1413(540)         = 2600.00  TVA  494.00  TTC 3094.00
--   H2: 2×1412(720) + 6×1415(130)         = 2220.00  TVA  421.80  TTC 2641.80
--   H3: 4×1414(450) + 5×1415(130)         = 2450.00  TVA  465.50  TTC 2915.50
--   H4: 3×1411(380) + 2×1412(720)         = 2580.00  TVA  490.20  TTC 3070.20
--   H5: 2×1413(540) + 4×1414(450)         = 2880.00  TVA  547.20  TTC 3427.20
SET IDENTITY_INSERT [dbo].[commande_vente] ON
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES
(14091,N'CV-FT-2026-091','2026-04-03','2026-04-09',N'LIVREE',    2600.00,494.00,3094.00,141,14,GETDATE(),GETDATE()),
(14092,N'CV-FT-2026-092','2026-04-04','2026-04-10',N'LIVREE',    2220.00,421.80,2641.80,142,14,GETDATE(),GETDATE()),
(14093,N'CV-FT-2026-093','2026-04-06','2026-04-12',N'LIVREE',    2450.00,465.50,2915.50,143,14,GETDATE(),GETDATE()),
(14094,N'CV-FT-2026-094','2026-04-07','2026-04-13',N'LIVREE',    2580.00,490.20,3070.20,144,14,GETDATE(),GETDATE()),
(14095,N'CV-FT-2026-095','2026-04-09','2026-04-15',N'LIVREE',    2880.00,547.20,3427.20,141,14,GETDATE(),GETDATE()),
(14096,N'CV-FT-2026-096','2026-04-11','2026-04-17',N'LIVREE',    2600.00,494.00,3094.00,142,14,GETDATE(),GETDATE()),
(14097,N'CV-FT-2026-097','2026-04-13','2026-04-19',N'LIVREE',    2220.00,421.80,2641.80,143,14,GETDATE(),GETDATE()),
(14098,N'CV-FT-2026-098','2026-04-15','2026-04-21',N'LIVREE',    2450.00,465.50,2915.50,144,14,GETDATE(),GETDATE()),
(14099,N'CV-FT-2026-099','2026-04-16','2026-04-22',N'LIVREE',    2580.00,490.20,3070.20,141,14,GETDATE(),GETDATE()),
(14100,N'CV-FT-2026-100','2026-04-18','2026-04-24',N'LIVREE',    2880.00,547.20,3427.20,142,14,GETDATE(),GETDATE()),
(14101,N'CV-FT-2026-101','2026-04-20','2026-04-26',N'LIVREE',    2600.00,494.00,3094.00,143,14,GETDATE(),GETDATE()),
(14102,N'CV-FT-2026-102','2026-04-22','2026-04-28',N'LIVREE',    2220.00,421.80,2641.80,144,14,GETDATE(),GETDATE()),
(14103,N'CV-FT-2026-103','2026-04-23','2026-04-29',N'CONFIRMEE', 2450.00,465.50,2915.50,141,14,GETDATE(),GETDATE()),
(14104,N'CV-FT-2026-104','2026-04-24','2026-04-30',N'CONFIRMEE', 2580.00,490.20,3070.20,142,14,GETDATE(),GETDATE()),
(14105,N'CV-FT-2026-105','2026-04-25','2026-05-01',N'CONFIRMEE', 2880.00,547.20,3427.20,143,14,GETDATE(),GETDATE()),
(14106,N'CV-FT-2026-106','2026-04-26','2026-05-02',N'CONFIRMEE', 2600.00,494.00,3094.00,144,14,GETDATE(),GETDATE()),
(14107,N'CV-FT-2026-107','2026-04-27','2026-05-03',N'CONFIRMEE', 2220.00,421.80,2641.80,141,14,GETDATE(),GETDATE()),
(14108,N'CV-FT-2026-108','2026-04-28',NULL,        N'EN_ATTENTE',2450.00,465.50,2915.50,142,14,GETDATE(),GETDATE()),
(14109,N'CV-FT-2026-109','2026-04-29',NULL,        N'EN_ATTENTE',2580.00,490.20,3070.20,143,14,GETDATE(),GETDATE()),
(14110,N'CV-FT-2026-110','2026-04-30',NULL,        N'BROUILLON', 2880.00,547.20,3427.20,144,14,GETDATE(),GETDATE())
SET IDENTITY_INSERT [dbo].[commande_vente] OFF
GO

-- 11.g ligne_commande_vente for 14091..14110
INSERT [dbo].[ligne_commande_vente] ([commande_vente_id],[article_id],[quantite],[prix_unitaire_ht],[taux_tva],[montant_ht],[montant_ttc],[created_at],[updated_at]) VALUES
(14091,1411,4.000,380.00,19.00,1520.00,1808.80,GETDATE(),GETDATE()),
(14091,1413,2.000,540.00,19.00,1080.00,1285.20,GETDATE(),GETDATE()),
(14092,1412,2.000,720.00,19.00,1440.00,1713.60,GETDATE(),GETDATE()),
(14092,1415,6.000,130.00,19.00, 780.00, 928.20,GETDATE(),GETDATE()),
(14093,1414,4.000,450.00,19.00,1800.00,2142.00,GETDATE(),GETDATE()),
(14093,1415,5.000,130.00,19.00, 650.00, 773.50,GETDATE(),GETDATE()),
(14094,1411,3.000,380.00,19.00,1140.00,1356.60,GETDATE(),GETDATE()),
(14094,1412,2.000,720.00,19.00,1440.00,1713.60,GETDATE(),GETDATE()),
(14095,1413,2.000,540.00,19.00,1080.00,1285.20,GETDATE(),GETDATE()),
(14095,1414,4.000,450.00,19.00,1800.00,2142.00,GETDATE(),GETDATE()),
(14096,1411,4.000,380.00,19.00,1520.00,1808.80,GETDATE(),GETDATE()),
(14096,1413,2.000,540.00,19.00,1080.00,1285.20,GETDATE(),GETDATE()),
(14097,1412,2.000,720.00,19.00,1440.00,1713.60,GETDATE(),GETDATE()),
(14097,1415,6.000,130.00,19.00, 780.00, 928.20,GETDATE(),GETDATE()),
(14098,1414,4.000,450.00,19.00,1800.00,2142.00,GETDATE(),GETDATE()),
(14098,1415,5.000,130.00,19.00, 650.00, 773.50,GETDATE(),GETDATE()),
(14099,1411,3.000,380.00,19.00,1140.00,1356.60,GETDATE(),GETDATE()),
(14099,1412,2.000,720.00,19.00,1440.00,1713.60,GETDATE(),GETDATE()),
(14100,1413,2.000,540.00,19.00,1080.00,1285.20,GETDATE(),GETDATE()),
(14100,1414,4.000,450.00,19.00,1800.00,2142.00,GETDATE(),GETDATE()),
(14101,1411,4.000,380.00,19.00,1520.00,1808.80,GETDATE(),GETDATE()),
(14101,1413,2.000,540.00,19.00,1080.00,1285.20,GETDATE(),GETDATE()),
(14102,1412,2.000,720.00,19.00,1440.00,1713.60,GETDATE(),GETDATE()),
(14102,1415,6.000,130.00,19.00, 780.00, 928.20,GETDATE(),GETDATE()),
(14103,1414,4.000,450.00,19.00,1800.00,2142.00,GETDATE(),GETDATE()),
(14103,1415,5.000,130.00,19.00, 650.00, 773.50,GETDATE(),GETDATE()),
(14104,1411,3.000,380.00,19.00,1140.00,1356.60,GETDATE(),GETDATE()),
(14104,1412,2.000,720.00,19.00,1440.00,1713.60,GETDATE(),GETDATE()),
(14105,1413,2.000,540.00,19.00,1080.00,1285.20,GETDATE(),GETDATE()),
(14105,1414,4.000,450.00,19.00,1800.00,2142.00,GETDATE(),GETDATE()),
(14106,1411,4.000,380.00,19.00,1520.00,1808.80,GETDATE(),GETDATE()),
(14106,1413,2.000,540.00,19.00,1080.00,1285.20,GETDATE(),GETDATE()),
(14107,1412,2.000,720.00,19.00,1440.00,1713.60,GETDATE(),GETDATE()),
(14107,1415,6.000,130.00,19.00, 780.00, 928.20,GETDATE(),GETDATE()),
(14108,1414,4.000,450.00,19.00,1800.00,2142.00,GETDATE(),GETDATE()),
(14108,1415,5.000,130.00,19.00, 650.00, 773.50,GETDATE(),GETDATE()),
(14109,1411,3.000,380.00,19.00,1140.00,1356.60,GETDATE(),GETDATE()),
(14109,1412,2.000,720.00,19.00,1440.00,1713.60,GETDATE(),GETDATE()),
(14110,1413,2.000,540.00,19.00,1080.00,1285.20,GETDATE(),GETDATE()),
(14110,1414,4.000,450.00,19.00,1800.00,2142.00,GETDATE(),GETDATE())
GO

-- 11.h prediction_log seed for FirstTechnology (id 14)
-- 4 issuances (May / Jun / Jul / Aug 2026) × 3 horizons = 12 rows.
-- actual_value left NULL on purpose: PredictionLogService.reconcile() runs
-- on every GET /api/analytics/predictions and fills it from commande_vente.
INSERT [dbo].[prediction_log]
    ([entreprise_id],[prediction_date],[prediction_month],[target_month],[horizon_months],
     [predicted_value],[lower_bound],[upper_bound],[confidence_score],
     [methodology],[model_name],
     [actual_value],[absolute_error],[percent_error],[within_interval],[settled_at],
     [created_at],[updated_at])
VALUES
(14,'2026-05-02','2026-05-01','2026-06-01',1,128000.00,117000.00,139000.00,0.91,N'OLS regression sur historique CA (lag-1, tendance, saison sin/cos), intervalles à 90 % via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',NULL,NULL,NULL,NULL,NULL,GETDATE(),GETDATE()),
(14,'2026-05-02','2026-05-01','2026-07-01',2,134000.00,118000.00,150000.00,0.84,N'OLS regression sur historique CA (lag-1, tendance, saison sin/cos), intervalles à 90 % via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',NULL,NULL,NULL,NULL,NULL,GETDATE(),GETDATE()),
(14,'2026-05-02','2026-05-01','2026-08-01',3,138000.00,117000.00,159000.00,0.74,N'OLS regression sur historique CA (lag-1, tendance, saison sin/cos), intervalles à 90 % via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',NULL,NULL,NULL,NULL,NULL,GETDATE(),GETDATE()),
(14,'2026-06-02','2026-06-01','2026-07-01',1,133000.00,121000.00,145000.00,0.91,N'OLS regression sur historique CA (lag-1, tendance, saison sin/cos), intervalles à 90 % via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',NULL,NULL,NULL,NULL,NULL,GETDATE(),GETDATE()),
(14,'2026-06-02','2026-06-01','2026-08-01',2,140000.00,124000.00,156000.00,0.84,N'OLS regression sur historique CA (lag-1, tendance, saison sin/cos), intervalles à 90 % via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',NULL,NULL,NULL,NULL,NULL,GETDATE(),GETDATE()),
(14,'2026-06-02','2026-06-01','2026-09-01',3,144000.00,122000.00,166000.00,0.74,N'OLS regression sur historique CA (lag-1, tendance, saison sin/cos), intervalles à 90 % via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',NULL,NULL,NULL,NULL,NULL,GETDATE(),GETDATE()),
(14,'2026-07-02','2026-07-01','2026-08-01',1,138000.00,125000.00,151000.00,0.91,N'OLS regression sur historique CA (lag-1, tendance, saison sin/cos), intervalles à 90 % via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',NULL,NULL,NULL,NULL,NULL,GETDATE(),GETDATE()),
(14,'2026-07-02','2026-07-01','2026-09-01',2,145000.00,128000.00,162000.00,0.84,N'OLS regression sur historique CA (lag-1, tendance, saison sin/cos), intervalles à 90 % via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',NULL,NULL,NULL,NULL,NULL,GETDATE(),GETDATE()),
(14,'2026-07-02','2026-07-01','2026-10-01',3,150000.00,127000.00,173000.00,0.74,N'OLS regression sur historique CA (lag-1, tendance, saison sin/cos), intervalles à 90 % via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',NULL,NULL,NULL,NULL,NULL,GETDATE(),GETDATE()),
(14,'2026-08-02','2026-08-01','2026-09-01',1,142000.00,129000.00,155000.00,0.91,N'OLS regression sur historique CA (lag-1, tendance, saison sin/cos), intervalles à 90 % via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',NULL,NULL,NULL,NULL,NULL,GETDATE(),GETDATE()),
(14,'2026-08-02','2026-08-01','2026-10-01',2,149000.00,131000.00,167000.00,0.84,N'OLS regression sur historique CA (lag-1, tendance, saison sin/cos), intervalles à 90 % via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',NULL,NULL,NULL,NULL,NULL,GETDATE(),GETDATE()),
(14,'2026-08-02','2026-08-01','2026-11-01',3,154000.00,131000.00,177000.00,0.74,N'OLS regression sur historique CA (lag-1, tendance, saison sin/cos), intervalles à 90 % via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',NULL,NULL,NULL,NULL,NULL,GETDATE(),GETDATE())
GO

PRINT N'FirstTechnology (id=14) extras applied:'
PRINT N'  - 5 new articles (FT-011..FT-015) + article_vente + stock'
PRINT N'  - 10 extra commande_achat (14051..14060)'
PRINT N'  - 20 extra commande_vente (14091..14110)'
PRINT N'  - 12 prediction_log rows (issuances 2026-05..2026-08)'
GO

SELECT
    (SELECT COUNT(*) FROM [dbo].[article]
       WHERE [id] IN (706,707,708,709,710,1206,1207,1208,1209,1210,1406,1407,1408,1409,1410)) AS new_articles,
    (SELECT COUNT(*) FROM [dbo].[article]
       WHERE [id] BETWEEN 1411 AND 1415)                       AS new_articles_ft14_extra,
    (SELECT COUNT(*) FROM [dbo].[commande_achat]
       WHERE [id] BETWEEN 7041 AND 7050 OR [id] BETWEEN 12041 AND 12050 OR [id] BETWEEN 14041 AND 14050) AS new_commande_achat,
    (SELECT COUNT(*) FROM [dbo].[commande_achat]
       WHERE [id] BETWEEN 14051 AND 14060)                     AS new_commande_achat_ft14_extra,
    (SELECT COUNT(*) FROM [dbo].[commande_vente]
       WHERE [id] BETWEEN 7076 AND 7090 OR [id] BETWEEN 12076 AND 12090 OR [id] BETWEEN 14076 AND 14090) AS new_commande_vente,
    (SELECT COUNT(*) FROM [dbo].[commande_vente]
       WHERE [id] BETWEEN 14091 AND 14110)                     AS new_commande_vente_ft14_extra,
    (SELECT COUNT(*) FROM [dbo].[prediction_log]
       WHERE [entreprise_id] IN (7,12) AND [prediction_month] BETWEEN '2026-02-01' AND '2026-05-01') AS new_prediction_log_rows,
    (SELECT COUNT(*) FROM [dbo].[prediction_log]
       WHERE [entreprise_id] = 14 AND [prediction_month] BETWEEN '2026-05-01' AND '2026-08-01') AS new_prediction_log_rows_ft14
GO
