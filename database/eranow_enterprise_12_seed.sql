-- ============================================================
-- ERANOW ENTERPRISE (ID: 12) - COSMETICS & BEAUTY DATA
-- ============================================================
-- Purpose : Populate realistic test data for Eranow (entreprise_id = 12)
-- Sector  : Cosmetics, Perfumery & Beauty Care
-- Scope   : Jan-Mar 2026 | 75 sales orders | 40 purchase orders
-- TVA     : 19 % on every line
--
-- Pricing templates are aligned with article prix_achat / prix_vente HT
-- so that line items sum exactly to the commande header montant_ht / ttc.
--
-- SAFE TO RE-RUN: IF NOT EXISTS guards and DELETE ranges for order IDs.
-- ============================================================

USE [BusinessManager]
GO

-- ============================================================
-- 1. ENTERPRISE (IF NOT EXISTS)
-- ============================================================
SET IDENTITY_INSERT [dbo].[entreprise] ON
IF NOT EXISTS (SELECT 1 FROM [dbo].[entreprise] WHERE id = 12)
    INSERT [dbo].[entreprise]
        ([id],[nom],[raison_sociale],[adresse],[telephone],[email],[site_web],[secteur_activite],[description],[date_creation],[created_at],[updated_at])
    VALUES
        (12,N'Eranow',N'Eranow Cosmetics & Beauty SARL',N'Immeuble Azur, Les Berges du Lac, Tunis',N'+216-71-840-120',N'contact@eranow.tn',N'https://www.eranow.tn',N'Cosmétiques & Beauté',N'Distribution de produits cosmétiques, parfums et soins de beauté haut de gamme',CAST(N'2024-03-20' AS Date),GETDATE(),GETDATE())
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

-- TVA (0 % and 19 %)
SET IDENTITY_INSERT [dbo].[tva] ON
IF NOT EXISTS (SELECT 1 FROM [dbo].[tva] WHERE id = 1)
    INSERT [dbo].[tva] ([id],[taux],[nom],[created_at],[updated_at]) VALUES (1,0.00,N'No VAT',GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[tva] WHERE id = 2)
    INSERT [dbo].[tva] ([id],[taux],[nom],[created_at],[updated_at]) VALUES (2,19.00,N'Standard VAT 19%',GETDATE(),GETDATE())
SET IDENTITY_INSERT [dbo].[tva] OFF
GO

-- Categorie article (Cosmetics)
SET IDENTITY_INSERT [dbo].[categorie_article] ON
IF NOT EXISTS (SELECT 1 FROM [dbo].[categorie_article] WHERE id = 12)
    INSERT [dbo].[categorie_article] ([id],[nom],[description],[created_at],[updated_at])
    VALUES (12,N'Cosmetiques',N'Parfums, soins visage, maquillage et huiles essentielles',GETDATE(),GETDATE())
SET IDENTITY_INSERT [dbo].[categorie_article] OFF
GO

-- Categorie fournisseur
SET IDENTITY_INSERT [dbo].[categorie_fournisseur] ON
IF NOT EXISTS (SELECT 1 FROM [dbo].[categorie_fournisseur] WHERE id = 12)
    INSERT [dbo].[categorie_fournisseur] ([id],[nom],[description],[created_at],[updated_at])
    VALUES (12,N'Fournisseurs Cosmetiques',N'Laboratoires et distributeurs cosmetiques en gros',GETDATE(),GETDATE())
SET IDENTITY_INSERT [dbo].[categorie_fournisseur] OFF
GO

-- Categorie client
SET IDENTITY_INSERT [dbo].[categorie_client] ON
IF NOT EXISTS (SELECT 1 FROM [dbo].[categorie_client] WHERE id = 12)
    INSERT [dbo].[categorie_client] ([id],[nom],[description],[created_at],[updated_at])
    VALUES (12,N'Professionnels Beaute',N'Instituts, parfumeries, pharmacies et salons partenaires',GETDATE(),GETDATE())
SET IDENTITY_INSERT [dbo].[categorie_client] OFF
GO

-- ============================================================
-- 3. ARTICLES 1201-1205 (Cosmetics for Eranow)
--    prix_achat / prix_vente are HT (excluding TVA 19 %)
-- ============================================================
SET IDENTITY_INSERT [dbo].[article] ON
IF NOT EXISTS (SELECT 1 FROM [dbo].[article] WHERE id = 1201)
    INSERT [dbo].[article] ([id],[reference],[nom],[description],[prix_achat],[prix_vente],[actif],[categorie_article_id],[mesure_id],[tva_id],[entreprise_id],[created_at],[updated_at])
    VALUES (1201,N'ERA-001',N'Coffret Parfum Premium 100ml',N'Coffret parfum haut de gamme longue tenue – eau de parfum 100ml',950.00,1400.00,1,12,1,2,12,GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[article] WHERE id = 1202)
    INSERT [dbo].[article] ([id],[reference],[nom],[description],[prix_achat],[prix_vente],[actif],[categorie_article_id],[mesure_id],[tva_id],[entreprise_id],[created_at],[updated_at])
    VALUES (1202,N'ERA-002',N'Sérum Anti-Âge Professionnel 50ml',N'Sérum concentré anti-rides enrichi en acide hyaluronique et vitamine C',420.00,620.00,1,12,1,2,12,GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[article] WHERE id = 1203)
    INSERT [dbo].[article] ([id],[reference],[nom],[description],[prix_achat],[prix_vente],[actif],[categorie_article_id],[mesure_id],[tva_id],[entreprise_id],[created_at],[updated_at])
    VALUES (1203,N'ERA-003',N'Crème Hydratante Bio 200ml',N'Crème hydratante visage bio certifiée, tous types de peaux',350.00,520.00,1,12,1,2,12,GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[article] WHERE id = 1204)
    INSERT [dbo].[article] ([id],[reference],[nom],[description],[prix_achat],[prix_vente],[actif],[categorie_article_id],[mesure_id],[tva_id],[entreprise_id],[created_at],[updated_at])
    VALUES (1204,N'ERA-004',N'Palette Maquillage Deluxe',N'Palette professionnelle 24 teintes mates et satinées, haute pigmentation',280.00,440.00,1,12,1,2,12,GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[article] WHERE id = 1205)
    INSERT [dbo].[article] ([id],[reference],[nom],[description],[prix_achat],[prix_vente],[actif],[categorie_article_id],[mesure_id],[tva_id],[entreprise_id],[created_at],[updated_at])
    VALUES (1205,N'ERA-005',N'Huile Essentielle Bio 50ml',N'Huile essentielle pure 100% naturelle certifiée Ecocert, lot de 1',75.00,130.00,1,12,1,2,12,GETDATE(),GETDATE())
SET IDENTITY_INSERT [dbo].[article] OFF
GO

-- ============================================================
-- 4. ARTICLE_VENTE 1201-1205
-- ============================================================
SET IDENTITY_INSERT [dbo].[article_vente] ON
IF NOT EXISTS (SELECT 1 FROM [dbo].[article_vente] WHERE id = 1201)
    INSERT [dbo].[article_vente] ([id],[article_id],[prix_vente_special],[description_vente],[created_at],[updated_at])
    VALUES (1201,1201,1350.00,N'Prix spécial grands comptes Coffret Parfum Premium',GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[article_vente] WHERE id = 1202)
    INSERT [dbo].[article_vente] ([id],[article_id],[prix_vente_special],[description_vente],[created_at],[updated_at])
    VALUES (1202,1202,590.00,N'Tarif préférentiel Sérum Anti-Âge revendeurs',GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[article_vente] WHERE id = 1203)
    INSERT [dbo].[article_vente] ([id],[article_id],[prix_vente_special],[description_vente],[created_at],[updated_at])
    VALUES (1203,1203,500.00,N'Promotion Crème Hydratante Bio spa partenaires',GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[article_vente] WHERE id = 1204)
    INSERT [dbo].[article_vente] ([id],[article_id],[prix_vente_special],[description_vente],[created_at],[updated_at])
    VALUES (1204,1204,420.00,N'Offre palette maquillage commandes > 10',GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[article_vente] WHERE id = 1205)
    INSERT [dbo].[article_vente] ([id],[article_id],[prix_vente_special],[description_vente],[created_at],[updated_at])
    VALUES (1205,1205,120.00,N'Remise huile essentielle lot > 5',GETDATE(),GETDATE())
