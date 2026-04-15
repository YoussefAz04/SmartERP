-- ============================================================
-- NOXUS ENTERPRISE (ID: 7) - AI INTELLIGENCE DASHBOARD DATA
-- ============================================================
-- Purpose : Populate realistic test data for Noxus (entreprise_id = 7)
-- Scope   : Jan-Mar 2026 | 75 sales orders | 40 purchase orders
-- Expected: ~389,796 TND sales vs ~156,366 TND purchases = +233,430 TND margin
--
-- Order templates (purchase A-H / sales SA-SH) cycle across dates,
-- suppliers and customers to ensure diverse, realistic data.
--
-- SAFE TO RE-RUN: All inserts are guarded with IF NOT EXISTS or
-- preceded by DELETE of the same ID range.
-- ============================================================

USE [BusinessManager]
GO

-- ============================================================
-- 1. ENTERPRISE (IF NOT EXISTS)
-- ============================================================
SET IDENTITY_INSERT [dbo].[entreprise] ON
IF NOT EXISTS (SELECT 1 FROM [dbo].[entreprise] WHERE id = 7)
    INSERT [dbo].[entreprise]
        ([id],[nom],[raison_sociale],[adresse],[telephone],[email],[secteur_activite],[date_creation],[created_at],[updated_at])
    VALUES
        (7,N'Noxus',N'Noxus Corporation SARL',N'Zone Industrielle, Tunis',N'+216-78-123-456',N'info@noxus.tn',N'Technology',CAST(N'2024-01-15' AS Date),GETDATE(),GETDATE())
SET IDENTITY_INSERT [dbo].[entreprise] OFF
GO

-- ============================================================
-- 2. LOOKUP DATA (IF NOT EXISTS)
-- ============================================================

-- Mesure
SET IDENTITY_INSERT [dbo].[mesure] ON
IF NOT EXISTS (SELECT 1 FROM [dbo].[mesure] WHERE id = 1)
    INSERT [dbo].[mesure] ([id],[nom],[symbole],[created_at],[updated_at]) VALUES (1,N'Piece',N'Pce',GETDATE(),GETDATE())
SET IDENTITY_INSERT [dbo].[mesure] OFF
GO

-- TVA
SET IDENTITY_INSERT [dbo].[tva] ON
IF NOT EXISTS (SELECT 1 FROM [dbo].[tva] WHERE id = 1)
    INSERT [dbo].[tva] ([id],[taux],[nom],[created_at],[updated_at]) VALUES (1,0.00,N'No VAT',GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[tva] WHERE id = 2)
    INSERT [dbo].[tva] ([id],[taux],[nom],[created_at],[updated_at]) VALUES (2,19.00,N'Standard VAT 19%',GETDATE(),GETDATE())
SET IDENTITY_INSERT [dbo].[tva] OFF
GO

-- Categorie article
SET IDENTITY_INSERT [dbo].[categorie_article] ON
IF NOT EXISTS (SELECT 1 FROM [dbo].[categorie_article] WHERE id = 1)
    INSERT [dbo].[categorie_article] ([id],[nom],[description],[created_at],[updated_at])
    VALUES (1,N'Electronics',N'Electronic equipment and devices',GETDATE(),GETDATE())
SET IDENTITY_INSERT [dbo].[categorie_article] OFF
GO

-- Categorie fournisseur (needed for FK if not null)
SET IDENTITY_INSERT [dbo].[categorie_fournisseur] ON
IF NOT EXISTS (SELECT 1 FROM [dbo].[categorie_fournisseur] WHERE id = 7)
    INSERT [dbo].[categorie_fournisseur] ([id],[nom],[description],[created_at],[updated_at])
    VALUES (7,N'Technology Suppliers',N'IT and electronics wholesale suppliers',GETDATE(),GETDATE())
SET IDENTITY_INSERT [dbo].[categorie_fournisseur] OFF
GO

-- Categorie client
SET IDENTITY_INSERT [dbo].[categorie_client] ON
IF NOT EXISTS (SELECT 1 FROM [dbo].[categorie_client] WHERE id = 7)
    INSERT [dbo].[categorie_client] ([id],[nom],[description],[created_at],[updated_at])
    VALUES (7,N'Corporate',N'Corporate technology clients',GETDATE(),GETDATE())
SET IDENTITY_INSERT [dbo].[categorie_client] OFF
GO

-- ============================================================
-- 3. ARTICLES 701-705 (Electronics for Noxus)
-- prix_achat / prix_vente are HT (excluding TVA 19%)
-- ============================================================
SET IDENTITY_INSERT [dbo].[article] ON
IF NOT EXISTS (SELECT 1 FROM [dbo].[article] WHERE id = 701)
    INSERT [dbo].[article] ([id],[reference],[nom],[description],[prix_achat],[prix_vente],[actif],[categorie_article_id],[mesure_id],[tva_id],[entreprise_id],[created_at],[updated_at])
    VALUES (701,N'NOX-001',N'Laptop ProBook 15"',N'Ordinateur portable haute performance pour professionnels',950.00,1400.00,1,1,1,2,7,GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[article] WHERE id = 702)
    INSERT [dbo].[article] ([id],[reference],[nom],[description],[prix_achat],[prix_vente],[actif],[categorie_article_id],[mesure_id],[tva_id],[entreprise_id],[created_at],[updated_at])
    VALUES (702,N'NOX-002',N'Tablette Galaxy Pro 11"',N'Tablette Android haut de gamme pour usage professionnel',420.00,620.00,1,1,1,2,7,GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[article] WHERE id = 703)
    INSERT [dbo].[article] ([id],[reference],[nom],[description],[prix_achat],[prix_vente],[actif],[categorie_article_id],[mesure_id],[tva_id],[entreprise_id],[created_at],[updated_at])
    VALUES (703,N'NOX-003',N'Smartphone Neon X Pro',N'Smartphone dernière génération 5G double SIM',350.00,520.00,1,1,1,2,7,GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[article] WHERE id = 704)
    INSERT [dbo].[article] ([id],[reference],[nom],[description],[prix_achat],[prix_vente],[actif],[categorie_article_id],[mesure_id],[tva_id],[entreprise_id],[created_at],[updated_at])
    VALUES (704,N'NOX-004',N'Moniteur 27" 4K',N'Écran professionnel Ultra HD 4K IPS',280.00,440.00,1,1,1,2,7,GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[article] WHERE id = 705)
    INSERT [dbo].[article] ([id],[reference],[nom],[description],[prix_achat],[prix_vente],[actif],[categorie_article_id],[mesure_id],[tva_id],[entreprise_id],[created_at],[updated_at])
    VALUES (705,N'NOX-005',N'Clavier Mécanique Pro',N'Clavier mécanique rétroéclairé pour développeurs',75.00,130.00,1,1,1,2,7,GETDATE(),GETDATE())
SET IDENTITY_INSERT [dbo].[article] OFF
GO

-- ============================================================
-- 4. ARTICLE_VENTE 701-705
-- ============================================================
SET IDENTITY_INSERT [dbo].[article_vente] ON
IF NOT EXISTS (SELECT 1 FROM [dbo].[article_vente] WHERE id = 701)
    INSERT [dbo].[article_vente] ([id],[article_id],[prix_vente_special],[description_vente],[created_at],[updated_at])
    VALUES (701,701,1350.00,N'Prix spécial grands comptes Laptop ProBook',GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[article_vente] WHERE id = 702)
    INSERT [dbo].[article_vente] ([id],[article_id],[prix_vente_special],[description_vente],[created_at],[updated_at])
    VALUES (702,702,590.00,N'Tarif préférentiel Tablette Galaxy Pro',GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[article_vente] WHERE id = 703)
    INSERT [dbo].[article_vente] ([id],[article_id],[prix_vente_special],[description_vente],[created_at],[updated_at])
    VALUES (703,703,500.00,N'Promotion Smartphone Neon X Pro',GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[article_vente] WHERE id = 704)
    INSERT [dbo].[article_vente] ([id],[article_id],[prix_vente_special],[description_vente],[created_at],[updated_at])
    VALUES (704,704,420.00,N'Offre moniteur 27" lot de 2+',GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[article_vente] WHERE id = 705)
    INSERT [dbo].[article_vente] ([id],[article_id],[prix_vente_special],[description_vente],[created_at],[updated_at])
    VALUES (705,705,120.00,N'Remise clavier mécanique commande > 5',GETDATE(),GETDATE())
