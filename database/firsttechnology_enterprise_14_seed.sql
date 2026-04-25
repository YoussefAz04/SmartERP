-- ============================================================
-- FIRSTTECHNOLOGY ENTERPRISE (ID: 14) - IT & TECHNOLOGY DATA
-- ============================================================
-- Purpose : Populate realistic test data for FirstTechnology (entreprise_id = 14)
-- Sector  : IT, Computer Hardware & Technology Equipment
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
IF NOT EXISTS (SELECT 1 FROM [dbo].[entreprise] WHERE id = 14)
    INSERT [dbo].[entreprise]
        ([id],[nom],[raison_sociale],[adresse],[telephone],[email],[site_web],[secteur_activite],[description],[date_creation],[created_at],[updated_at])
    VALUES
        (14,N'FirstTechnology',N'FirstTechnology IT Solutions SARL',N'Pôle Technologique El Ghazela, Bloc C, Ariana',N'+216-71-940-140',N'contact@firsttechnology.tn',N'https://www.firsttechnology.tn',N'Informatique & Technologie',N'Distribution de matériel informatique, équipement IT et solutions technologiques',CAST(N'2024-06-15' AS Date),GETDATE(),GETDATE())
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

-- Categorie article (IT / Technology)
SET IDENTITY_INSERT [dbo].[categorie_article] ON
IF NOT EXISTS (SELECT 1 FROM [dbo].[categorie_article] WHERE id = 14)
    INSERT [dbo].[categorie_article] ([id],[nom],[description],[created_at],[updated_at])
    VALUES (14,N'Informatique',N'Ordinateurs portables, écrans, périphériques et accessoires IT',GETDATE(),GETDATE())
SET IDENTITY_INSERT [dbo].[categorie_article] OFF
GO

-- Categorie fournisseur
SET IDENTITY_INSERT [dbo].[categorie_fournisseur] ON
IF NOT EXISTS (SELECT 1 FROM [dbo].[categorie_fournisseur] WHERE id = 14)
    INSERT [dbo].[categorie_fournisseur] ([id],[nom],[description],[created_at],[updated_at])
    VALUES (14,N'Fournisseurs IT',N'Distributeurs de matériel informatique et importateurs de technologie',GETDATE(),GETDATE())
SET IDENTITY_INSERT [dbo].[categorie_fournisseur] OFF
GO

-- Categorie client
SET IDENTITY_INSERT [dbo].[categorie_client] ON
IF NOT EXISTS (SELECT 1 FROM [dbo].[categorie_client] WHERE id = 14)
    INSERT [dbo].[categorie_client] ([id],[nom],[description],[created_at],[updated_at])
    VALUES (14,N'Entreprises & Institutions',N'Sociétés, universités, cabinets et hubs technologiques partenaires',GETDATE(),GETDATE())
SET IDENTITY_INSERT [dbo].[categorie_client] OFF
GO

-- ============================================================
-- 3. ARTICLES 1401-1405 (IT for FirstTechnology)
--    prix_achat / prix_vente are HT (excluding TVA 19 %)
-- ============================================================
SET IDENTITY_INSERT [dbo].[article] ON
IF NOT EXISTS (SELECT 1 FROM [dbo].[article] WHERE id = 1401)
    INSERT [dbo].[article] ([id],[reference],[nom],[description],[prix_achat],[prix_vente],[actif],[categorie_article_id],[mesure_id],[tva_id],[entreprise_id],[created_at],[updated_at])
    VALUES (1401,N'FT-001',N'Ordinateur Portable Pro 15"',N'Laptop professionnel 15" Intel Core i7, 16GB RAM, 512GB SSD, écran Full HD',1800.00,2400.00,1,14,1,2,14,GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[article] WHERE id = 1402)
    INSERT [dbo].[article] ([id],[reference],[nom],[description],[prix_achat],[prix_vente],[actif],[categorie_article_id],[mesure_id],[tva_id],[entreprise_id],[created_at],[updated_at])
    VALUES (1402,N'FT-002',N'Écran 27" 4K UHD',N'Moniteur 27 pouces 4K UHD IPS, HDMI/DisplayPort/USB-C, calibré usine',600.00,850.00,1,14,1,2,14,GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[article] WHERE id = 1403)
    INSERT [dbo].[article] ([id],[reference],[nom],[description],[prix_achat],[prix_vente],[actif],[categorie_article_id],[mesure_id],[tva_id],[entreprise_id],[created_at],[updated_at])
    VALUES (1403,N'FT-003',N'Clavier Mécanique RGB',N'Clavier mécanique gaming touches RGB, switches bleus, layout AZERTY',180.00,280.00,1,14,1,2,14,GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[article] WHERE id = 1404)
    INSERT [dbo].[article] ([id],[reference],[nom],[description],[prix_achat],[prix_vente],[actif],[categorie_article_id],[mesure_id],[tva_id],[entreprise_id],[created_at],[updated_at])
    VALUES (1404,N'FT-004',N'Souris Sans Fil Pro',N'Souris ergonomique sans fil 2.4GHz/Bluetooth, capteur 16000 DPI, batterie longue durée',90.00,150.00,1,14,1,2,14,GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[article] WHERE id = 1405)
    INSERT [dbo].[article] ([id],[reference],[nom],[description],[prix_achat],[prix_vente],[actif],[categorie_article_id],[mesure_id],[tva_id],[entreprise_id],[created_at],[updated_at])
    VALUES (1405,N'FT-005',N'Casque Audio Bluetooth ANC',N'Casque circum-aural Bluetooth 5.2 réduction de bruit active, 30h d''autonomie',220.00,350.00,1,14,1,2,14,GETDATE(),GETDATE())
SET IDENTITY_INSERT [dbo].[article] OFF
GO

-- ============================================================
-- 4. ARTICLE_VENTE 1401-1405
-- ============================================================
SET IDENTITY_INSERT [dbo].[article_vente] ON
IF NOT EXISTS (SELECT 1 FROM [dbo].[article_vente] WHERE id = 1401)
    INSERT [dbo].[article_vente] ([id],[article_id],[prix_vente_special],[description_vente],[created_at],[updated_at])
    VALUES (1401,1401,2300.00,N'Tarif spécial revendeurs Ordinateur Portable Pro',GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[article_vente] WHERE id = 1402)
    INSERT [dbo].[article_vente] ([id],[article_id],[prix_vente_special],[description_vente],[created_at],[updated_at])
    VALUES (1402,1402,800.00,N'Prix préférentiel Écran 27" 4K grands comptes',GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[article_vente] WHERE id = 1403)
    INSERT [dbo].[article_vente] ([id],[article_id],[prix_vente_special],[description_vente],[created_at],[updated_at])
    VALUES (1403,1403,260.00,N'Promotion Clavier Mécanique commandes > 10',GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[article_vente] WHERE id = 1404)
    INSERT [dbo].[article_vente] ([id],[article_id],[prix_vente_special],[description_vente],[created_at],[updated_at])
    VALUES (1404,1404,140.00,N'Remise Souris Sans Fil lot > 20',GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[article_vente] WHERE id = 1405)
    INSERT [dbo].[article_vente] ([id],[article_id],[prix_vente_special],[description_vente],[created_at],[updated_at])
    VALUES (1405,1405,330.00,N'Offre Casque Audio commandes > 5',GETDATE(),GETDATE())
