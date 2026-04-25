-- ============================================================
-- FIRSTTECHNOLOGY ENTERPRISE (ID: 14) – AI / FORECAST RICH SEED
-- ============================================================
-- Purpose : Provide enough historical depth + a back-tested AI prediction
--           log so the AI Intelligence dashboard (Forecast + "Historique :
--           Prédictions vs CA Réel") shows a meaningful track record.
-- Scope   : Apr 2025 → Dec 2025 sales history (182 commande_vente rows)
--           + 12 monthly prediction issuances × 3 horizons = 36 prediction
--           log rows (27 settled, 9 pending) for entreprise_id = 14.
-- Run AFTER the main file: firsttechnology_enterprise_14_seed.sql
-- SAFE TO RE-RUN: DELETE ranges scope to this seed's IDs only.
-- ============================================================
USE [BusinessManager]
GO

-- ============================================================
-- 1. CLEAN UP PREVIOUS RUN (this seed only)
-- ============================================================
DELETE FROM [dbo].[ligne_commande_vente] WHERE [commande_vente_id] BETWEEN 14101 AND 14299
DELETE FROM [dbo].[commande_vente]        WHERE [id]                BETWEEN 14101 AND 14299
DELETE FROM [dbo].[prediction_log]        WHERE [entreprise_id] = 14
GO

-- ============================================================
-- 2. HISTORICAL SALES ORDERS (Apr 2025 – Dec 2025, 182 orders)
-- ============================================================
SET IDENTITY_INSERT [dbo].[commande_vente] ON
GO

INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14101,N'CV-FT-HIST-14101','2025-04-01','2025-04-08',N'LIVREE',8900.00,1691.00,10591.00,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14102,N'CV-FT-HIST-14102','2025-04-03','2025-04-10',N'LIVREE',4240.00,805.60,5045.60,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14103,N'CV-FT-HIST-14103','2025-04-05','2025-04-12',N'LIVREE',5920.00,1124.80,7044.80,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14104,N'CV-FT-HIST-14104','2025-04-07','2025-04-14',N'LIVREE',1700.00,323.00,2023.00,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14105,N'CV-FT-HIST-14105','2025-04-09','2025-04-16',N'LIVREE',6050.00,1149.50,7199.50,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14106,N'CV-FT-HIST-14106','2025-04-12','2025-04-19',N'LIVREE',4550.00,864.50,5414.50,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14107,N'CV-FT-HIST-14107','2025-04-14','2025-04-21',N'LIVREE',5250.00,997.50,6247.50,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14108,N'CV-FT-HIST-14108','2025-04-16','2025-04-23',N'LIVREE',5650.00,1073.50,6723.50,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14109,N'CV-FT-HIST-14109','2025-04-18','2025-04-25',N'LIVREE',8900.00,1691.00,10591.00,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14110,N'CV-FT-HIST-14110','2025-04-21','2025-04-28',N'LIVREE',4240.00,805.60,5045.60,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14111,N'CV-FT-HIST-14111','2025-04-23','2025-04-30',N'LIVREE',5920.00,1124.80,7044.80,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14112,N'CV-FT-HIST-14112','2025-04-25','2025-05-02',N'LIVREE',1700.00,323.00,2023.00,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14113,N'CV-FT-HIST-14113','2025-04-27','2025-05-04',N'LIVREE',6050.00,1149.50,7199.50,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14114,N'CV-FT-HIST-14114','2025-04-30','2025-05-07',N'LIVREE',4550.00,864.50,5414.50,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14115,N'CV-FT-HIST-14115','2025-05-01','2025-05-08',N'LIVREE',5250.00,997.50,6247.50,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14116,N'CV-FT-HIST-14116','2025-05-03','2025-05-10',N'LIVREE',5650.00,1073.50,6723.50,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14117,N'CV-FT-HIST-14117','2025-05-05','2025-05-12',N'LIVREE',8900.00,1691.00,10591.00,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14118,N'CV-FT-HIST-14118','2025-05-07','2025-05-14',N'LIVREE',4240.00,805.60,5045.60,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14119,N'CV-FT-HIST-14119','2025-05-09','2025-05-16',N'LIVREE',5920.00,1124.80,7044.80,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14120,N'CV-FT-HIST-14120','2025-05-11','2025-05-18',N'LIVREE',1700.00,323.00,2023.00,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14121,N'CV-FT-HIST-14121','2025-05-13','2025-05-20',N'LIVREE',6050.00,1149.50,7199.50,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14122,N'CV-FT-HIST-14122','2025-05-15','2025-05-22',N'LIVREE',4550.00,864.50,5414.50,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14123,N'CV-FT-HIST-14123','2025-05-17','2025-05-24',N'LIVREE',5250.00,997.50,6247.50,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14124,N'CV-FT-HIST-14124','2025-05-19','2025-05-26',N'LIVREE',5650.00,1073.50,6723.50,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14125,N'CV-FT-HIST-14125','2025-05-21','2025-05-28',N'LIVREE',8900.00,1691.00,10591.00,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14126,N'CV-FT-HIST-14126','2025-05-23','2025-05-30',N'LIVREE',4240.00,805.60,5045.60,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14127,N'CV-FT-HIST-14127','2025-05-25','2025-06-01',N'LIVREE',5920.00,1124.80,7044.80,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14128,N'CV-FT-HIST-14128','2025-05-27','2025-06-03',N'LIVREE',1700.00,323.00,2023.00,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14129,N'CV-FT-HIST-14129','2025-05-29','2025-06-05',N'LIVREE',6050.00,1149.50,7199.50,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14130,N'CV-FT-HIST-14130','2025-05-31','2025-06-07',N'LIVREE',4550.00,864.50,5414.50,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14131,N'CV-FT-HIST-14131','2025-06-01','2025-06-08',N'LIVREE',5250.00,997.50,6247.50,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14132,N'CV-FT-HIST-14132','2025-06-02','2025-06-09',N'LIVREE',5650.00,1073.50,6723.50,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14133,N'CV-FT-HIST-14133','2025-06-04','2025-06-11',N'LIVREE',8900.00,1691.00,10591.00,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14134,N'CV-FT-HIST-14134','2025-06-06','2025-06-13',N'LIVREE',4240.00,805.60,5045.60,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14135,N'CV-FT-HIST-14135','2025-06-07','2025-06-14',N'LIVREE',5920.00,1124.80,7044.80,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14136,N'CV-FT-HIST-14136','2025-06-09','2025-06-16',N'LIVREE',1700.00,323.00,2023.00,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14137,N'CV-FT-HIST-14137','2025-06-11','2025-06-18',N'LIVREE',6050.00,1149.50,7199.50,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14138,N'CV-FT-HIST-14138','2025-06-12','2025-06-19',N'LIVREE',4550.00,864.50,5414.50,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14139,N'CV-FT-HIST-14139','2025-06-14','2025-06-21',N'LIVREE',5250.00,997.50,6247.50,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14140,N'CV-FT-HIST-14140','2025-06-16','2025-06-23',N'LIVREE',5650.00,1073.50,6723.50,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14141,N'CV-FT-HIST-14141','2025-06-18','2025-06-25',N'LIVREE',8900.00,1691.00,10591.00,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14142,N'CV-FT-HIST-14142','2025-06-19','2025-06-26',N'LIVREE',4240.00,805.60,5045.60,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14143,N'CV-FT-HIST-14143','2025-06-21','2025-06-28',N'LIVREE',5920.00,1124.80,7044.80,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14144,N'CV-FT-HIST-14144','2025-06-23','2025-06-30',N'LIVREE',1700.00,323.00,2023.00,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14145,N'CV-FT-HIST-14145','2025-06-24','2025-07-01',N'LIVREE',6050.00,1149.50,7199.50,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14146,N'CV-FT-HIST-14146','2025-06-26','2025-07-03',N'LIVREE',4550.00,864.50,5414.50,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14147,N'CV-FT-HIST-14147','2025-06-28','2025-07-05',N'LIVREE',5250.00,997.50,6247.50,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14148,N'CV-FT-HIST-14148','2025-06-30','2025-07-07',N'LIVREE',5650.00,1073.50,6723.50,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14149,N'CV-FT-HIST-14149','2025-07-01','2025-07-08',N'LIVREE',8900.00,1691.00,10591.00,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14150,N'CV-FT-HIST-14150','2025-07-03','2025-07-10',N'LIVREE',4240.00,805.60,5045.60,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14151,N'CV-FT-HIST-14151','2025-07-06','2025-07-13',N'LIVREE',5920.00,1124.80,7044.80,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14152,N'CV-FT-HIST-14152','2025-07-09','2025-07-16',N'LIVREE',1700.00,323.00,2023.00,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14153,N'CV-FT-HIST-14153','2025-07-11','2025-07-18',N'LIVREE',6050.00,1149.50,7199.50,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14154,N'CV-FT-HIST-14154','2025-07-14','2025-07-21',N'LIVREE',4550.00,864.50,5414.50,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14155,N'CV-FT-HIST-14155','2025-07-17','2025-07-24',N'LIVREE',5250.00,997.50,6247.50,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14156,N'CV-FT-HIST-14156','2025-07-20','2025-07-27',N'LIVREE',5650.00,1073.50,6723.50,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14157,N'CV-FT-HIST-14157','2025-07-22','2025-07-29',N'LIVREE',8900.00,1691.00,10591.00,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14158,N'CV-FT-HIST-14158','2025-07-25','2025-08-01',N'LIVREE',4240.00,805.60,5045.60,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14159,N'CV-FT-HIST-14159','2025-07-28','2025-08-04',N'LIVREE',5920.00,1124.80,7044.80,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14160,N'CV-FT-HIST-14160','2025-07-31','2025-08-07',N'LIVREE',1700.00,323.00,2023.00,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14161,N'CV-FT-HIST-14161','2025-08-01','2025-08-08',N'LIVREE',6050.00,1149.50,7199.50,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14162,N'CV-FT-HIST-14162','2025-08-04','2025-08-11',N'LIVREE',4550.00,864.50,5414.50,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14163,N'CV-FT-HIST-14163','2025-08-07','2025-08-14',N'LIVREE',5250.00,997.50,6247.50,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14164,N'CV-FT-HIST-14164','2025-08-10','2025-08-17',N'LIVREE',5650.00,1073.50,6723.50,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14165,N'CV-FT-HIST-14165','2025-08-13','2025-08-20',N'LIVREE',8900.00,1691.00,10591.00,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14166,N'CV-FT-HIST-14166','2025-08-16','2025-08-23',N'LIVREE',4240.00,805.60,5045.60,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14167,N'CV-FT-HIST-14167','2025-08-19','2025-08-26',N'LIVREE',5920.00,1124.80,7044.80,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14168,N'CV-FT-HIST-14168','2025-08-22','2025-08-29',N'LIVREE',1700.00,323.00,2023.00,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14169,N'CV-FT-HIST-14169','2025-08-25','2025-09-01',N'LIVREE',6050.00,1149.50,7199.50,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14170,N'CV-FT-HIST-14170','2025-08-28','2025-09-04',N'LIVREE',4550.00,864.50,5414.50,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14171,N'CV-FT-HIST-14171','2025-08-31','2025-09-07',N'LIVREE',5250.00,997.50,6247.50,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14172,N'CV-FT-HIST-14172','2025-09-01','2025-09-08',N'LIVREE',5650.00,1073.50,6723.50,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14173,N'CV-FT-HIST-14173','2025-09-02','2025-09-09',N'LIVREE',8900.00,1691.00,10591.00,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14174,N'CV-FT-HIST-14174','2025-09-03','2025-09-10',N'LIVREE',4240.00,805.60,5045.60,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14175,N'CV-FT-HIST-14175','2025-09-05','2025-09-12',N'LIVREE',5920.00,1124.80,7044.80,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14176,N'CV-FT-HIST-14176','2025-09-06','2025-09-13',N'LIVREE',1700.00,323.00,2023.00,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14177,N'CV-FT-HIST-14177','2025-09-07','2025-09-14',N'LIVREE',6050.00,1149.50,7199.50,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14178,N'CV-FT-HIST-14178','2025-09-09','2025-09-16',N'LIVREE',4550.00,864.50,5414.50,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14179,N'CV-FT-HIST-14179','2025-09-10','2025-09-17',N'LIVREE',5250.00,997.50,6247.50,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14180,N'CV-FT-HIST-14180','2025-09-12','2025-09-19',N'LIVREE',5650.00,1073.50,6723.50,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14181,N'CV-FT-HIST-14181','2025-09-13','2025-09-20',N'LIVREE',8900.00,1691.00,10591.00,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14182,N'CV-FT-HIST-14182','2025-09-14','2025-09-21',N'LIVREE',4240.00,805.60,5045.60,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14183,N'CV-FT-HIST-14183','2025-09-16','2025-09-23',N'LIVREE',5920.00,1124.80,7044.80,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14184,N'CV-FT-HIST-14184','2025-09-17','2025-09-24',N'LIVREE',1700.00,323.00,2023.00,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14185,N'CV-FT-HIST-14185','2025-09-18','2025-09-25',N'LIVREE',6050.00,1149.50,7199.50,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14186,N'CV-FT-HIST-14186','2025-09-20','2025-09-27',N'LIVREE',4550.00,864.50,5414.50,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14187,N'CV-FT-HIST-14187','2025-09-21','2025-09-28',N'LIVREE',5250.00,997.50,6247.50,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14188,N'CV-FT-HIST-14188','2025-09-23','2025-09-30',N'LIVREE',5650.00,1073.50,6723.50,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14189,N'CV-FT-HIST-14189','2025-09-24','2025-10-01',N'LIVREE',8900.00,1691.00,10591.00,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14190,N'CV-FT-HIST-14190','2025-09-25','2025-10-02',N'LIVREE',4240.00,805.60,5045.60,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14191,N'CV-FT-HIST-14191','2025-09-27','2025-10-04',N'LIVREE',5920.00,1124.80,7044.80,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14192,N'CV-FT-HIST-14192','2025-09-28','2025-10-05',N'LIVREE',1700.00,323.00,2023.00,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14193,N'CV-FT-HIST-14193','2025-09-30','2025-10-07',N'LIVREE',6050.00,1149.50,7199.50,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14194,N'CV-FT-HIST-14194','2025-10-01','2025-10-08',N'LIVREE',4550.00,864.50,5414.50,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14195,N'CV-FT-HIST-14195','2025-10-02','2025-10-09',N'LIVREE',5250.00,997.50,6247.50,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14196,N'CV-FT-HIST-14196','2025-10-03','2025-10-10',N'LIVREE',5650.00,1073.50,6723.50,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14197,N'CV-FT-HIST-14197','2025-10-04','2025-10-11',N'LIVREE',8900.00,1691.00,10591.00,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14198,N'CV-FT-HIST-14198','2025-10-06','2025-10-13',N'LIVREE',4240.00,805.60,5045.60,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14199,N'CV-FT-HIST-14199','2025-10-07','2025-10-14',N'LIVREE',5920.00,1124.80,7044.80,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14200,N'CV-FT-HIST-14200','2025-10-08','2025-10-15',N'LIVREE',1700.00,323.00,2023.00,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14201,N'CV-FT-HIST-14201','2025-10-09','2025-10-16',N'LIVREE',6050.00,1149.50,7199.50,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14202,N'CV-FT-HIST-14202','2025-10-11','2025-10-18',N'LIVREE',4550.00,864.50,5414.50,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14203,N'CV-FT-HIST-14203','2025-10-12','2025-10-19',N'LIVREE',5250.00,997.50,6247.50,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14204,N'CV-FT-HIST-14204','2025-10-13','2025-10-20',N'LIVREE',5650.00,1073.50,6723.50,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14205,N'CV-FT-HIST-14205','2025-10-14','2025-10-21',N'LIVREE',8900.00,1691.00,10591.00,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14206,N'CV-FT-HIST-14206','2025-10-16','2025-10-23',N'LIVREE',4240.00,805.60,5045.60,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14207,N'CV-FT-HIST-14207','2025-10-17','2025-10-24',N'LIVREE',5920.00,1124.80,7044.80,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14208,N'CV-FT-HIST-14208','2025-10-18','2025-10-25',N'LIVREE',1700.00,323.00,2023.00,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14209,N'CV-FT-HIST-14209','2025-10-19','2025-10-26',N'LIVREE',6050.00,1149.50,7199.50,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14210,N'CV-FT-HIST-14210','2025-10-21','2025-10-28',N'LIVREE',4550.00,864.50,5414.50,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14211,N'CV-FT-HIST-14211','2025-10-22','2025-10-29',N'LIVREE',5250.00,997.50,6247.50,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14212,N'CV-FT-HIST-14212','2025-10-23','2025-10-30',N'LIVREE',5650.00,1073.50,6723.50,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14213,N'CV-FT-HIST-14213','2025-10-24','2025-10-31',N'LIVREE',8900.00,1691.00,10591.00,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14214,N'CV-FT-HIST-14214','2025-10-26','2025-11-02',N'LIVREE',4240.00,805.60,5045.60,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14215,N'CV-FT-HIST-14215','2025-10-27','2025-11-03',N'LIVREE',5920.00,1124.80,7044.80,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14216,N'CV-FT-HIST-14216','2025-10-28','2025-11-04',N'LIVREE',1700.00,323.00,2023.00,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14217,N'CV-FT-HIST-14217','2025-10-29','2025-11-05',N'LIVREE',6050.00,1149.50,7199.50,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14218,N'CV-FT-HIST-14218','2025-10-31','2025-11-07',N'LIVREE',4550.00,864.50,5414.50,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14219,N'CV-FT-HIST-14219','2025-11-01','2025-11-08',N'LIVREE',5250.00,997.50,6247.50,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14220,N'CV-FT-HIST-14220','2025-11-02','2025-11-09',N'LIVREE',5650.00,1073.50,6723.50,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14221,N'CV-FT-HIST-14221','2025-11-03','2025-11-10',N'LIVREE',8900.00,1691.00,10591.00,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14222,N'CV-FT-HIST-14222','2025-11-04','2025-11-11',N'LIVREE',4240.00,805.60,5045.60,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14223,N'CV-FT-HIST-14223','2025-11-05','2025-11-12',N'LIVREE',5920.00,1124.80,7044.80,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14224,N'CV-FT-HIST-14224','2025-11-06','2025-11-13',N'LIVREE',1700.00,323.00,2023.00,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14225,N'CV-FT-HIST-14225','2025-11-07','2025-11-14',N'LIVREE',6050.00,1149.50,7199.50,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14226,N'CV-FT-HIST-14226','2025-11-08','2025-11-15',N'LIVREE',4550.00,864.50,5414.50,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14227,N'CV-FT-HIST-14227','2025-11-09','2025-11-16',N'LIVREE',5250.00,997.50,6247.50,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14228,N'CV-FT-HIST-14228','2025-11-10','2025-11-17',N'LIVREE',5650.00,1073.50,6723.50,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14229,N'CV-FT-HIST-14229','2025-11-11','2025-11-18',N'LIVREE',8900.00,1691.00,10591.00,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14230,N'CV-FT-HIST-14230','2025-11-12','2025-11-19',N'LIVREE',4240.00,805.60,5045.60,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14231,N'CV-FT-HIST-14231','2025-11-13','2025-11-20',N'LIVREE',5920.00,1124.80,7044.80,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14232,N'CV-FT-HIST-14232','2025-11-14','2025-11-21',N'LIVREE',1700.00,323.00,2023.00,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14233,N'CV-FT-HIST-14233','2025-11-15','2025-11-22',N'LIVREE',6050.00,1149.50,7199.50,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14234,N'CV-FT-HIST-14234','2025-11-16','2025-11-23',N'LIVREE',4550.00,864.50,5414.50,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14235,N'CV-FT-HIST-14235','2025-11-17','2025-11-24',N'LIVREE',5250.00,997.50,6247.50,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14236,N'CV-FT-HIST-14236','2025-11-18','2025-11-25',N'LIVREE',5650.00,1073.50,6723.50,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14237,N'CV-FT-HIST-14237','2025-11-19','2025-11-26',N'LIVREE',8900.00,1691.00,10591.00,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14238,N'CV-FT-HIST-14238','2025-11-20','2025-11-27',N'LIVREE',4240.00,805.60,5045.60,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14239,N'CV-FT-HIST-14239','2025-11-21','2025-11-28',N'LIVREE',5920.00,1124.80,7044.80,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14240,N'CV-FT-HIST-14240','2025-11-22','2025-11-29',N'LIVREE',1700.00,323.00,2023.00,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14241,N'CV-FT-HIST-14241','2025-11-23','2025-11-30',N'LIVREE',6050.00,1149.50,7199.50,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14242,N'CV-FT-HIST-14242','2025-11-24','2025-12-01',N'LIVREE',4550.00,864.50,5414.50,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14243,N'CV-FT-HIST-14243','2025-11-25','2025-12-02',N'LIVREE',5250.00,997.50,6247.50,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14244,N'CV-FT-HIST-14244','2025-11-26','2025-12-03',N'LIVREE',5650.00,1073.50,6723.50,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14245,N'CV-FT-HIST-14245','2025-11-27','2025-12-04',N'LIVREE',8900.00,1691.00,10591.00,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14246,N'CV-FT-HIST-14246','2025-11-28','2025-12-05',N'LIVREE',4240.00,805.60,5045.60,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14247,N'CV-FT-HIST-14247','2025-11-29','2025-12-06',N'LIVREE',5920.00,1124.80,7044.80,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14248,N'CV-FT-HIST-14248','2025-11-30','2025-12-07',N'LIVREE',1700.00,323.00,2023.00,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14249,N'CV-FT-HIST-14249','2025-12-01','2025-12-08',N'LIVREE',6050.00,1149.50,7199.50,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14250,N'CV-FT-HIST-14250','2025-12-01','2025-12-08',N'LIVREE',4550.00,864.50,5414.50,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14251,N'CV-FT-HIST-14251','2025-12-02','2025-12-09',N'LIVREE',5250.00,997.50,6247.50,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14252,N'CV-FT-HIST-14252','2025-12-03','2025-12-10',N'LIVREE',5650.00,1073.50,6723.50,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14253,N'CV-FT-HIST-14253','2025-12-04','2025-12-11',N'LIVREE',8900.00,1691.00,10591.00,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14254,N'CV-FT-HIST-14254','2025-12-05','2025-12-12',N'LIVREE',4240.00,805.60,5045.60,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14255,N'CV-FT-HIST-14255','2025-12-06','2025-12-13',N'LIVREE',5920.00,1124.80,7044.80,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14256,N'CV-FT-HIST-14256','2025-12-07','2025-12-14',N'LIVREE',1700.00,323.00,2023.00,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14257,N'CV-FT-HIST-14257','2025-12-08','2025-12-15',N'LIVREE',6050.00,1149.50,7199.50,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14258,N'CV-FT-HIST-14258','2025-12-09','2025-12-16',N'LIVREE',4550.00,864.50,5414.50,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14259,N'CV-FT-HIST-14259','2025-12-10','2025-12-17',N'LIVREE',5250.00,997.50,6247.50,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14260,N'CV-FT-HIST-14260','2025-12-11','2025-12-18',N'LIVREE',5650.00,1073.50,6723.50,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14261,N'CV-FT-HIST-14261','2025-12-11','2025-12-18',N'LIVREE',8900.00,1691.00,10591.00,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14262,N'CV-FT-HIST-14262','2025-12-12','2025-12-19',N'LIVREE',4240.00,805.60,5045.60,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14263,N'CV-FT-HIST-14263','2025-12-13','2025-12-20',N'LIVREE',5920.00,1124.80,7044.80,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14264,N'CV-FT-HIST-14264','2025-12-14','2025-12-21',N'LIVREE',1700.00,323.00,2023.00,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14265,N'CV-FT-HIST-14265','2025-12-15','2025-12-22',N'LIVREE',6050.00,1149.50,7199.50,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14266,N'CV-FT-HIST-14266','2025-12-16','2025-12-23',N'LIVREE',4550.00,864.50,5414.50,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14267,N'CV-FT-HIST-14267','2025-12-17','2025-12-24',N'LIVREE',5250.00,997.50,6247.50,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14268,N'CV-FT-HIST-14268','2025-12-18','2025-12-25',N'LIVREE',5650.00,1073.50,6723.50,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14269,N'CV-FT-HIST-14269','2025-12-19','2025-12-26',N'LIVREE',8900.00,1691.00,10591.00,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14270,N'CV-FT-HIST-14270','2025-12-20','2025-12-27',N'LIVREE',4240.00,805.60,5045.60,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14271,N'CV-FT-HIST-14271','2025-12-21','2025-12-28',N'LIVREE',5920.00,1124.80,7044.80,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14272,N'CV-FT-HIST-14272','2025-12-21','2025-12-28',N'LIVREE',1700.00,323.00,2023.00,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14273,N'CV-FT-HIST-14273','2025-12-22','2025-12-29',N'LIVREE',6050.00,1149.50,7199.50,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14274,N'CV-FT-HIST-14274','2025-12-23','2025-12-30',N'LIVREE',4550.00,864.50,5414.50,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14275,N'CV-FT-HIST-14275','2025-12-24','2025-12-31',N'LIVREE',5250.00,997.50,6247.50,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14276,N'CV-FT-HIST-14276','2025-12-25','2026-01-01',N'LIVREE',5650.00,1073.50,6723.50,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14277,N'CV-FT-HIST-14277','2025-12-26','2026-01-02',N'LIVREE',8900.00,1691.00,10591.00,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14278,N'CV-FT-HIST-14278','2025-12-27','2026-01-03',N'LIVREE',4240.00,805.60,5045.60,143,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14279,N'CV-FT-HIST-14279','2025-12-28','2026-01-04',N'LIVREE',5920.00,1124.80,7044.80,144,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14280,N'CV-FT-HIST-14280','2025-12-29','2026-01-05',N'LIVREE',1700.00,323.00,2023.00,141,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14281,N'CV-FT-HIST-14281','2025-12-30','2026-01-06',N'LIVREE',6050.00,1149.50,7199.50,142,14,GETDATE(),GETDATE())
INSERT [dbo].[commande_vente] ([id],[reference],[date_commande],[date_livraison_prevue],[statut],[montant_ht],[montant_tva],[montant_ttc],[client_id],[entreprise_id],[created_at],[updated_at]) VALUES (14282,N'CV-FT-HIST-14282','2025-12-31','2026-01-07',N'LIVREE',4550.00,864.50,5414.50,143,14,GETDATE(),GETDATE())
GO
SET IDENTITY_INSERT [dbo].[commande_vente] OFF
GO