SET IDENTITY_INSERT [dbo].[article_vente] OFF
GO

-- ============================================================
-- 5. FOURNISSEURS 71-74 (for Noxus)
-- ============================================================
SET IDENTITY_INSERT [dbo].[fournisseur] ON
IF NOT EXISTS (SELECT 1 FROM [dbo].[fournisseur] WHERE id = 71)
    INSERT [dbo].[fournisseur] ([id],[nom],[prenom],[email],[telephone],[adresse],[ville],[pays],[categorie_fournisseur_id],[entreprise_id],[created_at],[updated_at])
    VALUES (71,N'TechSupply SARL',N'Ahmed Ben Ali',N'contact@techsupply.tn',N'+216-71-234-567',N'23 Avenue Habib Bourguiba',N'Tunis',N'Tunisie',7,7,GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[fournisseur] WHERE id = 72)
    INSERT [dbo].[fournisseur] ([id],[nom],[prenom],[email],[telephone],[adresse],[ville],[pays],[categorie_fournisseur_id],[entreprise_id],[created_at],[updated_at])
    VALUES (72,N'Global Informatique',N'Fatima Khaled',N'info@globalinfo.tn',N'+216-74-345-678',N'Zone Portuaire, Bloc B',N'Sfax',N'Tunisie',7,7,GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[fournisseur] WHERE id = 73)
    INSERT [dbo].[fournisseur] ([id],[nom],[prenom],[email],[telephone],[adresse],[ville],[pays],[categorie_fournisseur_id],[entreprise_id],[created_at],[updated_at])
    VALUES (73,N'Premium Electronics Ltd',N'Mohammed Saidi',N'orders@premiumelec.tn',N'+216-73-456-789',N'Parc Technologique, Rue 5',N'Sousse',N'Tunisie',7,7,GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[fournisseur] WHERE id = 74)
    INSERT [dbo].[fournisseur] ([id],[nom],[prenom],[email],[telephone],[adresse],[ville],[pays],[categorie_fournisseur_id],[entreprise_id],[created_at],[updated_at])
    VALUES (74,N'QuickTech Distribution',N'Leila Mansouri',N'sales@quicktech.tn',N'+216-76-567-890',N'Centre Commercial Palms, Lot 12',N'Monastir',N'Tunisie',7,7,GETDATE(),GETDATE())
SET IDENTITY_INSERT [dbo].[fournisseur] OFF
GO

-- ============================================================
-- 6. CLIENTS 71-74 (for Noxus)
-- ============================================================
SET IDENTITY_INSERT [dbo].[client] ON
IF NOT EXISTS (SELECT 1 FROM [dbo].[client] WHERE id = 71)
    INSERT [dbo].[client] ([id],[nom],[prenom],[email],[telephone],[adresse],[secteur_activite],[categorie_client_id],[entreprise_id],[created_at],[updated_at])
    VALUES (71,N'Tech Solutions SARL',N'Directeur',N'contact@techsol.tn',N'+216-81-111-222',N'Tunis Tech Park, Bureau 14',N'Informatique',7,7,GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[client] WHERE id = 72)
    INSERT [dbo].[client] ([id],[nom],[prenom],[email],[telephone],[adresse],[secteur_activite],[categorie_client_id],[entreprise_id],[created_at],[updated_at])
    VALUES (72,N'Digital World',N'Responsable Achat',N'info@digitalworld.tn',N'+216-82-222-333',N'Sfax Business Center, Étage 3',N'Commerce',7,7,GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[client] WHERE id = 73)
    INSERT [dbo].[client] ([id],[nom],[prenom],[email],[telephone],[adresse],[secteur_activite],[categorie_client_id],[entreprise_id],[created_at],[updated_at])
    VALUES (73,N'Innovation Hub SARL',N'PDG',N'hello@innovhub.tn',N'+216-83-333-444',N'Pôle Innovation Sousse, Bâtiment C',N'Technologie',7,7,GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[client] WHERE id = 74)
    INSERT [dbo].[client] ([id],[nom],[prenom],[email],[telephone],[adresse],[secteur_activite],[categorie_client_id],[entreprise_id],[created_at],[updated_at])
    VALUES (74,N'Future Systems',N'Gérant',N'ceo@futuresys.tn',N'+216-84-444-555',N'ZI Monastir, Zone B2',N'Systèmes',7,7,GETDATE(),GETDATE())
SET IDENTITY_INSERT [dbo].[client] OFF
GO

-- ============================================================
-- 7. STOCK 701-705 (for Noxus)
-- ============================================================
SET IDENTITY_INSERT [dbo].[stock] ON
IF NOT EXISTS (SELECT 1 FROM [dbo].[stock] WHERE id = 701)
    INSERT [dbo].[stock] ([id],[article_id],[entreprise_id],[quantite_disponible],[quantite_minimale],[quantite_maximale],[emplacement],[created_at],[updated_at])
    VALUES (701,701,7,80.000,10.000,200.000,N'Entrepôt A – Étagère L1',GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[stock] WHERE id = 702)
    INSERT [dbo].[stock] ([id],[article_id],[entreprise_id],[quantite_disponible],[quantite_minimale],[quantite_maximale],[emplacement],[created_at],[updated_at])
    VALUES (702,702,7,150.000,20.000,400.000,N'Entrepôt A – Étagère L2',GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[stock] WHERE id = 703)
    INSERT [dbo].[stock] ([id],[article_id],[entreprise_id],[quantite_disponible],[quantite_minimale],[quantite_maximale],[emplacement],[created_at],[updated_at])
    VALUES (703,703,7,200.000,30.000,600.000,N'Entrepôt B – Armoire M1',GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[stock] WHERE id = 704)
    INSERT [dbo].[stock] ([id],[article_id],[entreprise_id],[quantite_disponible],[quantite_minimale],[quantite_maximale],[emplacement],[created_at],[updated_at])
    VALUES (704,704,7,60.000,5.000,150.000,N'Entrepôt A – Étagère L3',GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[stock] WHERE id = 705)
    INSERT [dbo].[stock] ([id],[article_id],[entreprise_id],[quantite_disponible],[quantite_minimale],[quantite_maximale],[emplacement],[created_at],[updated_at])
    VALUES (705,705,7,300.000,50.000,1000.000,N'Entrepôt B – Bac K5',GETDATE(),GETDATE())
SET IDENTITY_INSERT [dbo].[stock] OFF
GO

-- ============================================================
-- 8. PURCHASE ORDERS (commande_achat) – 40 orders Jan-Mar 2026
--
-- Statut valid values: BROUILLON | EN_ATTENTE | CONFIRMEE | RECUE | ANNULEE
--
-- Templates (HT / TVA @19% / TTC):
--   A  3160.00 /  600.40 / 3760.40   (2 Laptops + 3 Tablettes)
--   B  3850.00 /  731.50 / 4581.50   (5 Tablettes + 5 Smartphones)
--   C  3690.00 /  701.10 / 4391.10   (3 Laptops + 3 Moniteurs)
--   D  2240.00 /  425.60 / 2665.60   (4 Smartphones + 3 Moniteurs)
--   E  2850.00 /  541.50 / 3391.50   (5 Tablettes + 10 Claviers)
--   F  4600.00 /  874.00 / 5474.00   (3 Laptops + 5 Smartphones)
--   G  2240.00 /  425.60 / 2665.60   (4 Tablettes + 2 Moniteurs)
--   H  3650.00 /  693.50 / 4343.50   (2 Laptops + 5 Smartphones)
--
-- Monthly totals:  Jan ≈ 55 537 TND | Feb ≈ 51 015 TND | Mar ≈ 49 813 TND
-- Grand total purchases: ~156 366 TND TTC
-- ============================================================