SET IDENTITY_INSERT [dbo].[article_vente] OFF
GO

-- ============================================================
-- 5. FOURNISSEURS 121-124 (for Eranow)
-- ============================================================
SET IDENTITY_INSERT [dbo].[fournisseur] ON
IF NOT EXISTS (SELECT 1 FROM [dbo].[fournisseur] WHERE id = 121)
    INSERT [dbo].[fournisseur] ([id],[nom],[prenom],[email],[telephone],[adresse],[ville],[pays],[categorie_fournisseur_id],[entreprise_id],[created_at],[updated_at])
    VALUES (121,N'Orient Beauty Supplies',N'Karim Bouzid',N'contact@orientbeauty.tn',N'+216-71-981-200',N'18 Rue Ibn Khaldoun',N'Tunis',N'Tunisie',12,12,GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[fournisseur] WHERE id = 122)
    INSERT [dbo].[fournisseur] ([id],[nom],[prenom],[email],[telephone],[adresse],[ville],[pays],[categorie_fournisseur_id],[entreprise_id],[created_at],[updated_at])
    VALUES (122,N'Nature & Cosmétiques SARL',N'Sonia Gharbi',N'info@naturecosm.tn',N'+216-74-552-301',N'Zone Industrielle Poudrière',N'Sfax',N'Tunisie',12,12,GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[fournisseur] WHERE id = 123)
    INSERT [dbo].[fournisseur] ([id],[nom],[prenom],[email],[telephone],[adresse],[ville],[pays],[categorie_fournisseur_id],[entreprise_id],[created_at],[updated_at])
    VALUES (123,N'Méditerra Pharma',N'Walid Trabelsi',N'commandes@medipharma.tn',N'+216-73-710-402',N'Technopole El Ghazela, Bloc H',N'Ariana',N'Tunisie',12,12,GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[fournisseur] WHERE id = 124)
    INSERT [dbo].[fournisseur] ([id],[nom],[prenom],[email],[telephone],[adresse],[ville],[pays],[categorie_fournisseur_id],[entreprise_id],[created_at],[updated_at])
    VALUES (124,N'AromaLab Distribution',N'Nadia Hamrouni',N'sales@aromalab.tn',N'+216-72-460-508',N'Avenue Farhat Hached',N'Sousse',N'Tunisie',12,12,GETDATE(),GETDATE())
SET IDENTITY_INSERT [dbo].[fournisseur] OFF
GO

-- ============================================================
-- 6. CLIENTS 121-124 (for Eranow)
-- ============================================================
SET IDENTITY_INSERT [dbo].[client] ON
IF NOT EXISTS (SELECT 1 FROM [dbo].[client] WHERE id = 121)
    INSERT [dbo].[client] ([id],[nom],[prenom],[email],[telephone],[adresse],[secteur_activite],[categorie_client_id],[entreprise_id],[created_at],[updated_at])
    VALUES (121,N'Institut Élégance Beauté',N'Directrice',N'contact@elegance-beaute.tn',N'+216-25-111-201',N'Les Berges du Lac II, Tour B',N'Spa & Soins',12,12,GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[client] WHERE id = 122)
    INSERT [dbo].[client] ([id],[nom],[prenom],[email],[telephone],[adresse],[secteur_activite],[categorie_client_id],[entreprise_id],[created_at],[updated_at])
    VALUES (122,N'Parfumerie Oasis',N'Responsable Achats',N'achats@parfumerieoasis.tn',N'+216-26-222-302',N'Centre-Ville Sfax, Rue Mongi Slim',N'Parfumerie',12,12,GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[client] WHERE id = 123)
    INSERT [dbo].[client] ([id],[nom],[prenom],[email],[telephone],[adresse],[secteur_activite],[categorie_client_id],[entreprise_id],[created_at],[updated_at])
    VALUES (123,N'Pharmacie Al Hikma',N'Gérant',N'pharmacie.alhikma@tn.com',N'+216-27-333-403',N'Avenue Bourguiba, Sousse',N'Para-Pharmacie',12,12,GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[client] WHERE id = 124)
    INSERT [dbo].[client] ([id],[nom],[prenom],[email],[telephone],[adresse],[secteur_activite],[categorie_client_id],[entreprise_id],[created_at],[updated_at])
    VALUES (124,N'Salon Royal Coiffure',N'Propriétaire',N'royal.coiffure@tn.com',N'+216-28-444-504',N'Rue de la République, Monastir',N'Coiffure & Esthétique',12,12,GETDATE(),GETDATE())
SET IDENTITY_INSERT [dbo].[client] OFF
GO

-- ============================================================
-- 7. STOCK 1201-1205 (for Eranow)
-- ============================================================
SET IDENTITY_INSERT [dbo].[stock] ON
IF NOT EXISTS (SELECT 1 FROM [dbo].[stock] WHERE id = 1201)
    INSERT [dbo].[stock] ([id],[article_id],[entreprise_id],[quantite_disponible],[quantite_minimale],[quantite_maximale],[emplacement],[created_at],[updated_at])
    VALUES (1201,1201,12,80.000,10.000,200.000,N'Entrepôt Principal – Étagère A1',GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[stock] WHERE id = 1202)
    INSERT [dbo].[stock] ([id],[article_id],[entreprise_id],[quantite_disponible],[quantite_minimale],[quantite_maximale],[emplacement],[created_at],[updated_at])
    VALUES (1202,1202,12,150.000,20.000,400.000,N'Entrepôt Principal – Étagère A2',GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[stock] WHERE id = 1203)
    INSERT [dbo].[stock] ([id],[article_id],[entreprise_id],[quantite_disponible],[quantite_minimale],[quantite_maximale],[emplacement],[created_at],[updated_at])
    VALUES (1203,1203,12,200.000,30.000,600.000,N'Entrepôt Principal – Étagère B1',GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[stock] WHERE id = 1204)
    INSERT [dbo].[stock] ([id],[article_id],[entreprise_id],[quantite_disponible],[quantite_minimale],[quantite_maximale],[emplacement],[created_at],[updated_at])
    VALUES (1204,1204,12,60.000,5.000,150.000,N'Entrepôt Secondaire – Armoire M3',GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[stock] WHERE id = 1205)
    INSERT [dbo].[stock] ([id],[article_id],[entreprise_id],[quantite_disponible],[quantite_minimale],[quantite_maximale],[emplacement],[created_at],[updated_at])
    VALUES (1205,1205,12,300.000,50.000,1000.000,N'Entrepôt Secondaire – Bac K2',GETDATE(),GETDATE())
SET IDENTITY_INSERT [dbo].[stock] OFF
GO