SET IDENTITY_INSERT [dbo].[article_vente] OFF
GO

-- ============================================================
-- 5. FOURNISSEURS 141-144 (for FirstTechnology)
-- ============================================================
SET IDENTITY_INSERT [dbo].[fournisseur] ON
IF NOT EXISTS (SELECT 1 FROM [dbo].[fournisseur] WHERE id = 141)
    INSERT [dbo].[fournisseur] ([id],[nom],[prenom],[email],[telephone],[adresse],[ville],[pays],[categorie_fournisseur_id],[entreprise_id],[created_at],[updated_at])
    VALUES (141,N'Tech Components Distribution',N'Mehdi Saidi',N'achats@techcomp.tn',N'+216-71-560-101',N'Rue de l''Industrie, Charguia II',N'Tunis',N'Tunisie',14,14,GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[fournisseur] WHERE id = 142)
    INSERT [dbo].[fournisseur] ([id],[nom],[prenom],[email],[telephone],[adresse],[ville],[pays],[categorie_fournisseur_id],[entreprise_id],[created_at],[updated_at])
    VALUES (142,N'InfoTech Wholesale SARL',N'Lina Mansour',N'ventes@infotechws.tn',N'+216-74-330-202',N'Zone Industrielle Sidi Salem',N'Sfax',N'Tunisie',14,14,GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[fournisseur] WHERE id = 143)
    INSERT [dbo].[fournisseur] ([id],[nom],[prenom],[email],[telephone],[adresse],[ville],[pays],[categorie_fournisseur_id],[entreprise_id],[created_at],[updated_at])
    VALUES (143,N'DigitalPro Import Export',N'Hatem Riahi',N'contact@digitalpro.tn',N'+216-72-115-303',N'Avenue Mohamed V, Centre Tech',N'Sousse',N'Tunisie',14,14,GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[fournisseur] WHERE id = 144)
    INSERT [dbo].[fournisseur] ([id],[nom],[prenom],[email],[telephone],[adresse],[ville],[pays],[categorie_fournisseur_id],[entreprise_id],[created_at],[updated_at])
    VALUES (144,N'GreenIT Solutions',N'Amina Ben Ali',N'info@greenit.tn',N'+216-73-880-404',N'Pôle Technologique El Ghazala',N'Ariana',N'Tunisie',14,14,GETDATE(),GETDATE())
SET IDENTITY_INSERT [dbo].[fournisseur] OFF
GO

-- ============================================================
-- 6. CLIENTS 141-144 (for FirstTechnology)
-- ============================================================
SET IDENTITY_INSERT [dbo].[client] ON
IF NOT EXISTS (SELECT 1 FROM [dbo].[client] WHERE id = 141)
    INSERT [dbo].[client] ([id],[nom],[prenom],[email],[telephone],[adresse],[secteur_activite],[categorie_client_id],[entreprise_id],[created_at],[updated_at])
    VALUES (141,N'ProSoft Solutions',N'Directeur IT',N'achats@prosoft.tn',N'+216-25-141-101',N'Immeuble Lac 2, Tour A',N'Édition Logiciels',14,14,GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[client] WHERE id = 142)
    INSERT [dbo].[client] ([id],[nom],[prenom],[email],[telephone],[adresse],[secteur_activite],[categorie_client_id],[entreprise_id],[created_at],[updated_at])
    VALUES (142,N'Université Privée Mediterranée',N'Service Achats',N'info@upm.tn',N'+216-26-242-202',N'Campus UPM, Avenue de Carthage',N'Enseignement Supérieur',14,14,GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[client] WHERE id = 143)
    INSERT [dbo].[client] ([id],[nom],[prenom],[email],[telephone],[adresse],[secteur_activite],[categorie_client_id],[entreprise_id],[created_at],[updated_at])
    VALUES (143,N'Cabinet Audit & Conseil ACC',N'Associé Gérant',N'contact@acc-audit.tn',N'+216-27-343-303',N'Avenue Bourguiba, Tunis',N'Audit & Conseil',14,14,GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[client] WHERE id = 144)
    INSERT [dbo].[client] ([id],[nom],[prenom],[email],[telephone],[adresse],[secteur_activite],[categorie_client_id],[entreprise_id],[created_at],[updated_at])
    VALUES (144,N'TechHub Innovation Center',N'Responsable Achats',N'procurement@techhub.tn',N'+216-28-444-404',N'Espace El Manar, Bloc B',N'Incubateur & Coworking',14,14,GETDATE(),GETDATE())
SET IDENTITY_INSERT [dbo].[client] OFF
GO

-- ============================================================
-- 7. STOCK 1401-1405 (for FirstTechnology)
-- ============================================================
SET IDENTITY_INSERT [dbo].[stock] ON
IF NOT EXISTS (SELECT 1 FROM [dbo].[stock] WHERE id = 1401)
    INSERT [dbo].[stock] ([id],[article_id],[entreprise_id],[quantite_disponible],[quantite_minimale],[quantite_maximale],[emplacement],[created_at],[updated_at])
    VALUES (1401,1401,14,60.000,8.000,200.000,N'Entrepôt Principal – Étagère IT-A1',GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[stock] WHERE id = 1402)
    INSERT [dbo].[stock] ([id],[article_id],[entreprise_id],[quantite_disponible],[quantite_minimale],[quantite_maximale],[emplacement],[created_at],[updated_at])
    VALUES (1402,1402,14,90.000,15.000,300.000,N'Entrepôt Principal – Étagère IT-A2',GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[stock] WHERE id = 1403)
    INSERT [dbo].[stock] ([id],[article_id],[entreprise_id],[quantite_disponible],[quantite_minimale],[quantite_maximale],[emplacement],[created_at],[updated_at])
    VALUES (1403,1403,14,250.000,40.000,800.000,N'Entrepôt Principal – Étagère IT-B1',GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[stock] WHERE id = 1404)
    INSERT [dbo].[stock] ([id],[article_id],[entreprise_id],[quantite_disponible],[quantite_minimale],[quantite_maximale],[emplacement],[created_at],[updated_at])
    VALUES (1404,1404,14,300.000,50.000,1000.000,N'Entrepôt Principal – Étagère IT-B2',GETDATE(),GETDATE())