-- ------------------------------------------------------------
-- 3. LIGNE_COMMANDE_VENTE for historical sales
-- ------------------------------------------------------------
INSERT [dbo].[ligne_commande_vente] ([commande_vente_id],[article_id],[quantite],[prix_unitaire_ht],[taux_tva],[montant_ht],[montant_ttc],[created_at],[updated_at]) VALUES
(14101,1401,3.000,2400.00,19.00,7200.00,8568.00,GETDATE(),GETDATE()),
(14101,1402,2.000,850.00,19.00,1700.00,2023.00,GETDATE(),GETDATE()),
(14102,1402,4.000,850.00,19.00,3400.00,4046.00,GETDATE(),GETDATE()),
(14102,1403,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
(14103,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14103,1403,4.000,280.00,19.00,1120.00,1332.80,GETDATE(),GETDATE()),
(14104,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14104,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14105,1402,3.000,850.00,19.00,2550.00,3034.50,GETDATE(),GETDATE()),
(14105,1405,10.000,350.00,19.00,3500.00,4165.00,GETDATE(),GETDATE()),
(14106,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14106,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14107,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14107,1404,3.000,150.00,19.00,450.00,535.50,GETDATE(),GETDATE()),
(14108,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14108,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14109,1401,3.000,2400.00,19.00,7200.00,8568.00,GETDATE(),GETDATE()),
(14109,1402,2.000,850.00,19.00,1700.00,2023.00,GETDATE(),GETDATE()),
(14110,1402,4.000,850.00,19.00,3400.00,4046.00,GETDATE(),GETDATE()),
(14110,1403,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
(14111,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14111,1403,4.000,280.00,19.00,1120.00,1332.80,GETDATE(),GETDATE()),
(14112,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14112,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14113,1402,3.000,850.00,19.00,2550.00,3034.50,GETDATE(),GETDATE()),
(14113,1405,10.000,350.00,19.00,3500.00,4165.00,GETDATE(),GETDATE()),
(14114,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14114,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14115,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14115,1404,3.000,150.00,19.00,450.00,535.50,GETDATE(),GETDATE()),
(14116,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14116,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14117,1401,3.000,2400.00,19.00,7200.00,8568.00,GETDATE(),GETDATE()),
(14117,1402,2.000,850.00,19.00,1700.00,2023.00,GETDATE(),GETDATE()),
(14118,1402,4.000,850.00,19.00,3400.00,4046.00,GETDATE(),GETDATE()),
(14118,1403,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
(14119,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14119,1403,4.000,280.00,19.00,1120.00,1332.80,GETDATE(),GETDATE()),
(14120,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14120,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14121,1402,3.000,850.00,19.00,2550.00,3034.50,GETDATE(),GETDATE()),
(14121,1405,10.000,350.00,19.00,3500.00,4165.00,GETDATE(),GETDATE()),
(14122,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14122,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14123,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14123,1404,3.000,150.00,19.00,450.00,535.50,GETDATE(),GETDATE()),
(14124,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14124,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14125,1401,3.000,2400.00,19.00,7200.00,8568.00,GETDATE(),GETDATE()),
(14125,1402,2.000,850.00,19.00,1700.00,2023.00,GETDATE(),GETDATE()),
(14126,1402,4.000,850.00,19.00,3400.00,4046.00,GETDATE(),GETDATE()),
(14126,1403,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
(14127,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14127,1403,4.000,280.00,19.00,1120.00,1332.80,GETDATE(),GETDATE()),
(14128,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14128,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14129,1402,3.000,850.00,19.00,2550.00,3034.50,GETDATE(),GETDATE()),
(14129,1405,10.000,350.00,19.00,3500.00,4165.00,GETDATE(),GETDATE()),
(14130,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14130,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14131,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14131,1404,3.000,150.00,19.00,450.00,535.50,GETDATE(),GETDATE()),
(14132,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14132,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14133,1401,3.000,2400.00,19.00,7200.00,8568.00,GETDATE(),GETDATE()),
(14133,1402,2.000,850.00,19.00,1700.00,2023.00,GETDATE(),GETDATE()),
(14134,1402,4.000,850.00,19.00,3400.00,4046.00,GETDATE(),GETDATE()),
(14134,1403,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
(14135,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14135,1403,4.000,280.00,19.00,1120.00,1332.80,GETDATE(),GETDATE()),
(14136,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14136,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14137,1402,3.000,850.00,19.00,2550.00,3034.50,GETDATE(),GETDATE()),
(14137,1405,10.000,350.00,19.00,3500.00,4165.00,GETDATE(),GETDATE()),
(14138,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14138,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14139,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14139,1404,3.000,150.00,19.00,450.00,535.50,GETDATE(),GETDATE()),
(14140,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14140,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14141,1401,3.000,2400.00,19.00,7200.00,8568.00,GETDATE(),GETDATE()),
(14141,1402,2.000,850.00,19.00,1700.00,2023.00,GETDATE(),GETDATE()),
(14142,1402,4.000,850.00,19.00,3400.00,4046.00,GETDATE(),GETDATE()),
(14142,1403,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
(14143,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14143,1403,4.000,280.00,19.00,1120.00,1332.80,GETDATE(),GETDATE()),
(14144,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14144,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14145,1402,3.000,850.00,19.00,2550.00,3034.50,GETDATE(),GETDATE()),
(14145,1405,10.000,350.00,19.00,3500.00,4165.00,GETDATE(),GETDATE()),
(14146,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14146,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14147,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14147,1404,3.000,150.00,19.00,450.00,535.50,GETDATE(),GETDATE()),
(14148,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14148,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14149,1401,3.000,2400.00,19.00,7200.00,8568.00,GETDATE(),GETDATE()),
(14149,1402,2.000,850.00,19.00,1700.00,2023.00,GETDATE(),GETDATE()),
(14150,1402,4.000,850.00,19.00,3400.00,4046.00,GETDATE(),GETDATE()),
(14150,1403,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
(14151,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14151,1403,4.000,280.00,19.00,1120.00,1332.80,GETDATE(),GETDATE()),
(14152,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14152,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14153,1402,3.000,850.00,19.00,2550.00,3034.50,GETDATE(),GETDATE()),
(14153,1405,10.000,350.00,19.00,3500.00,4165.00,GETDATE(),GETDATE()),
(14154,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14154,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14155,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14155,1404,3.000,150.00,19.00,450.00,535.50,GETDATE(),GETDATE()),
(14156,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14156,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14157,1401,3.000,2400.00,19.00,7200.00,8568.00,GETDATE(),GETDATE()),
(14157,1402,2.000,850.00,19.00,1700.00,2023.00,GETDATE(),GETDATE()),
(14158,1402,4.000,850.00,19.00,3400.00,4046.00,GETDATE(),GETDATE()),
(14158,1403,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
(14159,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14159,1403,4.000,280.00,19.00,1120.00,1332.80,GETDATE(),GETDATE()),
(14160,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14160,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14161,1402,3.000,850.00,19.00,2550.00,3034.50,GETDATE(),GETDATE()),
(14161,1405,10.000,350.00,19.00,3500.00,4165.00,GETDATE(),GETDATE()),
(14162,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14162,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14163,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14163,1404,3.000,150.00,19.00,450.00,535.50,GETDATE(),GETDATE()),
(14164,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14164,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14165,1401,3.000,2400.00,19.00,7200.00,8568.00,GETDATE(),GETDATE()),
(14165,1402,2.000,850.00,19.00,1700.00,2023.00,GETDATE(),GETDATE()),
(14166,1402,4.000,850.00,19.00,3400.00,4046.00,GETDATE(),GETDATE()),
(14166,1403,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
(14167,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14167,1403,4.000,280.00,19.00,1120.00,1332.80,GETDATE(),GETDATE()),
(14168,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14168,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14169,1402,3.000,850.00,19.00,2550.00,3034.50,GETDATE(),GETDATE()),
(14169,1405,10.000,350.00,19.00,3500.00,4165.00,GETDATE(),GETDATE()),
(14170,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14170,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14171,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14171,1404,3.000,150.00,19.00,450.00,535.50,GETDATE(),GETDATE()),
(14172,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14172,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14173,1401,3.000,2400.00,19.00,7200.00,8568.00,GETDATE(),GETDATE()),
(14173,1402,2.000,850.00,19.00,1700.00,2023.00,GETDATE(),GETDATE()),
(14174,1402,4.000,850.00,19.00,3400.00,4046.00,GETDATE(),GETDATE()),
(14174,1403,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
(14175,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14175,1403,4.000,280.00,19.00,1120.00,1332.80,GETDATE(),GETDATE()),
(14176,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14176,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14177,1402,3.000,850.00,19.00,2550.00,3034.50,GETDATE(),GETDATE()),
(14177,1405,10.000,350.00,19.00,3500.00,4165.00,GETDATE(),GETDATE()),
(14178,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14178,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14179,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14179,1404,3.000,150.00,19.00,450.00,535.50,GETDATE(),GETDATE()),
(14180,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14180,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14181,1401,3.000,2400.00,19.00,7200.00,8568.00,GETDATE(),GETDATE()),
(14181,1402,2.000,850.00,19.00,1700.00,2023.00,GETDATE(),GETDATE()),
(14182,1402,4.000,850.00,19.00,3400.00,4046.00,GETDATE(),GETDATE()),
(14182,1403,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
(14183,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14183,1403,4.000,280.00,19.00,1120.00,1332.80,GETDATE(),GETDATE()),
(14184,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14184,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14185,1402,3.000,850.00,19.00,2550.00,3034.50,GETDATE(),GETDATE()),
(14185,1405,10.000,350.00,19.00,3500.00,4165.00,GETDATE(),GETDATE()),
(14186,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14186,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14187,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14187,1404,3.000,150.00,19.00,450.00,535.50,GETDATE(),GETDATE()),
(14188,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14188,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14189,1401,3.000,2400.00,19.00,7200.00,8568.00,GETDATE(),GETDATE()),
(14189,1402,2.000,850.00,19.00,1700.00,2023.00,GETDATE(),GETDATE()),
(14190,1402,4.000,850.00,19.00,3400.00,4046.00,GETDATE(),GETDATE()),
(14190,1403,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
(14191,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14191,1403,4.000,280.00,19.00,1120.00,1332.80,GETDATE(),GETDATE()),
(14192,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14192,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14193,1402,3.000,850.00,19.00,2550.00,3034.50,GETDATE(),GETDATE()),
(14193,1405,10.000,350.00,19.00,3500.00,4165.00,GETDATE(),GETDATE()),
(14194,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14194,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14195,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14195,1404,3.000,150.00,19.00,450.00,535.50,GETDATE(),GETDATE()),
(14196,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14196,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14197,1401,3.000,2400.00,19.00,7200.00,8568.00,GETDATE(),GETDATE()),
(14197,1402,2.000,850.00,19.00,1700.00,2023.00,GETDATE(),GETDATE()),
(14198,1402,4.000,850.00,19.00,3400.00,4046.00,GETDATE(),GETDATE()),
(14198,1403,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
(14199,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14199,1403,4.000,280.00,19.00,1120.00,1332.80,GETDATE(),GETDATE()),
(14200,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14200,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE())
GO

INSERT [dbo].[ligne_commande_vente] ([commande_vente_id],[article_id],[quantite],[prix_unitaire_ht],[taux_tva],[montant_ht],[montant_ttc],[created_at],[updated_at]) VALUES
(14201,1402,3.000,850.00,19.00,2550.00,3034.50,GETDATE(),GETDATE()),
(14201,1405,10.000,350.00,19.00,3500.00,4165.00,GETDATE(),GETDATE()),
(14202,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14202,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14203,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14203,1404,3.000,150.00,19.00,450.00,535.50,GETDATE(),GETDATE()),
(14204,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14204,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14205,1401,3.000,2400.00,19.00,7200.00,8568.00,GETDATE(),GETDATE()),
(14205,1402,2.000,850.00,19.00,1700.00,2023.00,GETDATE(),GETDATE()),
(14206,1402,4.000,850.00,19.00,3400.00,4046.00,GETDATE(),GETDATE()),
(14206,1403,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
(14207,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14207,1403,4.000,280.00,19.00,1120.00,1332.80,GETDATE(),GETDATE()),
(14208,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14208,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14209,1402,3.000,850.00,19.00,2550.00,3034.50,GETDATE(),GETDATE()),
(14209,1405,10.000,350.00,19.00,3500.00,4165.00,GETDATE(),GETDATE()),
(14210,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14210,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14211,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14211,1404,3.000,150.00,19.00,450.00,535.50,GETDATE(),GETDATE()),
(14212,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14212,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14213,1401,3.000,2400.00,19.00,7200.00,8568.00,GETDATE(),GETDATE()),
(14213,1402,2.000,850.00,19.00,1700.00,2023.00,GETDATE(),GETDATE()),
(14214,1402,4.000,850.00,19.00,3400.00,4046.00,GETDATE(),GETDATE()),
(14214,1403,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
(14215,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14215,1403,4.000,280.00,19.00,1120.00,1332.80,GETDATE(),GETDATE()),
(14216,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14216,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14217,1402,3.000,850.00,19.00,2550.00,3034.50,GETDATE(),GETDATE()),
(14217,1405,10.000,350.00,19.00,3500.00,4165.00,GETDATE(),GETDATE()),
(14218,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14218,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14219,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14219,1404,3.000,150.00,19.00,450.00,535.50,GETDATE(),GETDATE()),
(14220,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14220,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14221,1401,3.000,2400.00,19.00,7200.00,8568.00,GETDATE(),GETDATE()),
(14221,1402,2.000,850.00,19.00,1700.00,2023.00,GETDATE(),GETDATE()),
(14222,1402,4.000,850.00,19.00,3400.00,4046.00,GETDATE(),GETDATE()),
(14222,1403,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
(14223,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14223,1403,4.000,280.00,19.00,1120.00,1332.80,GETDATE(),GETDATE()),
(14224,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14224,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14225,1402,3.000,850.00,19.00,2550.00,3034.50,GETDATE(),GETDATE()),
(14225,1405,10.000,350.00,19.00,3500.00,4165.00,GETDATE(),GETDATE()),
(14226,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14226,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14227,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14227,1404,3.000,150.00,19.00,450.00,535.50,GETDATE(),GETDATE()),
(14228,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14228,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14229,1401,3.000,2400.00,19.00,7200.00,8568.00,GETDATE(),GETDATE()),
(14229,1402,2.000,850.00,19.00,1700.00,2023.00,GETDATE(),GETDATE()),
(14230,1402,4.000,850.00,19.00,3400.00,4046.00,GETDATE(),GETDATE()),
(14230,1403,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
(14231,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14231,1403,4.000,280.00,19.00,1120.00,1332.80,GETDATE(),GETDATE()),
(14232,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14232,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14233,1402,3.000,850.00,19.00,2550.00,3034.50,GETDATE(),GETDATE()),
(14233,1405,10.000,350.00,19.00,3500.00,4165.00,GETDATE(),GETDATE()),
(14234,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14234,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14235,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14235,1404,3.000,150.00,19.00,450.00,535.50,GETDATE(),GETDATE()),
(14236,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14236,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14237,1401,3.000,2400.00,19.00,7200.00,8568.00,GETDATE(),GETDATE()),
(14237,1402,2.000,850.00,19.00,1700.00,2023.00,GETDATE(),GETDATE()),
(14238,1402,4.000,850.00,19.00,3400.00,4046.00,GETDATE(),GETDATE()),
(14238,1403,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
(14239,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14239,1403,4.000,280.00,19.00,1120.00,1332.80,GETDATE(),GETDATE()),
(14240,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14240,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14241,1402,3.000,850.00,19.00,2550.00,3034.50,GETDATE(),GETDATE()),
(14241,1405,10.000,350.00,19.00,3500.00,4165.00,GETDATE(),GETDATE()),
(14242,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14242,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14243,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14243,1404,3.000,150.00,19.00,450.00,535.50,GETDATE(),GETDATE()),
(14244,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14244,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14245,1401,3.000,2400.00,19.00,7200.00,8568.00,GETDATE(),GETDATE()),
(14245,1402,2.000,850.00,19.00,1700.00,2023.00,GETDATE(),GETDATE()),
(14246,1402,4.000,850.00,19.00,3400.00,4046.00,GETDATE(),GETDATE()),
(14246,1403,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
(14247,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14247,1403,4.000,280.00,19.00,1120.00,1332.80,GETDATE(),GETDATE()),
(14248,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14248,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14249,1402,3.000,850.00,19.00,2550.00,3034.50,GETDATE(),GETDATE()),
(14249,1405,10.000,350.00,19.00,3500.00,4165.00,GETDATE(),GETDATE()),
(14250,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14250,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14251,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14251,1404,3.000,150.00,19.00,450.00,535.50,GETDATE(),GETDATE()),
(14252,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14252,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14253,1401,3.000,2400.00,19.00,7200.00,8568.00,GETDATE(),GETDATE()),
(14253,1402,2.000,850.00,19.00,1700.00,2023.00,GETDATE(),GETDATE()),
(14254,1402,4.000,850.00,19.00,3400.00,4046.00,GETDATE(),GETDATE()),
(14254,1403,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
(14255,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14255,1403,4.000,280.00,19.00,1120.00,1332.80,GETDATE(),GETDATE()),
(14256,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14256,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14257,1402,3.000,850.00,19.00,2550.00,3034.50,GETDATE(),GETDATE()),
(14257,1405,10.000,350.00,19.00,3500.00,4165.00,GETDATE(),GETDATE()),
(14258,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14258,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14259,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14259,1404,3.000,150.00,19.00,450.00,535.50,GETDATE(),GETDATE()),
(14260,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14260,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14261,1401,3.000,2400.00,19.00,7200.00,8568.00,GETDATE(),GETDATE()),
(14261,1402,2.000,850.00,19.00,1700.00,2023.00,GETDATE(),GETDATE()),
(14262,1402,4.000,850.00,19.00,3400.00,4046.00,GETDATE(),GETDATE()),
(14262,1403,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
(14263,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14263,1403,4.000,280.00,19.00,1120.00,1332.80,GETDATE(),GETDATE()),
(14264,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14264,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14265,1402,3.000,850.00,19.00,2550.00,3034.50,GETDATE(),GETDATE()),
(14265,1405,10.000,350.00,19.00,3500.00,4165.00,GETDATE(),GETDATE()),
(14266,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14266,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14267,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14267,1404,3.000,150.00,19.00,450.00,535.50,GETDATE(),GETDATE()),
(14268,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14268,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14269,1401,3.000,2400.00,19.00,7200.00,8568.00,GETDATE(),GETDATE()),
(14269,1402,2.000,850.00,19.00,1700.00,2023.00,GETDATE(),GETDATE()),
(14270,1402,4.000,850.00,19.00,3400.00,4046.00,GETDATE(),GETDATE()),
(14270,1403,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
(14271,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14271,1403,4.000,280.00,19.00,1120.00,1332.80,GETDATE(),GETDATE()),
(14272,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14272,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14273,1402,3.000,850.00,19.00,2550.00,3034.50,GETDATE(),GETDATE()),
(14273,1405,10.000,350.00,19.00,3500.00,4165.00,GETDATE(),GETDATE()),
(14274,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14274,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14275,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14275,1404,3.000,150.00,19.00,450.00,535.50,GETDATE(),GETDATE()),
(14276,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE()),
(14276,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14277,1401,3.000,2400.00,19.00,7200.00,8568.00,GETDATE(),GETDATE()),
(14277,1402,2.000,850.00,19.00,1700.00,2023.00,GETDATE(),GETDATE()),
(14278,1402,4.000,850.00,19.00,3400.00,4046.00,GETDATE(),GETDATE()),
(14278,1403,3.000,280.00,19.00,840.00,999.60,GETDATE(),GETDATE()),
(14279,1401,2.000,2400.00,19.00,4800.00,5712.00,GETDATE(),GETDATE()),
(14279,1403,4.000,280.00,19.00,1120.00,1332.80,GETDATE(),GETDATE()),
(14280,1403,5.000,280.00,19.00,1400.00,1666.00,GETDATE(),GETDATE()),
(14280,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14281,1402,3.000,850.00,19.00,2550.00,3034.50,GETDATE(),GETDATE()),
(14281,1405,10.000,350.00,19.00,3500.00,4165.00,GETDATE(),GETDATE()),
(14282,1404,2.000,150.00,19.00,300.00,357.00,GETDATE(),GETDATE()),
(14282,1402,5.000,850.00,19.00,4250.00,5057.50,GETDATE(),GETDATE())
GO

-- ============================================================
-- 4. AI PREDICTION LOG – realistic 12-month back-tested history
--
-- Issuance cadence : 1 prediction per month, horizons 1/2/3 months.
-- Settled rows fill actual_value (= SUM(montant_ttc) for the target month
-- in the seeded data) and derived error metrics. Pending rows leave
-- actuals NULL so the "Réconcilier" button shows real behaviour.
-- ============================================================

INSERT [dbo].[prediction_log] ([entreprise_id],[prediction_date],[prediction_month],[target_month],[horizon_months],[predicted_value],[lower_bound],[upper_bound],[confidence_score],[methodology],[model_name],[actual_value],[absolute_error],[percent_error],[within_interval],[settled_at],[created_at],[updated_at]) VALUES
(14,'2025-05-02','2025-05-01','2025-06-01',1,121005.50,111930.09,130080.91,0.91,N'OLS regression sur 18 mois d''historique CA (lag-1, tendance, saison sin/cos), intervalles à 90% via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',113549.80,7455.70,0.0657,1,'2025-06-07 00:00:00',GETDATE(),GETDATE()),
(14,'2025-05-02','2025-05-01','2025-07-01',2,69968.22,62621.56,77314.89,0.84,N'OLS regression sur 18 mois d''historique CA (lag-1, tendance, saison sin/cos), intervalles à 90% via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',74993.80,5025.58,0.0670,1,'2025-07-09 00:00:00',GETDATE(),GETDATE()),
(14,'2025-05-02','2025-05-01','2025-08-01',3,81474.96,69661.09,93288.82,0.74,N'OLS regression sur 18 mois d''historique CA (lag-1, tendance, saison sin/cos), intervalles à 90% via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',69150.90,12324.06,0.1782,0,'2025-08-10 00:00:00',GETDATE(),GETDATE()),
(14,'2025-06-02','2025-06-01','2025-07-01',1,70100.25,64842.73,75357.77,0.91,N'OLS regression sur 18 mois d''historique CA (lag-1, tendance, saison sin/cos), intervalles à 90% via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',74993.80,4893.55,0.0653,1,'2025-07-08 00:00:00',GETDATE(),GETDATE()),
(14,'2025-06-02','2025-06-01','2025-08-01',2,61923.46,55421.50,68425.42,0.84,N'OLS regression sur 18 mois d''historique CA (lag-1, tendance, saison sin/cos), intervalles à 90% via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',69150.90,7227.44,0.1045,0,'2025-08-09 00:00:00',GETDATE(),GETDATE()),
(14,'2025-06-02','2025-06-01','2025-09-01',3,153175.18,130964.78,175385.58,0.74,N'OLS regression sur 18 mois d''historique CA (lag-1, tendance, saison sin/cos), intervalles à 90% via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',139206.20,13968.98,0.1003,1,'2025-09-10 00:00:00',GETDATE(),GETDATE()),
(14,'2025-07-02','2025-07-01','2025-08-01',1,64483.89,59647.60,69320.19,0.91,N'OLS regression sur 18 mois d''historique CA (lag-1, tendance, saison sin/cos), intervalles à 90% via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',69150.90,4667.01,0.0675,1,'2025-08-07 00:00:00',GETDATE(),GETDATE()),
(14,'2025-07-02','2025-07-01','2025-09-01',2,149711.48,133991.78,165431.19,0.84,N'OLS regression sur 18 mois d''historique CA (lag-1, tendance, saison sin/cos), intervalles à 90% via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',139206.20,10505.28,0.0755,1,'2025-09-08 00:00:00',GETDATE(),GETDATE()),
(14,'2025-07-02','2025-07-01','2025-10-01',3,176406.94,150827.93,201985.94,0.74,N'OLS regression sur 18 mois d''historique CA (lag-1, tendance, saison sin/cos), intervalles à 90% via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',156282.70,20124.24,0.1288,1,'2025-10-10 00:00:00',GETDATE(),GETDATE()),
(14,'2025-08-02','2025-08-01','2025-09-01',1,132576.81,122633.55,142520.07,0.91,N'OLS regression sur 18 mois d''historique CA (lag-1, tendance, saison sin/cos), intervalles à 90% via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',139206.20,6629.39,0.0476,1,'2025-09-07 00:00:00',GETDATE(),GETDATE()),
(14,'2025-08-02','2025-08-01','2025-10-01',2,140389.33,125648.45,155130.21,0.84,N'OLS regression sur 18 mois d''historique CA (lag-1, tendance, saison sin/cos), intervalles à 90% via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',156282.70,15893.37,0.1017,0,'2025-10-09 00:00:00',GETDATE(),GETDATE()),
(14,'2025-08-02','2025-08-01','2025-11-01',3,170102.90,145437.98,194767.82,0.74,N'OLS regression sur 18 mois d''historique CA (lag-1, tendance, saison sin/cos), intervalles à 90% via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',188543.60,18440.70,0.0978,1,'2025-11-10 00:00:00',GETDATE(),GETDATE()),
(14,'2025-09-02','2025-09-01','2025-10-01',1,142301.83,131629.19,152974.46,0.91,N'OLS regression sur 18 mois d''historique CA (lag-1, tendance, saison sin/cos), intervalles à 90% via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',156282.70,13980.87,0.0895,0,'2025-10-08 00:00:00',GETDATE(),GETDATE()),
(14,'2025-09-02','2025-09-01','2025-11-01',2,174510.84,156187.20,192834.48,0.84,N'OLS regression sur 18 mois d''historique CA (lag-1, tendance, saison sin/cos), intervalles à 90% via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',188543.60,14032.76,0.0744,1,'2025-11-09 00:00:00',GETDATE(),GETDATE()),
(14,'2025-09-02','2025-09-01','2025-12-01',3,241939.78,206858.51,277021.05,0.74,N'OLS regression sur 18 mois d''historique CA (lag-1, tendance, saison sin/cos), intervalles à 90% via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',213771.60,28168.18,0.1318,1,'2025-12-11 00:00:00',GETDATE(),GETDATE()),
(14,'2025-10-02','2025-10-01','2025-11-01',1,203593.35,188323.85,218862.85,0.91,N'OLS regression sur 18 mois d''historique CA (lag-1, tendance, saison sin/cos), intervalles à 90% via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',188543.60,15049.75,0.0798,1,'2025-11-07 00:00:00',GETDATE(),GETDATE()),
(14,'2025-10-02','2025-10-01','2025-12-01',2,239635.73,214473.98,264797.48,0.84,N'OLS regression sur 18 mois d''historique CA (lag-1, tendance, saison sin/cos), intervalles à 90% via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',213771.60,25864.13,0.1210,0,'2025-12-09 00:00:00',GETDATE(),GETDATE()),
(14,'2025-10-02','2025-10-01','2026-01-01',3,211244.30,180613.87,241874.72,0.74,N'OLS regression sur 18 mois d''historique CA (lag-1, tendance, saison sin/cos), intervalles à 90% via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',188189.60,23054.70,0.1225,1,'2026-01-10 00:00:00',GETDATE(),GETDATE()),
(14,'2025-11-02','2025-11-01','2025-12-01',1,222287.41,205615.85,238958.97,0.91,N'OLS regression sur 18 mois d''historique CA (lag-1, tendance, saison sin/cos), intervalles à 90% via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',213771.60,8515.81,0.0398,1,'2025-12-08 00:00:00',GETDATE(),GETDATE()),
(14,'2025-11-02','2025-11-01','2026-01-01',2,206673.69,184972.95,228374.43,0.84,N'OLS regression sur 18 mois d''historique CA (lag-1, tendance, saison sin/cos), intervalles à 90% via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',188189.60,18484.09,0.0982,1,'2026-01-09 00:00:00',GETDATE(),GETDATE()),
(14,'2025-11-02','2025-11-01','2026-02-01',3,151711.25,129713.12,173709.39,0.74,N'OLS regression sur 18 mois d''historique CA (lag-1, tendance, saison sin/cos), intervalles à 90% via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',179479.80,27768.55,0.1547,0,'2026-02-10 00:00:00',GETDATE(),GETDATE()),
(14,'2025-12-02','2025-12-01','2026-01-01',1,203150.57,187914.28,218386.86,0.91,N'OLS regression sur 18 mois d''historique CA (lag-1, tendance, saison sin/cos), intervalles à 90% via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',188189.60,14960.97,0.0795,1,'2026-01-07 00:00:00',GETDATE(),GETDATE()),
(14,'2025-12-02','2025-12-01','2026-02-01',2,195151.64,174660.72,215642.57,0.84,N'OLS regression sur 18 mois d''historique CA (lag-1, tendance, saison sin/cos), intervalles à 90% via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',179479.80,15671.84,0.0873,1,'2026-02-08 00:00:00',GETDATE(),GETDATE()),
(14,'2025-12-02','2025-12-01','2026-03-01',3,98616.95,84317.49,112916.41,0.74,N'OLS regression sur 18 mois d''historique CA (lag-1, tendance, saison sin/cos), intervalles à 90% via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',107625.60,9008.65,0.0837,1,'2026-03-12 00:00:00',GETDATE(),GETDATE()),
(14,'2026-01-02','2026-01-01','2026-02-01',1,188558.94,174417.02,202700.87,0.91,N'OLS regression sur 18 mois d''historique CA (lag-1, tendance, saison sin/cos), intervalles à 90% via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',179479.80,9079.14,0.0506,1,'2026-02-07 00:00:00',GETDATE(),GETDATE()),
(14,'2026-01-02','2026-01-01','2026-03-01',2,115795.60,103637.06,127954.14,0.84,N'OLS regression sur 18 mois d''historique CA (lag-1, tendance, saison sin/cos), intervalles à 90% via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',107625.60,8170.00,0.0759,1,'2026-03-11 00:00:00',GETDATE(),GETDATE()),
(14,'2026-01-02','2026-01-01','2026-04-01',3,136273.49,116513.84,156033.15,0.74,N'OLS regression sur 18 mois d''historique CA (lag-1, tendance, saison sin/cos), intervalles à 90% via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',NULL,NULL,NULL,NULL,NULL,GETDATE(),GETDATE()),
(14,'2026-02-02','2026-02-01','2026-03-01',1,111724.21,103344.90,120103.53,0.91,N'OLS regression sur 18 mois d''historique CA (lag-1, tendance, saison sin/cos), intervalles à 90% via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',107625.60,4098.61,0.0381,1,'2026-03-10 00:00:00',GETDATE(),GETDATE()),
(14,'2026-02-02','2026-02-01','2026-04-01',2,136763.19,122403.05,151123.32,0.84,N'OLS regression sur 18 mois d''historique CA (lag-1, tendance, saison sin/cos), intervalles à 90% via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',NULL,NULL,NULL,NULL,NULL,GETDATE(),GETDATE()),
(14,'2026-02-02','2026-02-01','2026-05-01',3,127587.25,109087.10,146087.41,0.74,N'OLS regression sur 18 mois d''historique CA (lag-1, tendance, saison sin/cos), intervalles à 90% via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',NULL,NULL,NULL,NULL,NULL,GETDATE(),GETDATE()),
(14,'2026-03-02','2026-03-01','2026-04-01',1,137581.43,127262.82,147900.04,0.91,N'OLS regression sur 18 mois d''historique CA (lag-1, tendance, saison sin/cos), intervalles à 90% via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',NULL,NULL,NULL,NULL,NULL,GETDATE(),GETDATE()),
(14,'2026-03-02','2026-03-01','2026-05-01',2,120467.41,107818.34,133116.49,0.84,N'OLS regression sur 18 mois d''historique CA (lag-1, tendance, saison sin/cos), intervalles à 90% via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',NULL,NULL,NULL,NULL,NULL,GETDATE(),GETDATE()),
(14,'2026-03-02','2026-03-01','2026-06-01',3,132440.63,113236.73,151644.52,0.74,N'OLS regression sur 18 mois d''historique CA (lag-1, tendance, saison sin/cos), intervalles à 90% via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',NULL,NULL,NULL,NULL,NULL,GETDATE(),GETDATE()),
(14,'2026-04-02','2026-04-01','2026-05-01',1,122143.84,112983.05,131304.62,0.91,N'OLS regression sur 18 mois d''historique CA (lag-1, tendance, saison sin/cos), intervalles à 90% via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',NULL,NULL,NULL,NULL,NULL,GETDATE(),GETDATE()),
(14,'2026-04-02','2026-04-01','2026-06-01',2,132999.28,119034.36,146964.20,0.84,N'OLS regression sur 18 mois d''historique CA (lag-1, tendance, saison sin/cos), intervalles à 90% via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',NULL,NULL,NULL,NULL,NULL,GETDATE(),GETDATE()),
(14,'2026-04-02','2026-04-01','2026-07-01',3,100311.35,85766.21,114856.50,0.74,N'OLS regression sur 18 mois d''historique CA (lag-1, tendance, saison sin/cos), intervalles à 90% via résidus bootstrap. Modèle entraîné mensuellement.',N'ML-Forecast OLS+Seasonal v1.3',NULL,NULL,NULL,NULL,NULL,GETDATE(),GETDATE())
GO

-- ============================================================
-- 5. SUMMARY
-- ============================================================
PRINT N'FirstTechnology (id=14) AI seed loaded:'
PRINT N'  - 182 historical sales orders (Apr 2025 – Dec 2025)'
PRINT N'  - 364 ligne_commande_vente rows'
PRINT N'  - 36 prediction_log rows (27 settled, 9 pending)'
GO