-- ============================================================
-- 8. PURCHASE ORDERS (commande_achat) – 40 orders Jan-Mar 2026
--
-- Statut values: BROUILLON | EN_ATTENTE | CONFIRMEE | RECUE | ANNULEE
--
-- Templates (HT / TVA @19 % / TTC):
--   A  3160.00 /  600.40 / 3760.40   (2x1201 + 3x1202)
--   B  3850.00 /  731.50 / 4581.50   (5x1202 + 5x1203)
--   C  3690.00 /  701.10 / 4391.10   (3x1201 + 3x1204)
--   D  2240.00 /  425.60 / 2665.60   (4x1203 + 3x1204)
--   E  2850.00 /  541.50 / 3391.50   (5x1202 + 10x1205)
--   F  4600.00 /  874.00 / 5474.00   (3x1201 + 5x1203)
--   G  2240.00 /  425.60 / 2665.60   (4x1202 + 2x1204)
--   H  3650.00 /  693.50 / 4343.50   (2x1201 + 5x1203)
-- ============================================================

-- Clean up previous seed run (same ID range)
DELETE FROM [dbo].[ligne_commande_achat] WHERE [commande_achat_id] BETWEEN 12001 AND 12040
DELETE FROM [dbo].[commande_achat]        WHERE [id]                BETWEEN 12001 AND 12040
GO

SET IDENTITY_INSERT [dbo].[commande_achat] ON
GO

INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (12001,N'CA-ERA-2026-001','2026-01-02','2026-01-12',N'RECUE',3160.00,600.40,3760.40,121,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (12002,N'CA-ERA-2026-002','2026-01-05','2026-01-15',N'RECUE',3850.00,731.50,4581.50,122,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (12003,N'CA-ERA-2026-003','2026-01-07','2026-01-17',N'RECUE',3690.00,701.10,4391.10,123,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (12004,N'CA-ERA-2026-004','2026-01-09','2026-01-19',N'RECUE',2240.00,425.60,2665.60,124,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (12005,N'CA-ERA-2026-005','2026-01-12','2026-01-22',N'RECUE',2850.00,541.50,3391.50,121,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (12006,N'CA-ERA-2026-006','2026-01-14','2026-01-24',N'RECUE',4600.00,874.00,5474.00,122,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (12007,N'CA-ERA-2026-007','2026-01-16','2026-01-26',N'RECUE',2240.00,425.60,2665.60,123,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (12008,N'CA-ERA-2026-008','2026-01-19','2026-01-29',N'RECUE',3650.00,693.50,4343.50,124,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (12009,N'CA-ERA-2026-009','2026-01-21','2026-01-31',N'RECUE',3160.00,600.40,3760.40,121,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (12010,N'CA-ERA-2026-010','2026-01-23','2026-02-02',N'RECUE',3850.00,731.50,4581.50,122,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (12011,N'CA-ERA-2026-011','2026-01-24','2026-02-03',N'RECUE',3690.00,701.10,4391.10,123,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (12012,N'CA-ERA-2026-012','2026-01-26','2026-02-05',N'RECUE',2240.00,425.60,2665.60,124,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (12013,N'CA-ERA-2026-013','2026-01-27','2026-02-06',N'RECUE',2850.00,541.50,3391.50,121,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (12014,N'CA-ERA-2026-014','2026-01-29','2026-02-08',N'RECUE',4600.00,874.00,5474.00,122,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (12015,N'CA-ERA-2026-015','2026-02-02','2026-02-12',N'RECUE',2240.00,425.60,2665.60,123,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (12016,N'CA-ERA-2026-016','2026-02-04','2026-02-14',N'RECUE',3650.00,693.50,4343.50,124,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (12017,N'CA-ERA-2026-017','2026-02-06','2026-02-16',N'RECUE',3160.00,600.40,3760.40,121,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (12018,N'CA-ERA-2026-018','2026-02-09','2026-02-19',N'RECUE',3850.00,731.50,4581.50,122,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (12019,N'CA-ERA-2026-019','2026-02-11','2026-02-21',N'RECUE',3690.00,701.10,4391.10,123,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (12020,N'CA-ERA-2026-020','2026-02-13','2026-02-23',N'RECUE',2240.00,425.60,2665.60,124,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (12021,N'CA-ERA-2026-021','2026-02-16','2026-02-26',N'RECUE',2850.00,541.50,3391.50,121,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (12022,N'CA-ERA-2026-022','2026-02-18','2026-02-28',N'RECUE',4600.00,874.00,5474.00,122,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (12023,N'CA-ERA-2026-023','2026-02-20','2026-03-02',N'RECUE',2240.00,425.60,2665.60,123,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (12024,N'CA-ERA-2026-024','2026-02-23','2026-03-05',N'RECUE',3650.00,693.50,4343.50,124,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (12025,N'CA-ERA-2026-025','2026-02-24','2026-03-06',N'RECUE',3160.00,600.40,3760.40,121,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (12026,N'CA-ERA-2026-026','2026-02-26','2026-03-08',N'CONFIRMEE',3850.00,731.50,4581.50,122,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (12027,N'CA-ERA-2026-027','2026-02-27','2026-03-09',N'CONFIRMEE',3690.00,701.10,4391.10,123,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (12028,N'CA-ERA-2026-028','2026-03-02','2026-03-12',N'RECUE',2240.00,425.60,2665.60,124,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (12029,N'CA-ERA-2026-029','2026-03-04','2026-03-14',N'RECUE',2850.00,541.50,3391.50,121,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (12030,N'CA-ERA-2026-030','2026-03-06','2026-03-16',N'RECUE',4600.00,874.00,5474.00,122,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (12031,N'CA-ERA-2026-031','2026-03-09','2026-03-19',N'RECUE',2240.00,425.60,2665.60,123,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (12032,N'CA-ERA-2026-032','2026-03-11','2026-03-21',N'RECUE',3650.00,693.50,4343.50,124,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (12033,N'CA-ERA-2026-033','2026-03-12','2026-03-22',N'RECUE',3160.00,600.40,3760.40,121,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (12034,N'CA-ERA-2026-034','2026-03-14','2026-03-24',N'RECUE',3850.00,731.50,4581.50,122,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (12035,N'CA-ERA-2026-035','2026-03-17','2026-03-27',N'CONFIRMEE',3690.00,701.10,4391.10,123,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (12036,N'CA-ERA-2026-036','2026-03-19','2026-03-29',N'CONFIRMEE',2240.00,425.60,2665.60,124,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (12037,N'CA-ERA-2026-037','2026-03-21','2026-03-31',N'CONFIRMEE',2850.00,541.50,3391.50,121,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (12038,N'CA-ERA-2026-038','2026-03-24',NULL,N'BROUILLON',4600.00,874.00,5474.00,122,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (12039,N'CA-ERA-2026-039','2026-03-26',NULL,N'BROUILLON',2240.00,425.60,2665.60,123,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (12040,N'CA-ERA-2026-040','2026-03-28',NULL,N'BROUILLON',3650.00,693.50,4343.50,124,12,GETDATE(),GETDATE())

SET IDENTITY_INSERT [dbo].[commande_achat] OFF
GO

-- ============================================================
-- 9. PURCHASE ORDER LINE ITEMS (ligne_commande_achat)
--    montant_ht  = quantite * prix_unitaire_ht
--    montant_ttc = montant_ht * 1.19
-- ============================================================
INSERT [dbo].[ligne_commande_achat] ([commande_achat_id],[article_id],[quantite],[prix_unitaire_ht],[taux_tva],[montant_ht],[montant_ttc],[created_at],[updated_at]) VALUES
(12001,1201,2.000,950.00,19.00,1900.00,2261.00,GETDATE(),GETDATE()),
(12001,1202,3.000,420.00,19.00,1260.00,1499.40,GETDATE(),GETDATE()),
(12002,1202,5.000,420.00,19.00,2100.00,2499.00,GETDATE(),GETDATE()),
(12002,1203,5.000,350.00,19.00,1750.00,2082.50,GETDATE(),GETDATE()),
(12003,1201,3.000,950.00,19.00,2850.00,3391.50,GETDATE(),GETDATE()),
(12003,1204,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
(12004,1203,4.000,350.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(12004,1204,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
(12005,1202,5.000,420.00,19.00,2100.00,2499.00,GETDATE(),GETDATE()),
(12005,1205,10.000,75.00,19.00,750.00,892.50,GETDATE(),GETDATE()),
(12006,1201,3.000,950.00,19.00,2850.00,3391.50,GETDATE(),GETDATE()),
(12006,1203,5.000,350.00,19.00,1750.00,2082.50,GETDATE(),GETDATE()),
(12007,1202,4.000,420.00,19.00,1680.00,1999.20,GETDATE(),GETDATE()),
(12007,1204,2.000,280.00,19.00,560.00,666.40,GETDATE(),GETDATE()),
(12008,1201,2.000,950.00,19.00,1900.00,2261.00,GETDATE(),GETDATE()),
(12008,1203,5.000,350.00,19.00,1750.00,2082.50,GETDATE(),GETDATE()),
(12009,1201,2.000,950.00,19.00,1900.00,2261.00,GETDATE(),GETDATE()),
(12009,1202,3.000,420.00,19.00,1260.00,1499.40,GETDATE(),GETDATE()),
(12010,1202,5.000,420.00,19.00,2100.00,2499.00,GETDATE(),GETDATE()),
(12010,1203,5.000,350.00,19.00,1750.00,2082.50,GETDATE(),GETDATE()),
(12011,1201,3.000,950.00,19.00,2850.00,3391.50,GETDATE(),GETDATE()),
(12011,1204,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
(12012,1203,4.000,350.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(12012,1204,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
(12013,1202,5.000,420.00,19.00,2100.00,2499.00,GETDATE(),GETDATE()),
(12013,1205,10.000,75.00,19.00,750.00,892.50,GETDATE(),GETDATE()),
(12014,1201,3.000,950.00,19.00,2850.00,3391.50,GETDATE(),GETDATE()),
(12014,1203,5.000,350.00,19.00,1750.00,2082.50,GETDATE(),GETDATE()),
(12015,1202,4.000,420.00,19.00,1680.00,1999.20,GETDATE(),GETDATE()),
(12015,1204,2.000,280.00,19.00,560.00,666.40,GETDATE(),GETDATE()),
(12016,1201,2.000,950.00,19.00,1900.00,2261.00,GETDATE(),GETDATE()),
(12016,1203,5.000,350.00,19.00,1750.00,2082.50,GETDATE(),GETDATE()),
(12017,1201,2.000,950.00,19.00,1900.00,2261.00,GETDATE(),GETDATE()),
(12017,1202,3.000,420.00,19.00,1260.00,1499.40,GETDATE(),GETDATE()),
(12018,1202,5.000,420.00,19.00,2100.00,2499.00,GETDATE(),GETDATE()),
(12018,1203,5.000,350.00,19.00,1750.00,2082.50,GETDATE(),GETDATE()),
(12019,1201,3.000,950.00,19.00,2850.00,3391.50,GETDATE(),GETDATE()),
(12019,1204,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
(12020,1203,4.000,350.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(12020,1204,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
(12021,1202,5.000,420.00,19.00,2100.00,2499.00,GETDATE(),GETDATE()),
(12021,1205,10.000,75.00,19.00,750.00,892.50,GETDATE(),GETDATE()),
(12022,1201,3.000,950.00,19.00,2850.00,3391.50,GETDATE(),GETDATE()),
(12022,1203,5.000,350.00,19.00,1750.00,2082.50,GETDATE(),GETDATE()),
(12023,1202,4.000,420.00,19.00,1680.00,1999.20,GETDATE(),GETDATE()),
(12023,1204,2.000,280.00,19.00,560.00,666.40,GETDATE(),GETDATE()),
(12024,1201,2.000,950.00,19.00,1900.00,2261.00,GETDATE(),GETDATE()),
(12024,1203,5.000,350.00,19.00,1750.00,2082.50,GETDATE(),GETDATE()),
(12025,1201,2.000,950.00,19.00,1900.00,2261.00,GETDATE(),GETDATE()),
(12025,1202,3.000,420.00,19.00,1260.00,1499.40,GETDATE(),GETDATE()),
(12026,1202,5.000,420.00,19.00,2100.00,2499.00,GETDATE(),GETDATE()),
(12026,1203,5.000,350.00,19.00,1750.00,2082.50,GETDATE(),GETDATE()),
(12027,1201,3.000,950.00,19.00,2850.00,3391.50,GETDATE(),GETDATE()),
(12027,1204,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
(12028,1203,4.000,350.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(12028,1204,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
(12029,1202,5.000,420.00,19.00,2100.00,2499.00,GETDATE(),GETDATE()),
(12029,1205,10.000,75.00,19.00,750.00,892.50,GETDATE(),GETDATE()),
(12030,1201,3.000,950.00,19.00,2850.00,3391.50,GETDATE(),GETDATE()),
(12030,1203,5.000,350.00,19.00,1750.00,2082.50,GETDATE(),GETDATE()),
(12031,1202,4.000,420.00,19.00,1680.00,1999.20,GETDATE(),GETDATE()),
(12031,1204,2.000,280.00,19.00,560.00,666.40,GETDATE(),GETDATE()),
(12032,1201,2.000,950.00,19.00,1900.00,2261.00,GETDATE(),GETDATE()),
(12032,1203,5.000,350.00,19.00,1750.00,2082.50,GETDATE(),GETDATE()),
(12033,1201,2.000,950.00,19.00,1900.00,2261.00,GETDATE(),GETDATE()),
(12033,1202,3.000,420.00,19.00,1260.00,1499.40,GETDATE(),GETDATE()),
(12034,1202,5.000,420.00,19.00,2100.00,2499.00,GETDATE(),GETDATE()),
(12034,1203,5.000,350.00,19.00,1750.00,2082.50,GETDATE(),GETDATE()),
(12035,1201,3.000,950.00,19.00,2850.00,3391.50,GETDATE(),GETDATE()),
(12035,1204,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
(12036,1203,4.000,350.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(12036,1204,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
(12037,1202,5.000,420.00,19.00,2100.00,2499.00,GETDATE(),GETDATE()),
(12037,1205,10.000,75.00,19.00,750.00,892.50,GETDATE(),GETDATE()),
(12038,1201,3.000,950.00,19.00,2850.00,3391.50,GETDATE(),GETDATE()),
(12038,1203,5.000,350.00,19.00,1750.00,2082.50,GETDATE(),GETDATE()),
(12039,1202,4.000,420.00,19.00,1680.00,1999.20,GETDATE(),GETDATE()),
(12039,1204,2.000,280.00,19.00,560.00,666.40,GETDATE(),GETDATE()),
(12040,1201,2.000,950.00,19.00,1900.00,2261.00,GETDATE(),GETDATE()),
(12040,1203,5.000,350.00,19.00,1750.00,2082.50,GETDATE(),GETDATE())
GO

-- ============================================================
-- 10. SALES ORDERS (commande_vente) – 75 orders Jan-Mar 2026
--
-- Statut values: BROUILLON | EN_ATTENTE | CONFIRMEE | LIVREE | ANNULEE
--
-- Templates (HT / TVA @19 % / TTC):
--   SA  5440.00 / 1033.60 / 6473.60   (3x1201 + 2x1202)
--   SB  4040.00 /  767.60 / 4807.60   (4x1202 + 3x1203)
--   SC  4880.00 /  927.20 / 5807.20   (2x1201 + 4x1203)
--   SD  3480.00 /  661.20 / 4141.20   (5x1203 + 2x1204)
--   SE  3160.00 /  600.40 / 3760.40   (3x1202 + 10x1205)
--   SF  3980.00 /  756.20 / 4736.20   (2x1204 + 5x1202)
--   SG  4120.00 /  782.80 / 4902.80   (2x1201 + 3x1204)
--   SH  5700.00 / 1083.00 / 6783.00   (5x1202 + 5x1203)
-- ============================================================

-- Clean up previous seed run
DELETE FROM [dbo].[ligne_commande_vente] WHERE [commande_vente_id] BETWEEN 12001 AND 12075
DELETE FROM [dbo].[commande_vente]        WHERE [id]                BETWEEN 12001 AND 12075
GO

SET IDENTITY_INSERT [dbo].[commande_vente] ON
GO

INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12001,N'CV-ERA-2026-001','2026-01-02','2026-01-09',N'LIVREE',5440.00,1033.60,6473.60,121,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12002,N'CV-ERA-2026-002','2026-01-03','2026-01-10',N'LIVREE',4040.00,767.60,4807.60,122,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12003,N'CV-ERA-2026-003','2026-01-04','2026-01-11',N'LIVREE',4880.00,927.20,5807.20,123,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12004,N'CV-ERA-2026-004','2026-01-05','2026-01-12',N'LIVREE',3480.00,661.20,4141.20,124,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12005,N'CV-ERA-2026-005','2026-01-06','2026-01-13',N'LIVREE',3160.00,600.40,3760.40,121,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12006,N'CV-ERA-2026-006','2026-01-07','2026-01-14',N'LIVREE',3980.00,756.20,4736.20,122,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12007,N'CV-ERA-2026-007','2026-01-08','2026-01-15',N'LIVREE',4120.00,782.80,4902.80,123,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12008,N'CV-ERA-2026-008','2026-01-09','2026-01-16',N'LIVREE',5700.00,1083.00,6783.00,124,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12009,N'CV-ERA-2026-009','2026-01-10','2026-01-17',N'LIVREE',5440.00,1033.60,6473.60,121,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12010,N'CV-ERA-2026-010','2026-01-11','2026-01-18',N'LIVREE',4040.00,767.60,4807.60,122,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12011,N'CV-ERA-2026-011','2026-01-12','2026-01-19',N'LIVREE',4880.00,927.20,5807.20,123,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12012,N'CV-ERA-2026-012','2026-01-13','2026-01-20',N'LIVREE',3480.00,661.20,4141.20,124,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12013,N'CV-ERA-2026-013','2026-01-14','2026-01-21',N'LIVREE',3160.00,600.40,3760.40,121,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12014,N'CV-ERA-2026-014','2026-01-15','2026-01-22',N'LIVREE',3980.00,756.20,4736.20,122,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12015,N'CV-ERA-2026-015','2026-01-16','2026-01-23',N'LIVREE',4120.00,782.80,4902.80,123,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12016,N'CV-ERA-2026-016','2026-01-17','2026-01-24',N'LIVREE',5700.00,1083.00,6783.00,124,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12017,N'CV-ERA-2026-017','2026-01-18','2026-01-25',N'LIVREE',5440.00,1033.60,6473.60,121,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12018,N'CV-ERA-2026-018','2026-01-19','2026-01-26',N'LIVREE',4040.00,767.60,4807.60,122,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12019,N'CV-ERA-2026-019','2026-01-20','2026-01-27',N'LIVREE',4880.00,927.20,5807.20,123,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12020,N'CV-ERA-2026-020','2026-01-21','2026-01-28',N'LIVREE',3480.00,661.20,4141.20,124,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12021,N'CV-ERA-2026-021','2026-01-22','2026-01-29',N'LIVREE',3160.00,600.40,3760.40,121,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12022,N'CV-ERA-2026-022','2026-01-23','2026-01-30',N'LIVREE',3980.00,756.20,4736.20,122,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12023,N'CV-ERA-2026-023','2026-01-24','2026-01-31',N'LIVREE',4120.00,782.80,4902.80,123,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12024,N'CV-ERA-2026-024','2026-01-25','2026-02-01',N'LIVREE',5700.00,1083.00,6783.00,124,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12025,N'CV-ERA-2026-025','2026-01-26','2026-02-02',N'LIVREE',5440.00,1033.60,6473.60,121,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12026,N'CV-ERA-2026-026','2026-02-02','2026-02-09',N'LIVREE',4040.00,767.60,4807.60,122,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12027,N'CV-ERA-2026-027','2026-02-03','2026-02-10',N'LIVREE',4880.00,927.20,5807.20,123,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12028,N'CV-ERA-2026-028','2026-02-04','2026-02-11',N'LIVREE',3480.00,661.20,4141.20,124,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12029,N'CV-ERA-2026-029','2026-02-05','2026-02-12',N'LIVREE',3160.00,600.40,3760.40,121,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12030,N'CV-ERA-2026-030','2026-02-06','2026-02-13',N'LIVREE',3980.00,756.20,4736.20,122,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12031,N'CV-ERA-2026-031','2026-02-07','2026-02-14',N'LIVREE',4120.00,782.80,4902.80,123,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12032,N'CV-ERA-2026-032','2026-02-08','2026-02-15',N'LIVREE',5700.00,1083.00,6783.00,124,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12033,N'CV-ERA-2026-033','2026-02-09','2026-02-16',N'LIVREE',5440.00,1033.60,6473.60,121,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12034,N'CV-ERA-2026-034','2026-02-10','2026-02-17',N'LIVREE',4040.00,767.60,4807.60,122,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12035,N'CV-ERA-2026-035','2026-02-11','2026-02-18',N'LIVREE',4880.00,927.20,5807.20,123,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12036,N'CV-ERA-2026-036','2026-02-12','2026-02-19',N'LIVREE',3480.00,661.20,4141.20,124,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12037,N'CV-ERA-2026-037','2026-02-13','2026-02-20',N'LIVREE',3160.00,600.40,3760.40,121,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12038,N'CV-ERA-2026-038','2026-02-14','2026-02-21',N'LIVREE',3980.00,756.20,4736.20,122,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12039,N'CV-ERA-2026-039','2026-02-15','2026-02-22',N'LIVREE',4120.00,782.80,4902.80,123,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12040,N'CV-ERA-2026-040','2026-02-16','2026-02-23',N'LIVREE',5700.00,1083.00,6783.00,124,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12041,N'CV-ERA-2026-041','2026-02-17','2026-02-24',N'LIVREE',5440.00,1033.60,6473.60,121,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12042,N'CV-ERA-2026-042','2026-02-18','2026-02-25',N'LIVREE',4040.00,767.60,4807.60,122,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12043,N'CV-ERA-2026-043','2026-02-19','2026-02-26',N'LIVREE',4880.00,927.20,5807.20,123,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12044,N'CV-ERA-2026-044','2026-02-20','2026-02-27',N'LIVREE',3480.00,661.20,4141.20,124,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12045,N'CV-ERA-2026-045','2026-02-21','2026-02-28',N'LIVREE',3160.00,600.40,3760.40,121,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12046,N'CV-ERA-2026-046','2026-02-22','2026-03-01',N'LIVREE',3980.00,756.20,4736.20,122,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12047,N'CV-ERA-2026-047','2026-02-23','2026-03-02',N'LIVREE',4120.00,782.80,4902.80,123,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12048,N'CV-ERA-2026-048','2026-02-24','2026-03-03',N'LIVREE',5700.00,1083.00,6783.00,124,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12049,N'CV-ERA-2026-049','2026-02-25','2026-03-04',N'LIVREE',5440.00,1033.60,6473.60,121,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12050,N'CV-ERA-2026-050','2026-02-26','2026-03-05',N'LIVREE',4040.00,767.60,4807.60,122,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12051,N'CV-ERA-2026-051','2026-03-02','2026-03-09',N'LIVREE',3160.00,600.40,3760.40,123,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12052,N'CV-ERA-2026-052','2026-03-03','2026-03-10',N'LIVREE',3980.00,756.20,4736.20,124,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12053,N'CV-ERA-2026-053','2026-03-04','2026-03-11',N'LIVREE',4120.00,782.80,4902.80,121,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12054,N'CV-ERA-2026-054','2026-03-05','2026-03-12',N'LIVREE',5700.00,1083.00,6783.00,122,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12055,N'CV-ERA-2026-055','2026-03-06','2026-03-13',N'LIVREE',5440.00,1033.60,6473.60,123,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12056,N'CV-ERA-2026-056','2026-03-07','2026-03-14',N'LIVREE',4040.00,767.60,4807.60,124,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12057,N'CV-ERA-2026-057','2026-03-08','2026-03-15',N'LIVREE',4880.00,927.20,5807.20,121,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12058,N'CV-ERA-2026-058','2026-03-09','2026-03-16',N'LIVREE',3480.00,661.20,4141.20,122,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12059,N'CV-ERA-2026-059','2026-03-10','2026-03-17',N'LIVREE',3160.00,600.40,3760.40,123,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12060,N'CV-ERA-2026-060','2026-03-11','2026-03-18',N'LIVREE',3980.00,756.20,4736.20,124,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12061,N'CV-ERA-2026-061','2026-03-12','2026-03-19',N'LIVREE',4120.00,782.80,4902.80,121,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12062,N'CV-ERA-2026-062','2026-03-13','2026-03-20',N'LIVREE',5700.00,1083.00,6783.00,122,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12063,N'CV-ERA-2026-063','2026-03-14','2026-03-21',N'LIVREE',5440.00,1033.60,6473.60,123,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12064,N'CV-ERA-2026-064','2026-03-15','2026-03-22',N'LIVREE',4040.00,767.60,4807.60,124,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12065,N'CV-ERA-2026-065','2026-03-16','2026-03-23',N'LIVREE',4880.00,927.20,5807.20,121,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12066,N'CV-ERA-2026-066','2026-03-17','2026-03-24',N'LIVREE',3480.00,661.20,4141.20,122,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12067,N'CV-ERA-2026-067','2026-03-18',NULL,N'CONFIRMEE',3160.00,600.40,3760.40,123,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12068,N'CV-ERA-2026-068','2026-03-19',NULL,N'CONFIRMEE',3980.00,756.20,4736.20,124,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12069,N'CV-ERA-2026-069','2026-03-20',NULL,N'CONFIRMEE',4120.00,782.80,4902.80,121,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12070,N'CV-ERA-2026-070','2026-03-21',NULL,N'CONFIRMEE',5700.00,1083.00,6783.00,122,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12071,N'CV-ERA-2026-071','2026-03-22',NULL,N'CONFIRMEE',5440.00,1033.60,6473.60,123,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12072,N'CV-ERA-2026-072','2026-03-23',NULL,N'CONFIRMEE',4040.00,767.60,4807.60,124,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12073,N'CV-ERA-2026-073','2026-03-24',NULL,N'CONFIRMEE',4880.00,927.20,5807.20,121,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12074,N'CV-ERA-2026-074','2026-03-25',NULL,N'BROUILLON',3480.00,661.20,4141.20,122,12,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (12075,N'CV-ERA-2026-075','2026-03-26',NULL,N'BROUILLON',3160.00,600.40,3760.40,123,12,GETDATE(),GETDATE())

SET IDENTITY_INSERT [dbo].[commande_vente] OFF
GO

-- ============================================================
-- 11. SALES ORDER LINE ITEMS (ligne_commande_vente)
-- ============================================================
INSERT [dbo].[ligne_commande_vente] ([commande_vente_id],[article_id],[quantite],[prix_unitaire_ht],[taux_tva],[montant_ht],[montant_ttc],[created_at],[updated_at]) VALUES
(12001,1201,3.000,1400.00,19.00,4200.00,4998.00,GETDATE(),GETDATE()),
(12001,1202,2.000,620.00,19.00,1240.00,1475.60,GETDATE(),GETDATE()),
(12002,1202,4.000,620.00,19.00,2480.00,2951.20,GETDATE(),GETDATE()),
(12002,1203,3.000,520.00,19.00,1560.00,1856.40,GETDATE(),GETDATE()),
(12003,1201,2.000,1400.00,19.00,2800.00,3332.00,GETDATE(),GETDATE()),
(12003,1203,4.000,520.00,19.00,2080.00,2475.20,GETDATE(),GETDATE()),
(12004,1203,5.000,520.00,19.00,2600.00,3094.00,GETDATE(),GETDATE()),
(12004,1204,2.000,440.00,19.00,880.00,1047.20,GETDATE(),GETDATE()),
(12005,1202,3.000,620.00,19.00,1860.00,2213.40,GETDATE(),GETDATE()),
(12005,1205,10.000,130.00,19.00,1300.00,1547.00,GETDATE(),GETDATE()),
(12006,1204,2.000,440.00,19.00,880.00,1047.20,GETDATE(),GETDATE()),
(12006,1202,5.000,620.00,19.00,3100.00,3689.00,GETDATE(),GETDATE()),
(12007,1201,2.000,1400.00,19.00,2800.00,3332.00,GETDATE(),GETDATE()),
(12007,1204,3.000,440.00,19.00,1320.00,1570.80,GETDATE(),GETDATE()),
(12008,1202,5.000,620.00,19.00,3100.00,3689.00,GETDATE(),GETDATE()),
(12008,1203,5.000,520.00,19.00,2600.00,3094.00,GETDATE(),GETDATE()),
(12009,1201,3.000,1400.00,19.00,4200.00,4998.00,GETDATE(),GETDATE()),
(12009,1202,2.000,620.00,19.00,1240.00,1475.60,GETDATE(),GETDATE()),
(12010,1202,4.000,620.00,19.00,2480.00,2951.20,GETDATE(),GETDATE()),
(12010,1203,3.000,520.00,19.00,1560.00,1856.40,GETDATE(),GETDATE()),
(12011,1201,2.000,1400.00,19.00,2800.00,3332.00,GETDATE(),GETDATE()),
(12011,1203,4.000,520.00,19.00,2080.00,2475.20,GETDATE(),GETDATE()),
(12012,1203,5.000,520.00,19.00,2600.00,3094.00,GETDATE(),GETDATE()),
(12012,1204,2.000,440.00,19.00,880.00,1047.20,GETDATE(),GETDATE()),
(12013,1202,3.000,620.00,19.00,1860.00,2213.40,GETDATE(),GETDATE()),
(12013,1205,10.000,130.00,19.00,1300.00,1547.00,GETDATE(),GETDATE()),
(12014,1204,2.000,440.00,19.00,880.00,1047.20,GETDATE(),GETDATE()),
(12014,1202,5.000,620.00,19.00,3100.00,3689.00,GETDATE(),GETDATE()),
(12015,1201,2.000,1400.00,19.00,2800.00,3332.00,GETDATE(),GETDATE()),
(12015,1204,3.000,440.00,19.00,1320.00,1570.80,GETDATE(),GETDATE()),
(12016,1202,5.000,620.00,19.00,3100.00,3689.00,GETDATE(),GETDATE()),
(12016,1203,5.000,520.00,19.00,2600.00,3094.00,GETDATE(),GETDATE()),
(12017,1201,3.000,1400.00,19.00,4200.00,4998.00,GETDATE(),GETDATE()),
(12017,1202,2.000,620.00,19.00,1240.00,1475.60,GETDATE(),GETDATE()),
(12018,1202,4.000,620.00,19.00,2480.00,2951.20,GETDATE(),GETDATE()),
(12018,1203,3.000,520.00,19.00,1560.00,1856.40,GETDATE(),GETDATE()),
(12019,1201,2.000,1400.00,19.00,2800.00,3332.00,GETDATE(),GETDATE()),
(12019,1203,4.000,520.00,19.00,2080.00,2475.20,GETDATE(),GETDATE()),
(12020,1203,5.000,520.00,19.00,2600.00,3094.00,GETDATE(),GETDATE()),
(12020,1204,2.000,440.00,19.00,880.00,1047.20,GETDATE(),GETDATE()),
(12021,1202,3.000,620.00,19.00,1860.00,2213.40,GETDATE(),GETDATE()),
(12021,1205,10.000,130.00,19.00,1300.00,1547.00,GETDATE(),GETDATE()),
(12022,1204,2.000,440.00,19.00,880.00,1047.20,GETDATE(),GETDATE()),
(12022,1202,5.000,620.00,19.00,3100.00,3689.00,GETDATE(),GETDATE()),
(12023,1201,2.000,1400.00,19.00,2800.00,3332.00,GETDATE(),GETDATE()),
(12023,1204,3.000,440.00,19.00,1320.00,1570.80,GETDATE(),GETDATE()),
(12024,1202,5.000,620.00,19.00,3100.00,3689.00,GETDATE(),GETDATE()),
(12024,1203,5.000,520.00,19.00,2600.00,3094.00,GETDATE(),GETDATE()),
(12025,1201,3.000,1400.00,19.00,4200.00,4998.00,GETDATE(),GETDATE()),
(12025,1202,2.000,620.00,19.00,1240.00,1475.60,GETDATE(),GETDATE()),
(12026,1202,4.000,620.00,19.00,2480.00,2951.20,GETDATE(),GETDATE()),
(12026,1203,3.000,520.00,19.00,1560.00,1856.40,GETDATE(),GETDATE()),
(12027,1201,2.000,1400.00,19.00,2800.00,3332.00,GETDATE(),GETDATE()),
(12027,1203,4.000,520.00,19.00,2080.00,2475.20,GETDATE(),GETDATE()),
(12028,1203,5.000,520.00,19.00,2600.00,3094.00,GETDATE(),GETDATE()),
(12028,1204,2.000,440.00,19.00,880.00,1047.20,GETDATE(),GETDATE()),
(12029,1202,3.000,620.00,19.00,1860.00,2213.40,GETDATE(),GETDATE()),
(12029,1205,10.000,130.00,19.00,1300.00,1547.00,GETDATE(),GETDATE()),
(12030,1204,2.000,440.00,19.00,880.00,1047.20,GETDATE(),GETDATE()),
(12030,1202,5.000,620.00,19.00,3100.00,3689.00,GETDATE(),GETDATE()),
(12031,1201,2.000,1400.00,19.00,2800.00,3332.00,GETDATE(),GETDATE()),
(12031,1204,3.000,440.00,19.00,1320.00,1570.80,GETDATE(),GETDATE()),
(12032,1202,5.000,620.00,19.00,3100.00,3689.00,GETDATE(),GETDATE()),
(12032,1203,5.000,520.00,19.00,2600.00,3094.00,GETDATE(),GETDATE()),
(12033,1201,3.000,1400.00,19.00,4200.00,4998.00,GETDATE(),GETDATE()),
(12033,1202,2.000,620.00,19.00,1240.00,1475.60,GETDATE(),GETDATE()),
(12034,1202,4.000,620.00,19.00,2480.00,2951.20,GETDATE(),GETDATE()),
(12034,1203,3.000,520.00,19.00,1560.00,1856.40,GETDATE(),GETDATE()),
(12035,1201,2.000,1400.00,19.00,2800.00,3332.00,GETDATE(),GETDATE()),
(12035,1203,4.000,520.00,19.00,2080.00,2475.20,GETDATE(),GETDATE()),
(12036,1203,5.000,520.00,19.00,2600.00,3094.00,GETDATE(),GETDATE()),
(12036,1204,2.000,440.00,19.00,880.00,1047.20,GETDATE(),GETDATE()),
(12037,1202,3.000,620.00,19.00,1860.00,2213.40,GETDATE(),GETDATE()),
(12037,1205,10.000,130.00,19.00,1300.00,1547.00,GETDATE(),GETDATE()),
(12038,1204,2.000,440.00,19.00,880.00,1047.20,GETDATE(),GETDATE()),
(12038,1202,5.000,620.00,19.00,3100.00,3689.00,GETDATE(),GETDATE()),
(12039,1201,2.000,1400.00,19.00,2800.00,3332.00,GETDATE(),GETDATE()),
(12039,1204,3.000,440.00,19.00,1320.00,1570.80,GETDATE(),GETDATE()),
(12040,1202,5.000,620.00,19.00,3100.00,3689.00,GETDATE(),GETDATE()),
(12040,1203,5.000,520.00,19.00,2600.00,3094.00,GETDATE(),GETDATE()),
(12041,1201,3.000,1400.00,19.00,4200.00,4998.00,GETDATE(),GETDATE()),
(12041,1202,2.000,620.00,19.00,1240.00,1475.60,GETDATE(),GETDATE()),
(12042,1202,4.000,620.00,19.00,2480.00,2951.20,GETDATE(),GETDATE()),
(12042,1203,3.000,520.00,19.00,1560.00,1856.40,GETDATE(),GETDATE()),
(12043,1201,2.000,1400.00,19.00,2800.00,3332.00,GETDATE(),GETDATE()),
(12043,1203,4.000,520.00,19.00,2080.00,2475.20,GETDATE(),GETDATE()),
(12044,1203,5.000,520.00,19.00,2600.00,3094.00,GETDATE(),GETDATE()),
(12044,1204,2.000,440.00,19.00,880.00,1047.20,GETDATE(),GETDATE()),
(12045,1202,3.000,620.00,19.00,1860.00,2213.40,GETDATE(),GETDATE()),
(12045,1205,10.000,130.00,19.00,1300.00,1547.00,GETDATE(),GETDATE()),
(12046,1204,2.000,440.00,19.00,880.00,1047.20,GETDATE(),GETDATE()),
(12046,1202,5.000,620.00,19.00,3100.00,3689.00,GETDATE(),GETDATE()),
(12047,1201,2.000,1400.00,19.00,2800.00,3332.00,GETDATE(),GETDATE()),
(12047,1204,3.000,440.00,19.00,1320.00,1570.80,GETDATE(),GETDATE()),
(12048,1202,5.000,620.00,19.00,3100.00,3689.00,GETDATE(),GETDATE()),
(12048,1203,5.000,520.00,19.00,2600.00,3094.00,GETDATE(),GETDATE()),
(12049,1201,3.000,1400.00,19.00,4200.00,4998.00,GETDATE(),GETDATE()),
(12049,1202,2.000,620.00,19.00,1240.00,1475.60,GETDATE(),GETDATE()),
(12050,1202,4.000,620.00,19.00,2480.00,2951.20,GETDATE(),GETDATE()),
(12050,1203,3.000,520.00,19.00,1560.00,1856.40,GETDATE(),GETDATE()),
(12051,1202,3.000,620.00,19.00,1860.00,2213.40,GETDATE(),GETDATE()),
(12051,1205,10.000,130.00,19.00,1300.00,1547.00,GETDATE(),GETDATE()),
(12052,1204,2.000,440.00,19.00,880.00,1047.20,GETDATE(),GETDATE()),
(12052,1202,5.000,620.00,19.00,3100.00,3689.00,GETDATE(),GETDATE()),
(12053,1201,2.000,1400.00,19.00,2800.00,3332.00,GETDATE(),GETDATE()),
(12053,1204,3.000,440.00,19.00,1320.00,1570.80,GETDATE(),GETDATE()),
(12054,1202,5.000,620.00,19.00,3100.00,3689.00,GETDATE(),GETDATE()),
(12054,1203,5.000,520.00,19.00,2600.00,3094.00,GETDATE(),GETDATE()),
(12055,1201,3.000,1400.00,19.00,4200.00,4998.00,GETDATE(),GETDATE()),
(12055,1202,2.000,620.00,19.00,1240.00,1475.60,GETDATE(),GETDATE()),
(12056,1202,4.000,620.00,19.00,2480.00,2951.20,GETDATE(),GETDATE()),
(12056,1203,3.000,520.00,19.00,1560.00,1856.40,GETDATE(),GETDATE()),
(12057,1201,2.000,1400.00,19.00,2800.00,3332.00,GETDATE(),GETDATE()),
(12057,1203,4.000,520.00,19.00,2080.00,2475.20,GETDATE(),GETDATE()),
(12058,1203,5.000,520.00,19.00,2600.00,3094.00,GETDATE(),GETDATE()),
(12058,1204,2.000,440.00,19.00,880.00,1047.20,GETDATE(),GETDATE()),
(12059,1202,3.000,620.00,19.00,1860.00,2213.40,GETDATE(),GETDATE()),
(12059,1205,10.000,130.00,19.00,1300.00,1547.00,GETDATE(),GETDATE()),
(12060,1204,2.000,440.00,19.00,880.00,1047.20,GETDATE(),GETDATE()),
(12060,1202,5.000,620.00,19.00,3100.00,3689.00,GETDATE(),GETDATE()),
(12061,1201,2.000,1400.00,19.00,2800.00,3332.00,GETDATE(),GETDATE()),
(12061,1204,3.000,440.00,19.00,1320.00,1570.80,GETDATE(),GETDATE()),
(12062,1202,5.000,620.00,19.00,3100.00,3689.00,GETDATE(),GETDATE()),
(12062,1203,5.000,520.00,19.00,2600.00,3094.00,GETDATE(),GETDATE()),
(12063,1201,3.000,1400.00,19.00,4200.00,4998.00,GETDATE(),GETDATE()),
(12063,1202,2.000,620.00,19.00,1240.00,1475.60,GETDATE(),GETDATE()),
(12064,1202,4.000,620.00,19.00,2480.00,2951.20,GETDATE(),GETDATE()),
(12064,1203,3.000,520.00,19.00,1560.00,1856.40,GETDATE(),GETDATE()),
(12065,1201,2.000,1400.00,19.00,2800.00,3332.00,GETDATE(),GETDATE()),
(12065,1203,4.000,520.00,19.00,2080.00,2475.20,GETDATE(),GETDATE()),
(12066,1203,5.000,520.00,19.00,2600.00,3094.00,GETDATE(),GETDATE()),
(12066,1204,2.000,440.00,19.00,880.00,1047.20,GETDATE(),GETDATE()),
(12067,1202,3.000,620.00,19.00,1860.00,2213.40,GETDATE(),GETDATE()),
(12067,1205,10.000,130.00,19.00,1300.00,1547.00,GETDATE(),GETDATE()),
(12068,1204,2.000,440.00,19.00,880.00,1047.20,GETDATE(),GETDATE()),
(12068,1202,5.000,620.00,19.00,3100.00,3689.00,GETDATE(),GETDATE()),
(12069,1201,2.000,1400.00,19.00,2800.00,3332.00,GETDATE(),GETDATE()),
(12069,1204,3.000,440.00,19.00,1320.00,1570.80,GETDATE(),GETDATE()),
(12070,1202,5.000,620.00,19.00,3100.00,3689.00,GETDATE(),GETDATE()),
(12070,1203,5.000,520.00,19.00,2600.00,3094.00,GETDATE(),GETDATE()),
(12071,1201,3.000,1400.00,19.00,4200.00,4998.00,GETDATE(),GETDATE()),
(12071,1202,2.000,620.00,19.00,1240.00,1475.60,GETDATE(),GETDATE()),
(12072,1202,4.000,620.00,19.00,2480.00,2951.20,GETDATE(),GETDATE()),
(12072,1203,3.000,520.00,19.00,1560.00,1856.40,GETDATE(),GETDATE()),
(12073,1201,2.000,1400.00,19.00,2800.00,3332.00,GETDATE(),GETDATE()),
(12073,1203,4.000,520.00,19.00,2080.00,2475.20,GETDATE(),GETDATE()),
(12074,1203,5.000,520.00,19.00,2600.00,3094.00,GETDATE(),GETDATE()),
(12074,1204,2.000,440.00,19.00,880.00,1047.20,GETDATE(),GETDATE()),
(12075,1202,3.000,620.00,19.00,1860.00,2213.40,GETDATE(),GETDATE()),
(12075,1205,10.000,130.00,19.00,1300.00,1547.00,GETDATE(),GETDATE())
GO

-- ============================================================
-- 12. SUMMARY VALIDATION
-- ============================================================
PRINT N''
PRINT N'============================================================'
PRINT N' ERANOW ENTERPRISE (ID 12) – DATA INSERTION COMPLETE'
PRINT N'============================================================'
PRINT N''

SELECT
    N'PURCHASE ORDERS' AS [Type],
    COUNT(*)           AS [Total Orders],
    SUM(montant_ht)    AS [Total HT],
    SUM(montant_tva)   AS [Total TVA],
    SUM(montant_ttc)   AS [Total TTC]
FROM [dbo].[commande_achat]
WHERE entreprise_id = 12
  AND id BETWEEN 12001 AND 12040
UNION ALL
SELECT
    N'SALES ORDERS'    AS [Type],
    COUNT(*)           AS [Total Orders],
    SUM(montant_ht)    AS [Total HT],
    SUM(montant_tva)   AS [Total TVA],
    SUM(montant_ttc)   AS [Total TTC]
FROM [dbo].[commande_vente]
WHERE entreprise_id = 12
  AND id BETWEEN 12001 AND 12075
GO

SELECT
    YEAR(date_commande) AS [Year], MONTH(date_commande) AS [Month],
    N'ACHAT' AS [Type], COUNT(*) AS [Orders], SUM(montant_ttc) AS [Total TTC]
FROM [dbo].[commande_achat]
WHERE entreprise_id = 12 AND id BETWEEN 12001 AND 12040
GROUP BY YEAR(date_commande), MONTH(date_commande)
UNION ALL
SELECT YEAR(date_commande), MONTH(date_commande),
    N'VENTE', COUNT(*), SUM(montant_ttc)
FROM [dbo].[commande_vente]
WHERE entreprise_id = 12 AND id BETWEEN 12001 AND 12075
GROUP BY YEAR(date_commande), MONTH(date_commande)
ORDER BY [Year], [Month], [Type]
GO