IF NOT EXISTS (SELECT 1 FROM [dbo].[stock] WHERE id = 1405)
    INSERT [dbo].[stock] ([id],[article_id],[entreprise_id],[quantite_disponible],[quantite_minimale],[quantite_maximale],[emplacement],[created_at],[updated_at])
    VALUES (1405,1405,14,180.000,25.000,500.000,N'Entrepôt Secondaire – Bac IT-K3',GETDATE(),GETDATE())
SET IDENTITY_INSERT [dbo].[stock] OFF
GO

-- ============================================================
-- 8. PURCHASE ORDERS (commande_achat) – 40 orders Jan-Mar 2026
--
-- Statut values: BROUILLON | EN_ATTENTE | CONFIRMEE | RECUE | ANNULEE
--
-- Templates (HT / TVA @19 % / TTC):
--   A   5400.00 / 1026.00 /  6426.00   (2x1401 + 3x1402)
--   B   3900.00 /  741.00 /  4641.00   (5x1402 + 5x1403)
--   C   5670.00 / 1077.30 /  6747.30   (3x1401 + 3x1404)
--   D    990.00 /  188.10 /  1178.10   (4x1403 + 3x1404)
--   E   5200.00 /  988.00 /  6188.00   (5x1402 + 10x1405)
--   F   6300.00 / 1197.00 /  7497.00   (3x1401 + 5x1403)
--   G   2580.00 /  490.20 /  3070.20   (4x1402 + 2x1404)
--   H   4500.00 /  855.00 /  5355.00   (2x1401 + 5x1403)
-- ============================================================

-- Clean up previous seed run (same ID range)
DELETE FROM [dbo].[ligne_commande_achat] WHERE [commande_achat_id] BETWEEN 14001 AND 14040
DELETE FROM [dbo].[commande_achat]        WHERE [id]                BETWEEN 14001 AND 14040
GO

SET IDENTITY_INSERT [dbo].[commande_achat] ON
GO

INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (14001,N'CA-FT-2026-001','2026-01-02','2026-01-12',N'RECUE',5400.00,1026.00,6426.00,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (14002,N'CA-FT-2026-002','2026-01-04','2026-01-14',N'RECUE',3900.00,741.00,4641.00,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (14003,N'CA-FT-2026-003','2026-01-06','2026-01-16',N'RECUE',5670.00,1077.30,6747.30,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (14004,N'CA-FT-2026-004','2026-01-08','2026-01-18',N'RECUE',990.00,188.10,1178.10,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (14005,N'CA-FT-2026-005','2026-01-10','2026-01-20',N'RECUE',5200.00,988.00,6188.00,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (14006,N'CA-FT-2026-006','2026-01-12','2026-01-22',N'RECUE',6300.00,1197.00,7497.00,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (14007,N'CA-FT-2026-007','2026-01-14','2026-01-24',N'RECUE',2580.00,490.20,3070.20,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (14008,N'CA-FT-2026-008','2026-01-16','2026-01-26',N'RECUE',4500.00,855.00,5355.00,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (14009,N'CA-FT-2026-009','2026-01-18','2026-01-28',N'RECUE',5400.00,1026.00,6426.00,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (14010,N'CA-FT-2026-010','2026-01-20','2026-01-30',N'RECUE',3900.00,741.00,4641.00,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (14011,N'CA-FT-2026-011','2026-01-22','2026-02-01',N'RECUE',5670.00,1077.30,6747.30,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (14012,N'CA-FT-2026-012','2026-01-24','2026-02-03',N'RECUE',990.00,188.10,1178.10,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (14013,N'CA-FT-2026-013','2026-01-26','2026-02-05',N'RECUE',5200.00,988.00,6188.00,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (14014,N'CA-FT-2026-014','2026-01-28','2026-02-07',N'RECUE',6300.00,1197.00,7497.00,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (14015,N'CA-FT-2026-015','2026-01-30','2026-02-09',N'RECUE',2580.00,490.20,3070.20,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (14016,N'CA-FT-2026-016','2026-02-01','2026-02-11',N'RECUE',4500.00,855.00,5355.00,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (14017,N'CA-FT-2026-017','2026-02-03','2026-02-13',N'RECUE',5400.00,1026.00,6426.00,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (14018,N'CA-FT-2026-018','2026-02-05','2026-02-15',N'RECUE',3900.00,741.00,4641.00,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (14019,N'CA-FT-2026-019','2026-02-07','2026-02-17',N'RECUE',5670.00,1077.30,6747.30,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (14020,N'CA-FT-2026-020','2026-02-09','2026-02-19',N'RECUE',990.00,188.10,1178.10,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (14021,N'CA-FT-2026-021','2026-02-11','2026-02-21',N'RECUE',5200.00,988.00,6188.00,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (14022,N'CA-FT-2026-022','2026-02-13','2026-02-23',N'RECUE',6300.00,1197.00,7497.00,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (14023,N'CA-FT-2026-023','2026-02-15','2026-02-25',N'RECUE',2580.00,490.20,3070.20,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (14024,N'CA-FT-2026-024','2026-02-17','2026-02-27',N'RECUE',4500.00,855.00,5355.00,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (14025,N'CA-FT-2026-025','2026-02-19','2026-03-01',N'RECUE',5400.00,1026.00,6426.00,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (14026,N'CA-FT-2026-026','2026-02-21','2026-03-03',N'RECUE',3900.00,741.00,4641.00,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (14027,N'CA-FT-2026-027','2026-02-23','2026-03-05',N'RECUE',5670.00,1077.30,6747.30,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (14028,N'CA-FT-2026-028','2026-02-25','2026-03-07',N'RECUE',990.00,188.10,1178.10,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (14029,N'CA-FT-2026-029','2026-02-27','2026-03-09',N'RECUE',5200.00,988.00,6188.00,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (14030,N'CA-FT-2026-030','2026-03-01','2026-03-11',N'RECUE',6300.00,1197.00,7497.00,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (14031,N'CA-FT-2026-031','2026-03-03','2026-03-13',N'RECUE',2580.00,490.20,3070.20,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (14032,N'CA-FT-2026-032','2026-03-05','2026-03-15',N'RECUE',4500.00,855.00,5355.00,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (14033,N'CA-FT-2026-033','2026-03-07','2026-03-17',N'CONFIRMEE',5400.00,1026.00,6426.00,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (14034,N'CA-FT-2026-034','2026-03-09','2026-03-19',N'CONFIRMEE',3900.00,741.00,4641.00,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (14035,N'CA-FT-2026-035','2026-03-11','2026-03-21',N'CONFIRMEE',5670.00,1077.30,6747.30,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (14036,N'CA-FT-2026-036','2026-03-13','2026-03-23',N'CONFIRMEE',990.00,188.10,1178.10,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (14037,N'CA-FT-2026-037','2026-03-15','2026-03-25',N'CONFIRMEE',5200.00,988.00,6188.00,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (14038,N'CA-FT-2026-038','2026-03-17',NULL,N'BROUILLON',6300.00,1197.00,7497.00,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (14039,N'CA-FT-2026-039','2026-03-19',NULL,N'BROUILLON',2580.00,490.20,3070.20,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_achat] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[fournisseur_id],[entreprise_id],[created_at],[updated_at]) VALUES (14040,N'CA-FT-2026-040','2026-03-21',NULL,N'BROUILLON',4500.00,855.00,5355.00,144,14,GETDATE(),GETDATE())

SET IDENTITY_INSERT [dbo].[commande_achat] OFF
GO

-- ============================================================
-- 9. PURCHASE ORDER LINE ITEMS (ligne_commande_achat)
--    montant_ht  = quantite * prix_unitaire_ht
--    montant_ttc = montant_ht * 1.19
-- ============================================================
INSERT [dbo].[ligne_commande_achat] ([commande_achat_id],[article_id],[quantite],[prix_unitaire_ht],[taux_tva],[montant_ht],[montant_ttc],[created_at],[updated_at]) VALUES
(14001,1401,2.000,1800.00,19.00,3600.00,4284.00,GETDATE(),GETDATE()),
(14001,1402,3.000,600.00,19.00,1800.00,2142.00,GETDATE(),GETDATE()),
(14002,1402,5.000,600.00,19.00,3000.00,3570.00,GETDATE(),GETDATE()),
(14002,1403,5.000,180.00,19.00,900.00,1071.00,GETDATE(),GETDATE()),
(14003,1401,3.000,1800.00,19.00,5400.00,6426.00,GETDATE(),GETDATE()),
(14003,1404,3.000,90.00,19.00,270.00,321.30,GETDATE(),GETDATE()),
(14004,1403,4.000,180.00,19.00,720.00,856.80,GETDATE(),GETDATE()),
(14004,1404,3.000,90.00,19.00,270.00,321.30,GETDATE(),GETDATE()),
(14005,1402,5.000,600.00,19.00,3000.00,3570.00,GETDATE(),GETDATE()),
(14005,1405,10.000,220.00,19.00,2200.00,2618.00,GETDATE(),GETDATE()),
(14006,1401,3.000,1800.00,19.00,5400.00,6426.00,GETDATE(),GETDATE()),
(14006,1403,5.000,180.00,19.00,900.00,1071.00,GETDATE(),GETDATE()),
(14007,1402,4.000,600.00,19.00,2400.00,2856.00,GETDATE(),GETDATE()),
(14007,1404,2.000,90.00,19.00,180.00,214.20,GETDATE(),GETDATE()),
(14008,1401,2.000,1800.00,19.00,3600.00,4284.00,GETDATE(),GETDATE()),
(14008,1403,5.000,180.00,19.00,900.00,1071.00,GETDATE(),GETDATE()),
(14009,1401,2.000,1800.00,19.00,3600.00,4284.00,GETDATE(),GETDATE()),
(14009,1402,3.000,600.00,19.00,1800.00,2142.00,GETDATE(),GETDATE()),
(14010,1402,5.000,600.00,19.00,3000.00,3570.00,GETDATE(),GETDATE()),
(14010,1403,5.000,180.00,19.00,900.00,1071.00,GETDATE(),GETDATE()),
(14011,1401,3.000,1800.00,19.00,5400.00,6426.00,GETDATE(),GETDATE()),
(14011,1404,3.000,90.00,19.00,270.00,321.30,GETDATE(),GETDATE()),
(14012,1403,4.000,180.00,19.00,720.00,856.80,GETDATE(),GETDATE()),
(14012,1404,3.000,90.00,19.00,270.00,321.30,GETDATE(),GETDATE()),
(14013,1402,5.000,600.00,19.00,3000.00,3570.00,GETDATE(),GETDATE()),
(14013,1405,10.000,220.00,19.00,2200.00,2618.00,GETDATE(),GETDATE()),
(14014,1401,3.000,1800.00,19.00,5400.00,6426.00,GETDATE(),GETDATE()),
(14014,1403,5.000,180.00,19.00,900.00,1071.00,GETDATE(),GETDATE()),
(14015,1402,4.000,600.00,19.00,2400.00,2856.00,GETDATE(),GETDATE()),
(14015,1404,2.000,90.00,19.00,180.00,214.20,GETDATE(),GETDATE()),
(14016,1401,2.000,1800.00,19.00,3600.00,4284.00,GETDATE(),GETDATE()),
(14016,1403,5.000,180.00,19.00,900.00,1071.00,GETDATE(),GETDATE()),
(14017,1401,2.000,1800.00,19.00,3600.00,4284.00,GETDATE(),GETDATE()),
(14017,1402,3.000,600.00,19.00,1800.00,2142.00,GETDATE(),GETDATE()),
(14018,1402,5.000,600.00,19.00,3000.00,3570.00,GETDATE(),GETDATE()),
(14018,1403,5.000,180.00,19.00,900.00,1071.00,GETDATE(),GETDATE()),
(14019,1401,3.000,1800.00,19.00,5400.00,6426.00,GETDATE(),GETDATE()),
(14019,1404,3.000,90.00,19.00,270.00,321.30,GETDATE(),GETDATE()),
(14020,1403,4.000,180.00,19.00,720.00,856.80,GETDATE(),GETDATE()),
(14020,1404,3.000,90.00,19.00,270.00,321.30,GETDATE(),GETDATE()),
(14021,1402,5.000,600.00,19.00,3000.00,3570.00,GETDATE(),GETDATE()),
(14021,1405,10.000,220.00,19.00,2200.00,2618.00,GETDATE(),GETDATE()),
(14022,1401,3.000,1800.00,19.00,5400.00,6426.00,GETDATE(),GETDATE()),
(14022,1403,5.000,180.00,19.00,900.00,1071.00,GETDATE(),GETDATE()),
(14023,1402,4.000,600.00,19.00,2400.00,2856.00,GETDATE(),GETDATE()),
(14023,1404,2.000,90.00,19.00,180.00,214.20,GETDATE(),GETDATE()),
(14024,1401,2.000,1800.00,19.00,3600.00,4284.00,GETDATE(),GETDATE()),
(14024,1403,5.000,180.00,19.00,900.00,1071.00,GETDATE(),GETDATE()),
(14025,1401,2.000,1800.00,19.00,3600.00,4284.00,GETDATE(),GETDATE()),
(14025,1402,3.000,600.00,19.00,1800.00,2142.00,GETDATE(),GETDATE()),
(14026,1402,5.000,600.00,19.00,3000.00,3570.00,GETDATE(),GETDATE()),
(14026,1403,5.000,180.00,19.00,900.00,1071.00,GETDATE(),GETDATE()),
(14027,1401,3.000,1800.00,19.00,5400.00,6426.00,GETDATE(),GETDATE()),
(14027,1404,3.000,90.00,19.00,270.00,321.30,GETDATE(),GETDATE()),
(14028,1403,4.000,180.00,19.00,720.00,856.80,GETDATE(),GETDATE()),
(14028,1404,3.000,90.00,19.00,270.00,321.30,GETDATE(),GETDATE()),
(14029,1402,5.000,600.00,19.00,3000.00,3570.00,GETDATE(),GETDATE()),
(14029,1405,10.000,220.00,19.00,2200.00,2618.00,GETDATE(),GETDATE()),
(14030,1401,3.000,1800.00,19.00,5400.00,6426.00,GETDATE(),GETDATE()),
(14030,1403,5.000,180.00,19.00,900.00,1071.00,GETDATE(),GETDATE()),
(14031,1402,4.000,600.00,19.00,2400.00,2856.00,GETDATE(),GETDATE()),
(14031,1404,2.000,90.00,19.00,180.00,214.20,GETDATE(),GETDATE()),
(14032,1401,2.000,1800.00,19.00,3600.00,4284.00,GETDATE(),GETDATE()),
(14032,1403,5.000,180.00,19.00,900.00,1071.00,GETDATE(),GETDATE()),
(14033,1401,2.000,1800.00,19.00,3600.00,4284.00,GETDATE(),GETDATE()),
(14033,1402,3.000,600.00,19.00,1800.00,2142.00,GETDATE(),GETDATE()),
(14034,1402,5.000,600.00,19.00,3000.00,3570.00,GETDATE(),GETDATE()),
(14034,1403,5.000,180.00,19.00,900.00,1071.00,GETDATE(),GETDATE()),
(14035,1401,3.000,1800.00,19.00,5400.00,6426.00,GETDATE(),GETDATE()),
(14035,1404,3.000,90.00,19.00,270.00,321.30,GETDATE(),GETDATE()),
(14036,1403,4.000,180.00,19.00,720.00,856.80,GETDATE(),GETDATE()),
(14036,1404,3.000,90.00,19.00,270.00,321.30,GETDATE(),GETDATE()),
(14037,1402,5.000,600.00,19.00,3000.00,3570.00,GETDATE(),GETDATE()),
(14037,1405,10.000,220.00,19.00,2200.00,2618.00,GETDATE(),GETDATE()),
(14038,1401,3.000,1800.00,19.00,5400.00,6426.00,GETDATE(),GETDATE()),
(14038,1403,5.000,180.00,19.00,900.00,1071.00,GETDATE(),GETDATE()),
(14039,1402,4.000,600.00,19.00,2400.00,2856.00,GETDATE(),GETDATE()),
(14039,1404,2.000,90.00,19.00,180.00,214.20,GETDATE(),GETDATE()),
(14040,1401,2.000,1800.00,19.00,3600.00,4284.00,GETDATE(),GETDATE()),
(14040,1403,5.000,180.00,19.00,900.00,1071.00,GETDATE(),GETDATE())
GO

-- ============================================================
-- 10. SALES ORDERS (commande_vente) – 75 orders Jan-Mar 2026
--
-- Statut values: BROUILLON | EN_ATTENTE | CONFIRMEE | LIVREE | ANNULEE
--
-- Templates (HT / TVA @19 % / TTC):
--   SA   8900.00 / 1691.00 / 10591.00   (3x1401 + 2x1402)
--   SB   4240.00 /  805.60 /  5045.60   (4x1402 + 3x1403)
--   SC   5920.00 / 1124.80 /  7044.80   (2x1401 + 4x1403)
--   SD   1700.00 /  323.00 /  2023.00   (5x1403 + 2x1404)
--   SE   6050.00 / 1149.50 /  7199.50   (3x1402 + 10x1405)
--   SF   4550.00 /  864.50 /  5414.50   (2x1404 + 5x1402)
--   SG   5250.00 /  997.50 /  6247.50   (2x1401 + 3x1404)
--   SH   5650.00 / 1073.50 /  6723.50   (5x1402 + 5x1403)
-- ============================================================

-- Clean up previous seed run
DELETE FROM [dbo].[ligne_commande_vente] WHERE [commande_vente_id] BETWEEN 14001 AND 14075
DELETE FROM [dbo].[commande_vente]        WHERE [id]                BETWEEN 14001 AND 14075
GO

SET IDENTITY_INSERT [dbo].[commande_vente] ON
GO

INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14001,N'CV-FT-2026-001','2026-01-02','2026-01-09',N'LIVREE',8900.00,1691.00,10591.00,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14002,N'CV-FT-2026-002','2026-01-03','2026-01-10',N'LIVREE',4240.00,805.60,5045.60,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14003,N'CV-FT-2026-003','2026-01-04','2026-01-11',N'LIVREE',5920.00,1124.80,7044.80,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14004,N'CV-FT-2026-004','2026-01-05','2026-01-12',N'LIVREE',1700.00,323.00,2023.00,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14005,N'CV-FT-2026-005','2026-01-06','2026-01-13',N'LIVREE',6050.00,1149.50,7199.50,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14006,N'CV-FT-2026-006','2026-01-07','2026-01-14',N'LIVREE',4550.00,864.50,5414.50,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14007,N'CV-FT-2026-007','2026-01-08','2026-01-15',N'LIVREE',5250.00,997.50,6247.50,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14008,N'CV-FT-2026-008','2026-01-09','2026-01-16',N'LIVREE',5650.00,1073.50,6723.50,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14009,N'CV-FT-2026-009','2026-01-10','2026-01-17',N'LIVREE',8900.00,1691.00,10591.00,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14010,N'CV-FT-2026-010','2026-01-11','2026-01-18',N'LIVREE',4240.00,805.60,5045.60,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14011,N'CV-FT-2026-011','2026-01-12','2026-01-19',N'LIVREE',5920.00,1124.80,7044.80,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14012,N'CV-FT-2026-012','2026-01-13','2026-01-20',N'LIVREE',1700.00,323.00,2023.00,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14013,N'CV-FT-2026-013','2026-01-14','2026-01-21',N'LIVREE',6050.00,1149.50,7199.50,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14014,N'CV-FT-2026-014','2026-01-15','2026-01-22',N'LIVREE',4550.00,864.50,5414.50,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14015,N'CV-FT-2026-015','2026-01-16','2026-01-23',N'LIVREE',5250.00,997.50,6247.50,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14016,N'CV-FT-2026-016','2026-01-17','2026-01-24',N'LIVREE',5650.00,1073.50,6723.50,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14017,N'CV-FT-2026-017','2026-01-18','2026-01-25',N'LIVREE',8900.00,1691.00,10591.00,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14018,N'CV-FT-2026-018','2026-01-19','2026-01-26',N'LIVREE',4240.00,805.60,5045.60,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14019,N'CV-FT-2026-019','2026-01-20','2026-01-27',N'LIVREE',5920.00,1124.80,7044.80,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14020,N'CV-FT-2026-020','2026-01-21','2026-01-28',N'LIVREE',1700.00,323.00,2023.00,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14021,N'CV-FT-2026-021','2026-01-22','2026-01-29',N'LIVREE',6050.00,1149.50,7199.50,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14022,N'CV-FT-2026-022','2026-01-23','2026-01-30',N'LIVREE',4550.00,864.50,5414.50,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14023,N'CV-FT-2026-023','2026-01-24','2026-01-31',N'LIVREE',5250.00,997.50,6247.50,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14024,N'CV-FT-2026-024','2026-01-25','2026-02-01',N'LIVREE',5650.00,1073.50,6723.50,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14025,N'CV-FT-2026-025','2026-01-26','2026-02-02',N'LIVREE',8900.00,1691.00,10591.00,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14026,N'CV-FT-2026-026','2026-01-27','2026-02-03',N'LIVREE',4240.00,805.60,5045.60,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14027,N'CV-FT-2026-027','2026-01-28','2026-02-04',N'LIVREE',5920.00,1124.80,7044.80,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14028,N'CV-FT-2026-028','2026-01-29','2026-02-05',N'LIVREE',1700.00,323.00,2023.00,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14029,N'CV-FT-2026-029','2026-01-30','2026-02-06',N'LIVREE',6050.00,1149.50,7199.50,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14030,N'CV-FT-2026-030','2026-01-31','2026-02-07',N'LIVREE',4550.00,864.50,5414.50,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14031,N'CV-FT-2026-031','2026-02-01','2026-02-08',N'LIVREE',5250.00,997.50,6247.50,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14032,N'CV-FT-2026-032','2026-02-02','2026-02-09',N'LIVREE',5650.00,1073.50,6723.50,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14033,N'CV-FT-2026-033','2026-02-03','2026-02-10',N'LIVREE',8900.00,1691.00,10591.00,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14034,N'CV-FT-2026-034','2026-02-04','2026-02-11',N'LIVREE',4240.00,805.60,5045.60,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14035,N'CV-FT-2026-035','2026-02-05','2026-02-12',N'LIVREE',5920.00,1124.80,7044.80,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14036,N'CV-FT-2026-036','2026-02-06','2026-02-13',N'LIVREE',1700.00,323.00,2023.00,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14037,N'CV-FT-2026-037','2026-02-07','2026-02-14',N'LIVREE',6050.00,1149.50,7199.50,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14038,N'CV-FT-2026-038','2026-02-08','2026-02-15',N'LIVREE',4550.00,864.50,5414.50,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14039,N'CV-FT-2026-039','2026-02-09','2026-02-16',N'LIVREE',5250.00,997.50,6247.50,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14040,N'CV-FT-2026-040','2026-02-10','2026-02-17',N'LIVREE',5650.00,1073.50,6723.50,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14041,N'CV-FT-2026-041','2026-02-11','2026-02-18',N'LIVREE',8900.00,1691.00,10591.00,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14042,N'CV-FT-2026-042','2026-02-12','2026-02-19',N'LIVREE',4240.00,805.60,5045.60,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14043,N'CV-FT-2026-043','2026-02-13','2026-02-20',N'LIVREE',5920.00,1124.80,7044.80,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14044,N'CV-FT-2026-044','2026-02-14','2026-02-21',N'LIVREE',1700.00,323.00,2023.00,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14045,N'CV-FT-2026-045','2026-02-15','2026-02-22',N'LIVREE',6050.00,1149.50,7199.50,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14046,N'CV-FT-2026-046','2026-02-16','2026-02-23',N'LIVREE',4550.00,864.50,5414.50,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14047,N'CV-FT-2026-047','2026-02-17','2026-02-24',N'LIVREE',5250.00,997.50,6247.50,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14048,N'CV-FT-2026-048','2026-02-18','2026-02-25',N'LIVREE',5650.00,1073.50,6723.50,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14049,N'CV-FT-2026-049','2026-02-19','2026-02-26',N'LIVREE',8900.00,1691.00,10591.00,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14050,N'CV-FT-2026-050','2026-02-20','2026-02-27',N'LIVREE',4240.00,805.60,5045.60,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14051,N'CV-FT-2026-051','2026-02-21','2026-02-28',N'LIVREE',5920.00,1124.80,7044.80,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14052,N'CV-FT-2026-052','2026-02-22','2026-03-01',N'LIVREE',1700.00,323.00,2023.00,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14053,N'CV-FT-2026-053','2026-02-23','2026-03-02',N'LIVREE',6050.00,1149.50,7199.50,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14054,N'CV-FT-2026-054','2026-02-24','2026-03-03',N'LIVREE',4550.00,864.50,5414.50,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14055,N'CV-FT-2026-055','2026-02-25','2026-03-04',N'LIVREE',5250.00,997.50,6247.50,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14056,N'CV-FT-2026-056','2026-02-26','2026-03-05',N'LIVREE',5650.00,1073.50,6723.50,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14057,N'CV-FT-2026-057','2026-02-27','2026-03-06',N'LIVREE',8900.00,1691.00,10591.00,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14058,N'CV-FT-2026-058','2026-02-28','2026-03-07',N'LIVREE',4240.00,805.60,5045.60,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14059,N'CV-FT-2026-059','2026-03-01','2026-03-08',N'LIVREE',5920.00,1124.80,7044.80,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14060,N'CV-FT-2026-060','2026-03-02','2026-03-09',N'LIVREE',1700.00,323.00,2023.00,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14061,N'CV-FT-2026-061','2026-03-03','2026-03-10',N'LIVREE',6050.00,1149.50,7199.50,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14062,N'CV-FT-2026-062','2026-03-04','2026-03-11',N'LIVREE',4550.00,864.50,5414.50,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14063,N'CV-FT-2026-063','2026-03-05','2026-03-12',N'LIVREE',5250.00,997.50,6247.50,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14064,N'CV-FT-2026-064','2026-03-06','2026-03-13',N'LIVREE',5650.00,1073.50,6723.50,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14065,N'CV-FT-2026-065','2026-03-07','2026-03-14',N'LIVREE',8900.00,1691.00,10591.00,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14066,N'CV-FT-2026-066','2026-03-08','2026-03-15',N'LIVREE',4240.00,805.60,5045.60,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14067,N'CV-FT-2026-067','2026-03-09',NULL,N'CONFIRMEE',5920.00,1124.80,7044.80,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14068,N'CV-FT-2026-068','2026-03-10',NULL,N'CONFIRMEE',1700.00,323.00,2023.00,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14069,N'CV-FT-2026-069','2026-03-11',NULL,N'CONFIRMEE',6050.00,1149.50,7199.50,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14070,N'CV-FT-2026-070','2026-03-12',NULL,N'CONFIRMEE',4550.00,864.50,5414.50,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14071,N'CV-FT-2026-071','2026-03-13',NULL,N'CONFIRMEE',5250.00,997.50,6247.50,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14072,N'CV-FT-2026-072','2026-03-14',NULL,N'CONFIRMEE',5650.00,1073.50,6723.50,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14073,N'CV-FT-2026-073','2026-03-15',NULL,N'CONFIRMEE',8900.00,1691.00,10591.00,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14074,N'CV-FT-2026-074','2026-03-16',NULL,N'BROUILLON',4240.00,805.60,5045.60,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14075,N'CV-FT-2026-075','2026-03-17',NULL,N'BROUILLON',5920.00,1124.80,7044.80,143,14,GETDATE(),GETDATE())

SET IDENTITY_INSERT [dbo].[commande_vente] OFF
GO

-- ============================================================
-- 11. SALES ORDER LINE ITEMS (ligne_commande_vente)
-- ============================================================
INSERT [dbo].[ligne_commande_vente] ([commande_vente_id],[article_id],[quantite],[prix_unitaire_ht],[taux_tva],[montant_ht],[montant_ttc],[created_at],[updated_at]) VALUES
(14001,1401,3.000,2400.00,19.00,7200.00,8568.00,GETDATE(),GETDATE()),
(14001,1402,2.000,850.00,19.00,1700.00,2023.00,GETDATE(),GETDATE()),
(14002,1402,4.000,850.00,19.00,3400.00,4046.00,GETDATE(),GETDATE()),
(14002,1403,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
(14003,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14003,1403,4.000,280.00,19.00,1120.00,1332.80,GETDATE(),GETDATE()),
(14004,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14004,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14005,1402,3.000,850.00,19.00,2550.00,3034.50,GETDATE(),GETDATE()),
(14005,1405,10.000,350.00,19.00,3500.00,4165.00,GETDATE(),GETDATE()),
(14006,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14006,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14007,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14007,1404,3.000,150.00,19.00,450.00,535.50,GETDATE(),GETDATE()),
(14008,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14008,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14009,1401,3.000,2400.00,19.00,7200.00,8568.00,GETDATE(),GETDATE()),
(14009,1402,2.000,850.00,19.00,1700.00,2023.00,GETDATE(),GETDATE()),
(14010,1402,4.000,850.00,19.00,3400.00,4046.00,GETDATE(),GETDATE()),
(14010,1403,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
(14011,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14011,1403,4.000,280.00,19.00,1120.00,1332.80,GETDATE(),GETDATE()),
(14012,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14012,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14013,1402,3.000,850.00,19.00,2550.00,3034.50,GETDATE(),GETDATE()),
(14013,1405,10.000,350.00,19.00,3500.00,4165.00,GETDATE(),GETDATE()),
(14014,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14014,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14015,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14015,1404,3.000,150.00,19.00,450.00,535.50,GETDATE(),GETDATE()),
(14016,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14016,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14017,1401,3.000,2400.00,19.00,7200.00,8568.00,GETDATE(),GETDATE()),
(14017,1402,2.000,850.00,19.00,1700.00,2023.00,GETDATE(),GETDATE()),
(14018,1402,4.000,850.00,19.00,3400.00,4046.00,GETDATE(),GETDATE()),
(14018,1403,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
(14019,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14019,1403,4.000,280.00,19.00,1120.00,1332.80,GETDATE(),GETDATE()),
(14020,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14020,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14021,1402,3.000,850.00,19.00,2550.00,3034.50,GETDATE(),GETDATE()),
(14021,1405,10.000,350.00,19.00,3500.00,4165.00,GETDATE(),GETDATE()),
(14022,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14022,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14023,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14023,1404,3.000,150.00,19.00,450.00,535.50,GETDATE(),GETDATE()),
(14024,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14024,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14025,1401,3.000,2400.00,19.00,7200.00,8568.00,GETDATE(),GETDATE()),
(14025,1402,2.000,850.00,19.00,1700.00,2023.00,GETDATE(),GETDATE()),
(14026,1402,4.000,850.00,19.00,3400.00,4046.00,GETDATE(),GETDATE()),
(14026,1403,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
(14027,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14027,1403,4.000,280.00,19.00,1120.00,1332.80,GETDATE(),GETDATE()),
(14028,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14028,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14029,1402,3.000,850.00,19.00,2550.00,3034.50,GETDATE(),GETDATE()),
(14029,1405,10.000,350.00,19.00,3500.00,4165.00,GETDATE(),GETDATE()),
(14030,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14030,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14031,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14031,1404,3.000,150.00,19.00,450.00,535.50,GETDATE(),GETDATE()),
(14032,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14032,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14033,1401,3.000,2400.00,19.00,7200.00,8568.00,GETDATE(),GETDATE()),
(14033,1402,2.000,850.00,19.00,1700.00,2023.00,GETDATE(),GETDATE()),
(14034,1402,4.000,850.00,19.00,3400.00,4046.00,GETDATE(),GETDATE()),
(14034,1403,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
(14035,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14035,1403,4.000,280.00,19.00,1120.00,1332.80,GETDATE(),GETDATE()),
(14036,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14036,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14037,1402,3.000,850.00,19.00,2550.00,3034.50,GETDATE(),GETDATE()),
(14037,1405,10.000,350.00,19.00,3500.00,4165.00,GETDATE(),GETDATE()),
(14038,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14038,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14039,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14039,1404,3.000,150.00,19.00,450.00,535.50,GETDATE(),GETDATE()),
(14040,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14040,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14041,1401,3.000,2400.00,19.00,7200.00,8568.00,GETDATE(),GETDATE()),
(14041,1402,2.000,850.00,19.00,1700.00,2023.00,GETDATE(),GETDATE()),
(14042,1402,4.000,850.00,19.00,3400.00,4046.00,GETDATE(),GETDATE()),
(14042,1403,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
(14043,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14043,1403,4.000,280.00,19.00,1120.00,1332.80,GETDATE(),GETDATE()),
(14044,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14044,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14045,1402,3.000,850.00,19.00,2550.00,3034.50,GETDATE(),GETDATE()),
(14045,1405,10.000,350.00,19.00,3500.00,4165.00,GETDATE(),GETDATE()),
(14046,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14046,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14047,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14047,1404,3.000,150.00,19.00,450.00,535.50,GETDATE(),GETDATE()),
(14048,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14048,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14049,1401,3.000,2400.00,19.00,7200.00,8568.00,GETDATE(),GETDATE()),
(14049,1402,2.000,850.00,19.00,1700.00,2023.00,GETDATE(),GETDATE()),
(14050,1402,4.000,850.00,19.00,3400.00,4046.00,GETDATE(),GETDATE()),
(14050,1403,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
(14051,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14051,1403,4.000,280.00,19.00,1120.00,1332.80,GETDATE(),GETDATE()),
(14052,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14052,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14053,1402,3.000,850.00,19.00,2550.00,3034.50,GETDATE(),GETDATE()),
(14053,1405,10.000,350.00,19.00,3500.00,4165.00,GETDATE(),GETDATE()),
(14054,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14054,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14055,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14055,1404,3.000,150.00,19.00,450.00,535.50,GETDATE(),GETDATE()),
(14056,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14056,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14057,1401,3.000,2400.00,19.00,7200.00,8568.00,GETDATE(),GETDATE()),
(14057,1402,2.000,850.00,19.00,1700.00,2023.00,GETDATE(),GETDATE()),
(14058,1402,4.000,850.00,19.00,3400.00,4046.00,GETDATE(),GETDATE()),
(14058,1403,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
(14059,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14059,1403,4.000,280.00,19.00,1120.00,1332.80,GETDATE(),GETDATE()),
(14060,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14060,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14061,1402,3.000,850.00,19.00,2550.00,3034.50,GETDATE(),GETDATE()),
(14061,1405,10.000,350.00,19.00,3500.00,4165.00,GETDATE(),GETDATE()),
(14062,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14062,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14063,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14063,1404,3.000,150.00,19.00,450.00,535.50,GETDATE(),GETDATE()),
(14064,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14064,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14065,1401,3.000,2400.00,19.00,7200.00,8568.00,GETDATE(),GETDATE()),
(14065,1402,2.000,850.00,19.00,1700.00,2023.00,GETDATE(),GETDATE()),
(14066,1402,4.000,850.00,19.00,3400.00,4046.00,GETDATE(),GETDATE()),
(14066,1403,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
(14067,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14067,1403,4.000,280.00,19.00,1120.00,1332.80,GETDATE(),GETDATE()),
(14068,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14068,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14069,1402,3.000,850.00,19.00,2550.00,3034.50,GETDATE(),GETDATE()),
(14069,1405,10.000,350.00,19.00,3500.00,4165.00,GETDATE(),GETDATE()),
(14070,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14070,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14071,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14071,1404,3.000,150.00,19.00,450.00,535.50,GETDATE(),GETDATE()),
(14072,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14072,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14073,1401,3.000,2400.00,19.00,7200.00,8568.00,GETDATE(),GETDATE()),
(14073,1402,2.000,850.00,19.00,1700.00,2023.00,GETDATE(),GETDATE()),
(14074,1402,4.000,850.00,19.00,3400.00,4046.00,GETDATE(),GETDATE()),
(14074,1403,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
(14075,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14075,1403,4.000,280.00,19.00,1120.00,1332.80,GETDATE(),GETDATE())
GO

-- ============================================================
-- 12. SUMMARY VALIDATION
-- ============================================================
PRINT N''
PRINT N'============================================================'
PRINT N' FIRSTTECHNOLOGY ENTERPRISE (ID 14) – DATA INSERTION COMPLETE'
PRINT N'============================================================'
PRINT N''

SELECT
    N'PURCHASE ORDERS' AS [Type],
    COUNT(*)           AS [Total Orders],
    SUM(montant_ht)    AS [Total HT],
    SUM(montant_tva)   AS [Total TVA],
    SUM(montant_ttc)   AS [Total TTC]
FROM [dbo].[commande_achat]
WHERE entreprise_id = 14
  AND id BETWEEN 14001 AND 14040
UNION ALL
SELECT
    N'SALES ORDERS'    AS [Type],
    COUNT(*)           AS [Total Orders],
    SUM(montant_ht)    AS [Total HT],
    SUM(montant_tva)   AS [Total TVA],
    SUM(montant_ttc)   AS [Total TTC]
FROM [dbo].[commande_vente]
WHERE entreprise_id = 14
  AND id BETWEEN 14001 AND 14075
GO

SELECT
    YEAR(date_commande) AS [Year], MONTH(date_commande) AS [Month],
    N'ACHAT' AS [Type], COUNT(*) AS [Orders], SUM(montant_ttc) AS [Total TTC]
FROM [dbo].[commande_achat]
WHERE entreprise_id = 14 AND id BETWEEN 14001 AND 14040
GROUP BY YEAR(date_commande), MONTH(date_commande)
UNION ALL
SELECT YEAR(date_commande), MONTH(date_commande),
    N'VENTE', COUNT(*), SUM(montant_ttc)
FROM [dbo].[commande_vente]
WHERE entreprise_id = 14 AND id BETWEEN 14001 AND 14075
GROUP BY YEAR(date_commande), MONTH(date_commande)
ORDER BY [Year], [Month], [Type]
GO