-- Clean up previous seed run (same ID range)
DELETE FROM [dbo].[ligne_commande_achat] WHERE [commande_achat_id] BETWEEN 7001 AND 7040
DELETE FROM [dbo].[commande_achat]        WHERE [id]                BETWEEN 7001 AND 7040
GO

SET IDENTITY_INSERT [dbo].[commande_achat] ON
GO

-- ---- JANUARY 2026 (14 orders) pattern: A B C D E F G H A B C D E F ----
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (7001,N'CA-NOX-2026-001','2026-01-02','2026-01-12',N'RECUE',3160.00,600.40,3760.40,71,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (7002,N'CA-NOX-2026-002','2026-01-05','2026-01-15',N'RECUE',3850.00,731.50,4581.50,72,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (7003,N'CA-NOX-2026-003','2026-01-07','2026-01-17',N'RECUE',3690.00,701.10,4391.10,73,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (7004,N'CA-NOX-2026-004','2026-01-09','2026-01-19',N'RECUE',2240.00,425.60,2665.60,74,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (7005,N'CA-NOX-2026-005','2026-01-12','2026-01-22',N'RECUE',2850.00,541.50,3391.50,71,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (7006,N'CA-NOX-2026-006','2026-01-14','2026-01-24',N'RECUE',4600.00,874.00,5474.00,72,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (7007,N'CA-NOX-2026-007','2026-01-16','2026-01-26',N'RECUE',2240.00,425.60,2665.60,73,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (7008,N'CA-NOX-2026-008','2026-01-19','2026-01-29',N'RECUE',3650.00,693.50,4343.50,74,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (7009,N'CA-NOX-2026-009','2026-01-21','2026-01-31',N'RECUE',3160.00,600.40,3760.40,71,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (7010,N'CA-NOX-2026-010','2026-01-23','2026-02-02',N'RECUE',3850.00,731.50,4581.50,72,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (7011,N'CA-NOX-2026-011','2026-01-24','2026-02-03',N'RECUE',3690.00,701.10,4391.10,73,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (7012,N'CA-NOX-2026-012','2026-01-26','2026-02-05',N'RECUE',2240.00,425.60,2665.60,74,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (7013,N'CA-NOX-2026-013','2026-01-27','2026-02-06',N'RECUE',2850.00,541.50,3391.50,71,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (7014,N'CA-NOX-2026-014','2026-01-29','2026-02-08',N'RECUE',4600.00,874.00,5474.00,72,7,GETDATE(),GETDATE())

-- ---- FEBRUARY 2026 (13 orders) pattern: G H A B C D E F G H A B C ----
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (7015,N'CA-NOX-2026-015','2026-02-02','2026-02-12',N'RECUE',2240.00,425.60,2665.60,73,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (7016,N'CA-NOX-2026-016','2026-02-04','2026-02-14',N'RECUE',3650.00,693.50,4343.50,74,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (7017,N'CA-NOX-2026-017','2026-02-06','2026-02-16',N'RECUE',3160.00,600.40,3760.40,71,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (7018,N'CA-NOX-2026-018','2026-02-09','2026-02-19',N'RECUE',3850.00,731.50,4581.50,72,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (7019,N'CA-NOX-2026-019','2026-02-11','2026-02-21',N'RECUE',3690.00,701.10,4391.10,73,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (7020,N'CA-NOX-2026-020','2026-02-13','2026-02-23',N'RECUE',2240.00,425.60,2665.60,74,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (7021,N'CA-NOX-2026-021','2026-02-16','2026-02-26',N'RECUE',2850.00,541.50,3391.50,71,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (7022,N'CA-NOX-2026-022','2026-02-18','2026-02-28',N'RECUE',4600.00,874.00,5474.00,72,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (7023,N'CA-NOX-2026-023','2026-02-20','2026-03-02',N'RECUE',2240.00,425.60,2665.60,73,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (7024,N'CA-NOX-2026-024','2026-02-23','2026-03-05',N'RECUE',3650.00,693.50,4343.50,74,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (7025,N'CA-NOX-2026-025','2026-02-24','2026-03-06',N'RECUE',3160.00,600.40,3760.40,71,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (7026,N'CA-NOX-2026-026','2026-02-26','2026-03-08',N'CONFIRMEE',3850.00,731.50,4581.50,72,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (7027,N'CA-NOX-2026-027','2026-02-27','2026-03-09',N'CONFIRMEE',3690.00,701.10,4391.10,73,7,GETDATE(),GETDATE())

-- ---- MARCH 2026 (13 orders) pattern: D E F G H A B C D E F G H ----
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (7028,N'CA-NOX-2026-028','2026-03-02','2026-03-12',N'RECUE',2240.00,425.60,2665.60,74,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (7029,N'CA-NOX-2026-029','2026-03-04','2026-03-14',N'RECUE',2850.00,541.50,3391.50,71,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (7030,N'CA-NOX-2026-030','2026-03-06','2026-03-16',N'RECUE',4600.00,874.00,5474.00,72,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (7031,N'CA-NOX-2026-031','2026-03-09','2026-03-19',N'RECUE',2240.00,425.60,2665.60,73,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (7032,N'CA-NOX-2026-032','2026-03-11','2026-03-21',N'RECUE',3650.00,693.50,4343.50,74,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (7033,N'CA-NOX-2026-033','2026-03-12','2026-03-22',N'RECUE',3160.00,600.40,3760.40,71,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (7034,N'CA-NOX-2026-034','2026-03-14','2026-03-24',N'RECUE',3850.00,731.50,4581.50,72,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (7035,N'CA-NOX-2026-035','2026-03-17','2026-03-27',N'CONFIRMEE',3690.00,701.10,4391.10,73,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (7036,N'CA-NOX-2026-036','2026-03-19','2026-03-29',N'CONFIRMEE',2240.00,425.60,2665.60,74,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (7037,N'CA-NOX-2026-037','2026-03-21','2026-03-31',N'CONFIRMEE',2850.00,541.50,3391.50,71,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (7038,N'CA-NOX-2026-038','2026-03-24',NULL,N'BROUILLON',4600.00,874.00,5474.00,72,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (7039,N'CA-NOX-2026-039','2026-03-26',NULL,N'BROUILLON',2240.00,425.60,2665.60,73,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (7040,N'CA-NOX-2026-040','2026-03-28',NULL,N'BROUILLON',3650.00,693.50,4343.50,74,7,GETDATE(),GETDATE())

SET IDENTITY_INSERT [dbo].[commande_achat] OFF
GO

-- ============================================================
-- 9. PURCHASE ORDER LINE ITEMS (ligne_commande_achat)
--    Two lines per order; article_id references article 701-705.
--    montant_ht = quantite * prix_unitaire_ht
--    montant_ttc = montant_ht * 1.19
-- ============================================================
INSERT [dbo].[ligne_commande_achat] ([commande_achat_id],[article_id],[quantite],[prix_unitaire_ht],[taux_tva],[montant_ht],[montant_ttc],[created_at],[updated_at]) VALUES
-- 7001 Template A
(7001,701,2.000,950.00,19.00,1900.00,2261.00,GETDATE(),GETDATE()),
(7001,702,3.000,420.00,19.00,1260.00,1499.40,GETDATE(),GETDATE()),
-- 7002 Template B
(7002,702,5.000,420.00,19.00,2100.00,2499.00,GETDATE(),GETDATE()),
(7002,703,5.000,350.00,19.00,1750.00,2082.50,GETDATE(),GETDATE()),
-- 7003 Template C
(7003,701,3.000,950.00,19.00,2850.00,3391.50,GETDATE(),GETDATE()),
(7003,704,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
-- 7004 Template D
(7004,703,4.000,350.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(7004,704,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
-- 7005 Template E
(7005,702,5.000,420.00,19.00,2100.00,2499.00,GETDATE(),GETDATE()),
(7005,705,10.000,75.00,19.00,750.00,892.50,GETDATE(),GETDATE()),
-- 7006 Template F
(7006,701,3.000,950.00,19.00,2850.00,3391.50,GETDATE(),GETDATE()),
(7006,703,5.000,350.00,19.00,1750.00,2082.50,GETDATE(),GETDATE()),
-- 7007 Template G
(7007,702,4.000,420.00,19.00,1680.00,1999.20,GETDATE(),GETDATE()),
(7007,704,2.000,280.00,19.00,560.00,666.40,GETDATE(),GETDATE()),
-- 7008 Template H
(7008,701,2.000,950.00,19.00,1900.00,2261.00,GETDATE(),GETDATE()),
(7008,703,5.000,350.00,19.00,1750.00,2082.50,GETDATE(),GETDATE()),
-- 7009 Template A
(7009,701,2.000,950.00,19.00,1900.00,2261.00,GETDATE(),GETDATE()),
(7009,702,3.000,420.00,19.00,1260.00,1499.40,GETDATE(),GETDATE()),
-- 7010 Template B
(7010,702,5.000,420.00,19.00,2100.00,2499.00,GETDATE(),GETDATE()),
(7010,703,5.000,350.00,19.00,1750.00,2082.50,GETDATE(),GETDATE()),
-- 7011 Template C
(7011,701,3.000,950.00,19.00,2850.00,3391.50,GETDATE(),GETDATE()),
(7011,704,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
-- 7012 Template D
(7012,703,4.000,350.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(7012,704,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
-- 7013 Template E
(7013,702,5.000,420.00,19.00,2100.00,2499.00,GETDATE(),GETDATE()),
(7013,705,10.000,75.00,19.00,750.00,892.50,GETDATE(),GETDATE()),
-- 7014 Template F
(7014,701,3.000,950.00,19.00,2850.00,3391.50,GETDATE(),GETDATE()),
(7014,703,5.000,350.00,19.00,1750.00,2082.50,GETDATE(),GETDATE()),
-- 7015 Template G
(7015,702,4.000,420.00,19.00,1680.00,1999.20,GETDATE(),GETDATE()),
(7015,704,2.000,280.00,19.00,560.00,666.40,GETDATE(),GETDATE()),
-- 7016 Template H
(7016,701,2.000,950.00,19.00,1900.00,2261.00,GETDATE(),GETDATE()),
(7016,703,5.000,350.00,19.00,1750.00,2082.50,GETDATE(),GETDATE()),
-- 7017 Template A
(7017,701,2.000,950.00,19.00,1900.00,2261.00,GETDATE(),GETDATE()),
(7017,702,3.000,420.00,19.00,1260.00,1499.40,GETDATE(),GETDATE()),
-- 7018 Template B
(7018,702,5.000,420.00,19.00,2100.00,2499.00,GETDATE(),GETDATE()),
(7018,703,5.000,350.00,19.00,1750.00,2082.50,GETDATE(),GETDATE()),
-- 7019 Template C
(7019,701,3.000,950.00,19.00,2850.00,3391.50,GETDATE(),GETDATE()),
(7019,704,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
-- 7020 Template D
(7020,703,4.000,350.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(7020,704,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
-- 7021 Template E
(7021,702,5.000,420.00,19.00,2100.00,2499.00,GETDATE(),GETDATE()),
(7021,705,10.000,75.00,19.00,750.00,892.50,GETDATE(),GETDATE()),
-- 7022 Template F
(7022,701,3.000,950.00,19.00,2850.00,3391.50,GETDATE(),GETDATE()),
(7022,703,5.000,350.00,19.00,1750.00,2082.50,GETDATE(),GETDATE()),
-- 7023 Template G
(7023,702,4.000,420.00,19.00,1680.00,1999.20,GETDATE(),GETDATE()),
(7023,704,2.000,280.00,19.00,560.00,666.40,GETDATE(),GETDATE()),
-- 7024 Template H
(7024,701,2.000,950.00,19.00,1900.00,2261.00,GETDATE(),GETDATE()),
(7024,703,5.000,350.00,19.00,1750.00,2082.50,GETDATE(),GETDATE()),
-- 7025 Template A
(7025,701,2.000,950.00,19.00,1900.00,2261.00,GETDATE(),GETDATE()),
(7025,702,3.000,420.00,19.00,1260.00,1499.40,GETDATE(),GETDATE()),
-- 7026 Template B
(7026,702,5.000,420.00,19.00,2100.00,2499.00,GETDATE(),GETDATE()),
(7026,703,5.000,350.00,19.00,1750.00,2082.50,GETDATE(),GETDATE()),
-- 7027 Template C
(7027,701,3.000,950.00,19.00,2850.00,3391.50,GETDATE(),GETDATE()),
(7027,704,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
-- 7028 Template D
(7028,703,4.000,350.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(7028,704,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
-- 7029 Template E
(7029,702,5.000,420.00,19.00,2100.00,2499.00,GETDATE(),GETDATE()),
(7029,705,10.000,75.00,19.00,750.00,892.50,GETDATE(),GETDATE()),
-- 7030 Template F
(7030,701,3.000,950.00,19.00,2850.00,3391.50,GETDATE(),GETDATE()),
(7030,703,5.000,350.00,19.00,1750.00,2082.50,GETDATE(),GETDATE()),
-- 7031 Template G
(7031,702,4.000,420.00,19.00,1680.00,1999.20,GETDATE(),GETDATE()),
(7031,704,2.000,280.00,19.00,560.00,666.40,GETDATE(),GETDATE()),
-- 7032 Template H
(7032,701,2.000,950.00,19.00,1900.00,2261.00,GETDATE(),GETDATE()),
(7032,703,5.000,350.00,19.00,1750.00,2082.50,GETDATE(),GETDATE()),
-- 7033 Template A
(7033,701,2.000,950.00,19.00,1900.00,2261.00,GETDATE(),GETDATE()),
(7033,702,3.000,420.00,19.00,1260.00,1499.40,GETDATE(),GETDATE()),
-- 7034 Template B
(7034,702,5.000,420.00,19.00,2100.00,2499.00,GETDATE(),GETDATE()),
(7034,703,5.000,350.00,19.00,1750.00,2082.50,GETDATE(),GETDATE()),
-- 7035 Template C
(7035,701,3.000,950.00,19.00,2850.00,3391.50,GETDATE(),GETDATE()),
(7035,704,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
-- 7036 Template D
(7036,703,4.000,350.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(7036,704,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
-- 7037 Template E
(7037,702,5.000,420.00,19.00,2100.00,2499.00,GETDATE(),GETDATE()),
(7037,705,10.000,75.00,19.00,750.00,892.50,GETDATE(),GETDATE()),
-- 7038 Template F
(7038,701,3.000,950.00,19.00,2850.00,3391.50,GETDATE(),GETDATE()),
(7038,703,5.000,350.00,19.00,1750.00,2082.50,GETDATE(),GETDATE()),
-- 7039 Template G
(7039,702,4.000,420.00,19.00,1680.00,1999.20,GETDATE(),GETDATE()),
(7039,704,2.000,280.00,19.00,560.00,666.40,GETDATE(),GETDATE()),
-- 7040 Template H
(7040,701,2.000,950.00,19.00,1900.00,2261.00,GETDATE(),GETDATE()),
(7040,703,5.000,350.00,19.00,1750.00,2082.50,GETDATE(),GETDATE())
GO

-- ============================================================
-- 10. SALES ORDERS (commande_vente) – 75 orders Jan-Mar 2026
--
-- Statut valid values: BROUILLON | EN_ATTENTE | CONFIRMEE | LIVREE | ANNULEE
--
-- Templates (HT / TVA @19% / TTC):
--   SA  5440.00 / 1033.60 / 6473.60  (3 Laptops + 2 Tablettes)
--   SB  4040.00 /  767.60 / 4807.60  (4 Tablettes + 3 Smartphones)
--   SC  4880.00 /  927.20 / 5807.20  (2 Laptops + 4 Smartphones)
--   SD  3480.00 /  661.20 / 4141.20  (5 Smartphones + 2 Moniteurs)
--   SE  3160.00 /  600.40 / 3760.40  (3 Tablettes + 10 Claviers)
--   SF  3980.00 /  756.20 / 4736.20  (2 Moniteurs + 5 Tablettes)
--   SG  4120.00 /  782.80 / 4902.80  (2 Laptops + 3 Moniteurs)
--   SH  5700.00 / 1083.00 / 6783.00  (5 Tablettes + 5 Smartphones)
--
-- Monthly totals:  Jan ≈ 130 710 TND | Feb ≈ 129 044 TND | Mar ≈ 130 043 TND
-- Grand total sales: ~389 796 TND TTC
-- ============================================================

-- Clean up previous seed run
DELETE FROM [dbo].[ligne_commande_vente] WHERE [commande_vente_id] BETWEEN 7001 AND 7075
DELETE FROM [dbo].[commande_vente]        WHERE [id]               BETWEEN 7001 AND 7075
GO

SET IDENTITY_INSERT [dbo].[commande_vente] ON
GO

-- ---- JANUARY 2026 (25 orders) pattern: SA SB SC SD SE SF SG SH cycling ----
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7001,N'CV-NOX-2026-001','2026-01-02','2026-01-09',N'LIVREE',5440.00,1033.60,6473.60,71,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7002,N'CV-NOX-2026-002','2026-01-03','2026-01-10',N'LIVREE',4040.00,767.60,4807.60,72,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7003,N'CV-NOX-2026-003','2026-01-04','2026-01-11',N'LIVREE',4880.00,927.20,5807.20,73,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7004,N'CV-NOX-2026-004','2026-01-05','2026-01-12',N'LIVREE',3480.00,661.20,4141.20,74,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7005,N'CV-NOX-2026-005','2026-01-06','2026-01-13',N'LIVREE',3160.00,600.40,3760.40,71,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7006,N'CV-NOX-2026-006','2026-01-07','2026-01-14',N'LIVREE',3980.00,756.20,4736.20,72,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7007,N'CV-NOX-2026-007','2026-01-08','2026-01-15',N'LIVREE',4120.00,782.80,4902.80,73,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7008,N'CV-NOX-2026-008','2026-01-09','2026-01-16',N'LIVREE',5700.00,1083.00,6783.00,74,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7009,N'CV-NOX-2026-009','2026-01-10','2026-01-17',N'LIVREE',5440.00,1033.60,6473.60,71,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7010,N'CV-NOX-2026-010','2026-01-11','2026-01-18',N'LIVREE',4040.00,767.60,4807.60,72,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7011,N'CV-NOX-2026-011','2026-01-12','2026-01-19',N'LIVREE',4880.00,927.20,5807.20,73,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7012,N'CV-NOX-2026-012','2026-01-13','2026-01-20',N'LIVREE',3480.00,661.20,4141.20,74,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7013,N'CV-NOX-2026-013','2026-01-14','2026-01-21',N'LIVREE',3160.00,600.40,3760.40,71,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7014,N'CV-NOX-2026-014','2026-01-15','2026-01-22',N'LIVREE',3980.00,756.20,4736.20,72,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7015,N'CV-NOX-2026-015','2026-01-16','2026-01-23',N'LIVREE',4120.00,782.80,4902.80,73,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7016,N'CV-NOX-2026-016','2026-01-17','2026-01-24',N'LIVREE',5700.00,1083.00,6783.00,74,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7017,N'CV-NOX-2026-017','2026-01-18','2026-01-25',N'LIVREE',5440.00,1033.60,6473.60,71,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7018,N'CV-NOX-2026-018','2026-01-19','2026-01-26',N'LIVREE',4040.00,767.60,4807.60,72,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7019,N'CV-NOX-2026-019','2026-01-20','2026-01-27',N'LIVREE',4880.00,927.20,5807.20,73,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7020,N'CV-NOX-2026-020','2026-01-21','2026-01-28',N'LIVREE',3480.00,661.20,4141.20,74,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7021,N'CV-NOX-2026-021','2026-01-22','2026-01-29',N'LIVREE',3160.00,600.40,3760.40,71,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7022,N'CV-NOX-2026-022','2026-01-23','2026-01-30',N'LIVREE',3980.00,756.20,4736.20,72,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7023,N'CV-NOX-2026-023','2026-01-24','2026-01-31',N'LIVREE',4120.00,782.80,4902.80,73,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7024,N'CV-NOX-2026-024','2026-01-25','2026-02-01',N'LIVREE',5700.00,1083.00,6783.00,74,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7025,N'CV-NOX-2026-025','2026-01-26','2026-02-02',N'LIVREE',5440.00,1033.60,6473.60,71,7,GETDATE(),GETDATE())

-- ---- FEBRUARY 2026 (25 orders) pattern: SB SC SD SE SF SG SH SA cycling ----
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7026,N'CV-NOX-2026-026','2026-02-02','2026-02-09',N'LIVREE',4040.00,767.60,4807.60,72,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7027,N'CV-NOX-2026-027','2026-02-03','2026-02-10',N'LIVREE',4880.00,927.20,5807.20,73,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7028,N'CV-NOX-2026-028','2026-02-04','2026-02-11',N'LIVREE',3480.00,661.20,4141.20,74,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7029,N'CV-NOX-2026-029','2026-02-05','2026-02-12',N'LIVREE',3160.00,600.40,3760.40,71,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7030,N'CV-NOX-2026-030','2026-02-06','2026-02-13',N'LIVREE',3980.00,756.20,4736.20,72,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7031,N'CV-NOX-2026-031','2026-02-07','2026-02-14',N'LIVREE',4120.00,782.80,4902.80,73,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7032,N'CV-NOX-2026-032','2026-02-09','2026-02-16',N'LIVREE',5700.00,1083.00,6783.00,74,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7033,N'CV-NOX-2026-033','2026-02-10','2026-02-17',N'LIVREE',5440.00,1033.60,6473.60,71,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7034,N'CV-NOX-2026-034','2026-02-11','2026-02-18',N'LIVREE',4040.00,767.60,4807.60,72,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7035,N'CV-NOX-2026-035','2026-02-12','2026-02-19',N'LIVREE',4880.00,927.20,5807.20,73,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7036,N'CV-NOX-2026-036','2026-02-13','2026-02-20',N'LIVREE',3480.00,661.20,4141.20,74,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7037,N'CV-NOX-2026-037','2026-02-14','2026-02-21',N'LIVREE',3160.00,600.40,3760.40,71,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7038,N'CV-NOX-2026-038','2026-02-16','2026-02-23',N'LIVREE',3980.00,756.20,4736.20,72,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7039,N'CV-NOX-2026-039','2026-02-17','2026-02-24',N'LIVREE',4120.00,782.80,4902.80,73,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7040,N'CV-NOX-2026-040','2026-02-18','2026-02-25',N'LIVREE',5700.00,1083.00,6783.00,74,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7041,N'CV-NOX-2026-041','2026-02-19','2026-02-26',N'LIVREE',5440.00,1033.60,6473.60,71,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7042,N'CV-NOX-2026-042','2026-02-20','2026-02-27',N'LIVREE',4040.00,767.60,4807.60,72,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7043,N'CV-NOX-2026-043','2026-02-21','2026-02-28',N'LIVREE',4880.00,927.20,5807.20,73,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7044,N'CV-NOX-2026-044','2026-02-23','2026-03-02',N'LIVREE',3480.00,661.20,4141.20,74,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7045,N'CV-NOX-2026-045','2026-02-24','2026-03-03',N'LIVREE',3160.00,600.40,3760.40,71,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7046,N'CV-NOX-2026-046','2026-02-25','2026-03-04',N'LIVREE',3980.00,756.20,4736.20,72,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7047,N'CV-NOX-2026-047','2026-02-26','2026-03-05',N'LIVREE',4120.00,782.80,4902.80,73,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7048,N'CV-NOX-2026-048','2026-02-27','2026-03-06',N'LIVREE',5700.00,1083.00,6783.00,74,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7049,N'CV-NOX-2026-049','2026-02-28','2026-03-07',N'LIVREE',5440.00,1033.60,6473.60,71,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7050,N'CV-NOX-2026-050','2026-02-28','2026-03-07',N'LIVREE',4040.00,767.60,4807.60,72,7,GETDATE(),GETDATE())

-- ---- MARCH 2026 (25 orders) pattern: SC SD SE SF SG SH SA SB cycling ----
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7051,N'CV-NOX-2026-051','2026-03-02','2026-03-09',N'LIVREE',4880.00,927.20,5807.20,73,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7052,N'CV-NOX-2026-052','2026-03-03','2026-03-10',N'LIVREE',3480.00,661.20,4141.20,74,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7053,N'CV-NOX-2026-053','2026-03-04','2026-03-11',N'LIVREE',3160.00,600.40,3760.40,71,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7054,N'CV-NOX-2026-054','2026-03-05','2026-03-12',N'LIVREE',3980.00,756.20,4736.20,72,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7055,N'CV-NOX-2026-055','2026-03-06','2026-03-13',N'LIVREE',4120.00,782.80,4902.80,73,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7056,N'CV-NOX-2026-056','2026-03-07','2026-03-14',N'LIVREE',5700.00,1083.00,6783.00,74,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7057,N'CV-NOX-2026-057','2026-03-09','2026-03-16',N'LIVREE',5440.00,1033.60,6473.60,71,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7058,N'CV-NOX-2026-058','2026-03-10','2026-03-17',N'LIVREE',4040.00,767.60,4807.60,72,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7059,N'CV-NOX-2026-059','2026-03-11','2026-03-18',N'LIVREE',4880.00,927.20,5807.20,73,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7060,N'CV-NOX-2026-060','2026-03-12','2026-03-19',N'LIVREE',3480.00,661.20,4141.20,74,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7061,N'CV-NOX-2026-061','2026-03-13','2026-03-20',N'LIVREE',3160.00,600.40,3760.40,71,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7062,N'CV-NOX-2026-062','2026-03-14','2026-03-21',N'LIVREE',3980.00,756.20,4736.20,72,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7063,N'CV-NOX-2026-063','2026-03-16','2026-03-23',N'LIVREE',4120.00,782.80,4902.80,73,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7064,N'CV-NOX-2026-064','2026-03-17','2026-03-24',N'LIVREE',5700.00,1083.00,6783.00,74,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7065,N'CV-NOX-2026-065','2026-03-18','2026-03-25',N'LIVREE',5440.00,1033.60,6473.60,71,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7066,N'CV-NOX-2026-066','2026-03-19','2026-03-26',N'LIVREE',4040.00,767.60,4807.60,72,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7067,N'CV-NOX-2026-067','2026-03-20','2026-03-27',N'LIVREE',4880.00,927.20,5807.20,73,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7068,N'CV-NOX-2026-068','2026-03-21','2026-03-28',N'LIVREE',3480.00,661.20,4141.20,74,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7069,N'CV-NOX-2026-069','2026-03-23','2026-03-30',N'LIVREE',3160.00,600.40,3760.40,71,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7070,N'CV-NOX-2026-070','2026-03-24','2026-03-31',N'LIVREE',3980.00,756.20,4736.20,72,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7071,N'CV-NOX-2026-071','2026-03-25',NULL,N'CONFIRMEE',4120.00,782.80,4902.80,73,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7072,N'CV-NOX-2026-072','2026-03-26',NULL,N'CONFIRMEE',5700.00,1083.00,6783.00,74,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7073,N'CV-NOX-2026-073','2026-03-27',NULL,N'CONFIRMEE',5440.00,1033.60,6473.60,71,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7074,N'CV-NOX-2026-074','2026-03-28',NULL,N'BROUILLON',4040.00,767.60,4807.60,72,7,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (7075,N'CV-NOX-2026-075','2026-03-30',NULL,N'BROUILLON',4880.00,927.20,5807.20,73,7,GETDATE(),GETDATE())

SET IDENTITY_INSERT [dbo].[commande_vente] OFF
GO

-- ============================================================
-- 11. SALES ORDER LINE ITEMS (ligne_commande_vente)
--     Two lines per order; article_id references article 701-705.
--     Template mapping repeated per 8-order cycle.
-- ============================================================
INSERT [dbo].[ligne_commande_vente] ([commande_vente_id],[article_id],[quantite],[prix_unitaire_ht],[taux_tva],[montant_ht],[montant_ttc],[created_at],[updated_at]) VALUES
-- 7001 SA: 3 Laptops + 2 Tablettes
(7001,701,3.000,1400.00,19.00,4200.00,4998.00,GETDATE(),GETDATE()),
(7001,702,2.000,620.00,19.00,1240.00,1475.60,GETDATE(),GETDATE()),
-- 7002 SB: 4 Tablettes + 3 Smartphones
(7002,702,4.000,620.00,19.00,2480.00,2951.20,GETDATE(),GETDATE()),
(7002,703,3.000,520.00,19.00,1560.00,1856.40,GETDATE(),GETDATE()),
-- 7003 SC: 2 Laptops + 4 Smartphones
(7003,701,2.000,1400.00,19.00,2800.00,3332.00,GETDATE(),GETDATE()),
(7003,703,4.000,520.00,19.00,2080.00,2475.20,GETDATE(),GETDATE()),
-- 7004 SD: 5 Smartphones + 2 Moniteurs
(7004,703,5.000,520.00,19.00,2600.00,3094.00,GETDATE(),GETDATE()),
(7004,704,2.000,440.00,19.00,880.00,1047.20,GETDATE(),GETDATE()),
-- 7005 SE: 3 Tablettes + 10 Claviers
(7005,702,3.000,620.00,19.00,1860.00,2213.40,GETDATE(),GETDATE()),
(7005,705,10.000,130.00,19.00,1300.00,1547.00,GETDATE(),GETDATE()),
-- 7006 SF: 2 Moniteurs + 5 Tablettes
(7006,704,2.000,440.00,19.00,880.00,1047.20,GETDATE(),GETDATE()),
(7006,702,5.000,620.00,19.00,3100.00,3689.00,GETDATE(),GETDATE()),
-- 7007 SG: 2 Laptops + 3 Moniteurs
(7007,701,2.000,1400.00,19.00,2800.00,3332.00,GETDATE(),GETDATE()),
(7007,704,3.000,440.00,19.00,1320.00,1570.80,GETDATE(),GETDATE()),
-- 7008 SH: 5 Tablettes + 5 Smartphones
(7008,702,5.000,620.00,19.00,3100.00,3689.00,GETDATE(),GETDATE()),
(7008,703,5.000,520.00,19.00,2600.00,3094.00,GETDATE(),GETDATE()),
-- 7009 SA
(7009,701,3.000,1400.00,19.00,4200.00,4998.00,GETDATE(),GETDATE()),
(7009,702,2.000,620.00,19.00,1240.00,1475.60,GETDATE(),GETDATE()),
-- 7010 SB
(7010,702,4.000,620.00,19.00,2480.00,2951.20,GETDATE(),GETDATE()),
(7010,703,3.000,520.00,19.00,1560.00,1856.40,GETDATE(),GETDATE()),
-- 7011 SC
(7011,701,2.000,1400.00,19.00,2800.00,3332.00,GETDATE(),GETDATE()),
(7011,703,4.000,520.00,19.00,2080.00,2475.20,GETDATE(),GETDATE()),
-- 7012 SD
(7012,703,5.000,520.00,19.00,2600.00,3094.00,GETDATE(),GETDATE()),
(7012,704,2.000,440.00,19.00,880.00,1047.20,GETDATE(),GETDATE()),
-- 7013 SE
(7013,702,3.000,620.00,19.00,1860.00,2213.40,GETDATE(),GETDATE()),
(7013,705,10.000,130.00,19.00,1300.00,1547.00,GETDATE(),GETDATE()),
-- 7014 SF
(7014,704,2.000,440.00,19.00,880.00,1047.20,GETDATE(),GETDATE()),
(7014,702,5.000,620.00,19.00,3100.00,3689.00,GETDATE(),GETDATE()),
-- 7015 SG
(7015,701,2.000,1400.00,19.00,2800.00,3332.00,GETDATE(),GETDATE()),
(7015,704,3.000,440.00,19.00,1320.00,1570.80,GETDATE(),GETDATE()),
-- 7016 SH
(7016,702,5.000,620.00,19.00,3100.00,3689.00,GETDATE(),GETDATE()),
(7016,703,5.000,520.00,19.00,2600.00,3094.00,GETDATE(),GETDATE()),
-- 7017 SA
(7017,701,3.000,1400.00,19.00,4200.00,4998.00,GETDATE(),GETDATE()),
(7017,702,2.000,620.00,19.00,1240.00,1475.60,GETDATE(),GETDATE()),
-- 7018 SB
(7018,702,4.000,620.00,19.00,2480.00,2951.20,GETDATE(),GETDATE()),
(7018,703,3.000,520.00,19.00,1560.00,1856.40,GETDATE(),GETDATE()),
-- 7019 SC
(7019,701,2.000,1400.00,19.00,2800.00,3332.00,GETDATE(),GETDATE()),
(7019,703,4.000,520.00,19.00,2080.00,2475.20,GETDATE(),GETDATE()),
-- 7020 SD
(7020,703,5.000,520.00,19.00,2600.00,3094.00,GETDATE(),GETDATE()),
(7020,704,2.000,440.00,19.00,880.00,1047.20,GETDATE(),GETDATE()),
-- 7021 SE
(7021,702,3.000,620.00,19.00,1860.00,2213.40,GETDATE(),GETDATE()),
(7021,705,10.000,130.00,19.00,1300.00,1547.00,GETDATE(),GETDATE()),
-- 7022 SF
(7022,704,2.000,440.00,19.00,880.00,1047.20,GETDATE(),GETDATE()),
(7022,702,5.000,620.00,19.00,3100.00,3689.00,GETDATE(),GETDATE()),
-- 7023 SG
(7023,701,2.000,1400.00,19.00,2800.00,3332.00,GETDATE(),GETDATE()),
(7023,704,3.000,440.00,19.00,1320.00,1570.80,GETDATE(),GETDATE()),
-- 7024 SH
(7024,702,5.000,620.00,19.00,3100.00,3689.00,GETDATE(),GETDATE()),
(7024,703,5.000,520.00,19.00,2600.00,3094.00,GETDATE(),GETDATE()),
-- 7025 SA
(7025,701,3.000,1400.00,19.00,4200.00,4998.00,GETDATE(),GETDATE()),
(7025,702,2.000,620.00,19.00,1240.00,1475.60,GETDATE(),GETDATE()),
-- 7026 SB
(7026,702,4.000,620.00,19.00,2480.00,2951.20,GETDATE(),GETDATE()),
(7026,703,3.000,520.00,19.00,1560.00,1856.40,GETDATE(),GETDATE()),
-- 7027 SC
(7027,701,2.000,1400.00,19.00,2800.00,3332.00,GETDATE(),GETDATE()),
(7027,703,4.000,520.00,19.00,2080.00,2475.20,GETDATE(),GETDATE()),
-- 7028 SD
(7028,703,5.000,520.00,19.00,2600.00,3094.00,GETDATE(),GETDATE()),
(7028,704,2.000,440.00,19.00,880.00,1047.20,GETDATE(),GETDATE()),
-- 7029 SE
(7029,702,3.000,620.00,19.00,1860.00,2213.40,GETDATE(),GETDATE()),
(7029,705,10.000,130.00,19.00,1300.00,1547.00,GETDATE(),GETDATE()),
-- 7030 SF
(7030,704,2.000,440.00,19.00,880.00,1047.20,GETDATE(),GETDATE()),
(7030,702,5.000,620.00,19.00,3100.00,3689.00,GETDATE(),GETDATE()),
-- 7031 SG
(7031,701,2.000,1400.00,19.00,2800.00,3332.00,GETDATE(),GETDATE()),
(7031,704,3.000,440.00,19.00,1320.00,1570.80,GETDATE(),GETDATE()),
-- 7032 SH
(7032,702,5.000,620.00,19.00,3100.00,3689.00,GETDATE(),GETDATE()),
(7032,703,5.000,520.00,19.00,2600.00,3094.00,GETDATE(),GETDATE()),
-- 7033 SA
(7033,701,3.000,1400.00,19.00,4200.00,4998.00,GETDATE(),GETDATE()),
(7033,702,2.000,620.00,19.00,1240.00,1475.60,GETDATE(),GETDATE()),
-- 7034 SB
(7034,702,4.000,620.00,19.00,2480.00,2951.20,GETDATE(),GETDATE()),
(7034,703,3.000,520.00,19.00,1560.00,1856.40,GETDATE(),GETDATE()),
-- 7035 SC
(7035,701,2.000,1400.00,19.00,2800.00,3332.00,GETDATE(),GETDATE()),
(7035,703,4.000,520.00,19.00,2080.00,2475.20,GETDATE(),GETDATE()),
-- 7036 SD
(7036,703,5.000,520.00,19.00,2600.00,3094.00,GETDATE(),GETDATE()),
(7036,704,2.000,440.00,19.00,880.00,1047.20,GETDATE(),GETDATE()),
-- 7037 SE
(7037,702,3.000,620.00,19.00,1860.00,2213.40,GETDATE(),GETDATE()),
(7037,705,10.000,130.00,19.00,1300.00,1547.00,GETDATE(),GETDATE()),
-- 7038 SF
(7038,704,2.000,440.00,19.00,880.00,1047.20,GETDATE(),GETDATE()),
(7038,702,5.000,620.00,19.00,3100.00,3689.00,GETDATE(),GETDATE()),
-- 7039 SG
(7039,701,2.000,1400.00,19.00,2800.00,3332.00,GETDATE(),GETDATE()),
(7039,704,3.000,440.00,19.00,1320.00,1570.80,GETDATE(),GETDATE()),
-- 7040 SH
(7040,702,5.000,620.00,19.00,3100.00,3689.00,GETDATE(),GETDATE()),
(7040,703,5.000,520.00,19.00,2600.00,3094.00,GETDATE(),GETDATE()),
-- 7041 SA
(7041,701,3.000,1400.00,19.00,4200.00,4998.00,GETDATE(),GETDATE()),
(7041,702,2.000,620.00,19.00,1240.00,1475.60,GETDATE(),GETDATE()),
-- 7042 SB
(7042,702,4.000,620.00,19.00,2480.00,2951.20,GETDATE(),GETDATE()),
(7042,703,3.000,520.00,19.00,1560.00,1856.40,GETDATE(),GETDATE()),
-- 7043 SC
(7043,701,2.000,1400.00,19.00,2800.00,3332.00,GETDATE(),GETDATE()),
(7043,703,4.000,520.00,19.00,2080.00,2475.20,GETDATE(),GETDATE()),
-- 7044 SD
(7044,703,5.000,520.00,19.00,2600.00,3094.00,GETDATE(),GETDATE()),
(7044,704,2.000,440.00,19.00,880.00,1047.20,GETDATE(),GETDATE()),
-- 7045 SE
(7045,702,3.000,620.00,19.00,1860.00,2213.40,GETDATE(),GETDATE()),
(7045,705,10.000,130.00,19.00,1300.00,1547.00,GETDATE(),GETDATE()),
-- 7046 SF
(7046,704,2.000,440.00,19.00,880.00,1047.20,GETDATE(),GETDATE()),
(7046,702,5.000,620.00,19.00,3100.00,3689.00,GETDATE(),GETDATE()),
-- 7047 SG
(7047,701,2.000,1400.00,19.00,2800.00,3332.00,GETDATE(),GETDATE()),
(7047,704,3.000,440.00,19.00,1320.00,1570.80,GETDATE(),GETDATE()),
-- 7048 SH
(7048,702,5.000,620.00,19.00,3100.00,3689.00,GETDATE(),GETDATE()),
(7048,703,5.000,520.00,19.00,2600.00,3094.00,GETDATE(),GETDATE()),
-- 7049 SA
(7049,701,3.000,1400.00,19.00,4200.00,4998.00,GETDATE(),GETDATE()),
(7049,702,2.000,620.00,19.00,1240.00,1475.60,GETDATE(),GETDATE()),
-- 7050 SB
(7050,702,4.000,620.00,19.00,2480.00,2951.20,GETDATE(),GETDATE()),
(7050,703,3.000,520.00,19.00,1560.00,1856.40,GETDATE(),GETDATE()),
-- 7051 SC
(7051,701,2.000,1400.00,19.00,2800.00,3332.00,GETDATE(),GETDATE()),
(7051,703,4.000,520.00,19.00,2080.00,2475.20,GETDATE(),GETDATE()),
-- 7052 SD
(7052,703,5.000,520.00,19.00,2600.00,3094.00,GETDATE(),GETDATE()),
(7052,704,2.000,440.00,19.00,880.00,1047.20,GETDATE(),GETDATE()),
-- 7053 SE
(7053,702,3.000,620.00,19.00,1860.00,2213.40,GETDATE(),GETDATE()),
(7053,705,10.000,130.00,19.00,1300.00,1547.00,GETDATE(),GETDATE()),
-- 7054 SF
(7054,704,2.000,440.00,19.00,880.00,1047.20,GETDATE(),GETDATE()),
(7054,702,5.000,620.00,19.00,3100.00,3689.00,GETDATE(),GETDATE()),
-- 7055 SG
(7055,701,2.000,1400.00,19.00,2800.00,3332.00,GETDATE(),GETDATE()),
(7055,704,3.000,440.00,19.00,1320.00,1570.80,GETDATE(),GETDATE()),
-- 7056 SH
(7056,702,5.000,620.00,19.00,3100.00,3689.00,GETDATE(),GETDATE()),
(7056,703,5.000,520.00,19.00,2600.00,3094.00,GETDATE(),GETDATE()),
-- 7057 SA
(7057,701,3.000,1400.00,19.00,4200.00,4998.00,GETDATE(),GETDATE()),
(7057,702,2.000,620.00,19.00,1240.00,1475.60,GETDATE(),GETDATE()),
-- 7058 SB
(7058,702,4.000,620.00,19.00,2480.00,2951.20,GETDATE(),GETDATE()),
(7058,703,3.000,520.00,19.00,1560.00,1856.40,GETDATE(),GETDATE()),
-- 7059 SC
(7059,701,2.000,1400.00,19.00,2800.00,3332.00,GETDATE(),GETDATE()),
(7059,703,4.000,520.00,19.00,2080.00,2475.20,GETDATE(),GETDATE()),
-- 7060 SD
(7060,703,5.000,520.00,19.00,2600.00,3094.00,GETDATE(),GETDATE()),
(7060,704,2.000,440.00,19.00,880.00,1047.20,GETDATE(),GETDATE()),
-- 7061 SE
(7061,702,3.000,620.00,19.00,1860.00,2213.40,GETDATE(),GETDATE()),
(7061,705,10.000,130.00,19.00,1300.00,1547.00,GETDATE(),GETDATE()),
-- 7062 SF
(7062,704,2.000,440.00,19.00,880.00,1047.20,GETDATE(),GETDATE()),
(7062,702,5.000,620.00,19.00,3100.00,3689.00,GETDATE(),GETDATE()),
-- 7063 SG
(7063,701,2.000,1400.00,19.00,2800.00,3332.00,GETDATE(),GETDATE()),
(7063,704,3.000,440.00,19.00,1320.00,1570.80,GETDATE(),GETDATE()),
-- 7064 SH
(7064,702,5.000,620.00,19.00,3100.00,3689.00,GETDATE(),GETDATE()),
(7064,703,5.000,520.00,19.00,2600.00,3094.00,GETDATE(),GETDATE()),
-- 7065 SA
(7065,701,3.000,1400.00,19.00,4200.00,4998.00,GETDATE(),GETDATE()),
(7065,702,2.000,620.00,19.00,1240.00,1475.60,GETDATE(),GETDATE()),
-- 7066 SB
(7066,702,4.000,620.00,19.00,2480.00,2951.20,GETDATE(),GETDATE()),
(7066,703,3.000,520.00,19.00,1560.00,1856.40,GETDATE(),GETDATE()),
-- 7067 SC
(7067,701,2.000,1400.00,19.00,2800.00,3332.00,GETDATE(),GETDATE()),
(7067,703,4.000,520.00,19.00,2080.00,2475.20,GETDATE(),GETDATE()),
-- 7068 SD
(7068,703,5.000,520.00,19.00,2600.00,3094.00,GETDATE(),GETDATE()),
(7068,704,2.000,440.00,19.00,880.00,1047.20,GETDATE(),GETDATE()),
-- 7069 SE
(7069,702,3.000,620.00,19.00,1860.00,2213.40,GETDATE(),GETDATE()),
(7069,705,10.000,130.00,19.00,1300.00,1547.00,GETDATE(),GETDATE()),
-- 7070 SF
(7070,704,2.000,440.00,19.00,880.00,1047.20,GETDATE(),GETDATE()),
(7070,702,5.000,620.00,19.00,3100.00,3689.00,GETDATE(),GETDATE()),
-- 7071 SG
(7071,701,2.000,1400.00,19.00,2800.00,3332.00,GETDATE(),GETDATE()),
(7071,704,3.000,440.00,19.00,1320.00,1570.80,GETDATE(),GETDATE()),
-- 7072 SH
(7072,702,5.000,620.00,19.00,3100.00,3689.00,GETDATE(),GETDATE()),
(7072,703,5.000,520.00,19.00,2600.00,3094.00,GETDATE(),GETDATE()),
-- 7073 SA
(7073,701,3.000,1400.00,19.00,4200.00,4998.00,GETDATE(),GETDATE()),
(7073,702,2.000,620.00,19.00,1240.00,1475.60,GETDATE(),GETDATE()),
-- 7074 SB
(7074,702,4.000,620.00,19.00,2480.00,2951.20,GETDATE(),GETDATE()),
(7074,703,3.000,520.00,19.00,1560.00,1856.40,GETDATE(),GETDATE()),
-- 7075 SC
(7075,701,2.000,1400.00,19.00,2800.00,3332.00,GETDATE(),GETDATE()),
(7075,703,4.000,520.00,19.00,2080.00,2475.20,GETDATE(),GETDATE())
GO

-- ============================================================
-- 12. SUMMARY VALIDATION QUERY
-- ============================================================
PRINT N''
PRINT N'============================================================'
PRINT N' NOXUS ENTERPRISE (ID 7) – DATA INSERTION COMPLETE'
PRINT N'============================================================'
PRINT N''

SELECT
    N'PURCHASE ORDERS' AS [Type],
    COUNT(*)           AS [Total Orders],
    SUM(montant_ht)    AS [Total HT],
    SUM(montant_tva)   AS [Total TVA],
    SUM(montant_ttc)   AS [Total TTC]
FROM [dbo].[commande_achat]
WHERE entreprise_id = 7
  AND id BETWEEN 7001 AND 7040

UNION ALL

SELECT
    N'SALES ORDERS'    AS [Type],
    COUNT(*)           AS [Total Orders],
    SUM(montant_ht)    AS [Total HT],
    SUM(montant_tva)   AS [Total TVA],
    SUM(montant_ttc)   AS [Total TTC]
FROM [dbo].[commande_vente]
WHERE entreprise_id = 7
  AND id BETWEEN 7001 AND 7075
GO

SELECT
    YEAR(date_commande) AS [Year],
    MONTH(date_commande) AS [Month],
    N'ACHAT' AS [Type],
    COUNT(*) AS [Orders],
    SUM(montant_ttc) AS [Total TTC]
FROM [dbo].[commande_achat]
WHERE entreprise_id = 7 AND id BETWEEN 7001 AND 7040
GROUP BY YEAR(date_commande), MONTH(date_commande)

UNION ALL

SELECT
    YEAR(date_commande), MONTH(date_commande),
    N'VENTE', COUNT(*), SUM(montant_ttc)
FROM [dbo].[commande_vente]
WHERE entreprise_id = 7 AND id BETWEEN 7001 AND 7075
GROUP BY YEAR(date_commande), MONTH(date_commande)

ORDER BY [Year], [Month], [Type]
GO

