-- ============================================================
-- BUSINESSMANAGER ERP - Complete Database Schema
-- SQL Server 2019+ Compatible
-- Generated: 2026-03-23
-- Description: Full ERP schema with multi-tenant support.
--              Replaces old Team Management schema.
-- ============================================================

USE [master]
GO

-- ============================================================
-- 1. DATABASE CREATION
-- ============================================================
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'BusinessManager')
BEGIN
    CREATE DATABASE [BusinessManager]
     CONTAINMENT = NONE
     ON PRIMARY
    ( NAME = N'BusinessManager', FILENAME = N'C:\Program Files\Microsoft SQL Server\MSSQL16.MSSQLSERVER\MSSQL\DATA\BusinessManager.mdf', SIZE = 8192KB, MAXSIZE = UNLIMITED, FILEGROWTH = 65536KB )
     LOG ON
    ( NAME = N'BusinessManager_log', FILENAME = N'C:\Program Files\Microsoft SQL Server\MSSQL16.MSSQLSERVER\MSSQL\DATA\BusinessManager_log.ldf', SIZE = 73728KB, MAXSIZE = 2048GB, FILEGROWTH = 65536KB )
     WITH CATALOG_COLLATION = DATABASE_DEFAULT, LEDGER = OFF
END
GO

ALTER DATABASE [BusinessManager] SET COMPATIBILITY_LEVEL = 160
GO
ALTER DATABASE [BusinessManager] SET ANSI_NULL_DEFAULT OFF
GO
ALTER DATABASE [BusinessManager] SET ANSI_NULLS OFF
GO
ALTER DATABASE [BusinessManager] SET ANSI_PADDING OFF
GO
ALTER DATABASE [BusinessManager] SET ANSI_WARNINGS OFF
GO
ALTER DATABASE [BusinessManager] SET ARITHABORT OFF
GO
ALTER DATABASE [BusinessManager] SET AUTO_CLOSE OFF
GO
ALTER DATABASE [BusinessManager] SET AUTO_SHRINK OFF
GO
ALTER DATABASE [BusinessManager] SET AUTO_UPDATE_STATISTICS ON
GO
ALTER DATABASE [BusinessManager] SET QUOTED_IDENTIFIER OFF
GO
ALTER DATABASE [BusinessManager] SET RECURSIVE_TRIGGERS OFF
GO
ALTER DATABASE [BusinessManager] SET DISABLE_BROKER
GO
ALTER DATABASE [BusinessManager] SET AUTO_UPDATE_STATISTICS_ASYNC OFF
GO
ALTER DATABASE [BusinessManager] SET RECOVERY FULL
GO
ALTER DATABASE [BusinessManager] SET MULTI_USER
GO
ALTER DATABASE [BusinessManager] SET PAGE_VERIFY CHECKSUM
GO
ALTER DATABASE [BusinessManager] SET DB_CHAINING OFF
GO
ALTER DATABASE [BusinessManager] SET TARGET_RECOVERY_TIME = 60 SECONDS
GO
ALTER DATABASE [BusinessManager] SET DELAYED_DURABILITY = DISABLED
GO

USE [BusinessManager]
GO

-- ============================================================
-- 2. AUTH DOMAIN
--    Tables: app_user, roles, user_roles,
--            module_tittle, module, sous_module, permission
-- ============================================================

-- entreprise must be created before app_user (FK)
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[entreprise] (
    [id]                              BIGINT IDENTITY(1,1) NOT NULL,
    [nom]                             VARCHAR(255) NULL,
    [raison_sociale]                  VARCHAR(255) NULL,
    [adresse]                         VARCHAR(255) NULL,
    [telephone]                       VARCHAR(255) NULL,
    [email]                           VARCHAR(255) NULL,
    [site_web]                        VARCHAR(255) NULL,
    [secteur_activite]                VARCHAR(255) NULL,
    [description]                     VARCHAR(255) NULL,
    [numero_identification_fiscale]   VARCHAR(255) NULL,
    [registre_commerce]               VARCHAR(255) NULL,
    [statut_juridique]                VARCHAR(255) NULL,
    [date_creation]                   DATE NULL,
    [date_derniere_modification]      DATETIME2(6) NULL,
    [created_at]                      DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    [updated_at]                      DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    CONSTRAINT [PK_entreprise] PRIMARY KEY CLUSTERED ([id] ASC)
      WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,
            ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[app_user] (
    [id]            BIGINT IDENTITY(1,1) NOT NULL,
    [email]         VARCHAR(255) NULL,
    [username]      VARCHAR(255) NOT NULL,
    [password]      VARCHAR(255) NULL,
    [enabled]       BIT NOT NULL DEFAULT 1,
    [image_url]     VARCHAR(255) NULL,
    [phone]         VARCHAR(255) NULL,
    [removed_at]    DATETIME2(6) NULL,
    [entreprise_id] BIGINT NULL,
    [created_at]    DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    [updated_at]    DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    CONSTRAINT [PK_app_user] PRIMARY KEY CLUSTERED ([id] ASC)
      WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,
            ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[roles] (
    [id]         BIGINT IDENTITY(1,1) NOT NULL,
    [name]       VARCHAR(255) NULL,
    [created_on] DATE NULL,
    [created_at] DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    [updated_at] DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    CONSTRAINT [PK_roles] PRIMARY KEY CLUSTERED ([id] ASC)
      WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,
            ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[user_roles] (
    [user_id] BIGINT NOT NULL,
    [role_id] BIGINT NOT NULL
) ON [PRIMARY]
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[module_tittle] (
    [id]            BIGINT NOT NULL,
    [module_tittle] VARCHAR(255) NULL,
    [created_at]    DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    [updated_at]    DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    CONSTRAINT [PK_module_tittle] PRIMARY KEY CLUSTERED ([id] ASC)
      WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,
            ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[module] (
    [id]               BIGINT NOT NULL,
    [title]            VARCHAR(255) NULL,
    [status]           BIT NOT NULL DEFAULT 1,
    [module_tittle_id] BIGINT NULL,
    [created_at]       DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    [updated_at]       DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    CONSTRAINT [PK_module] PRIMARY KEY CLUSTERED ([id] ASC)
      WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,
            ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[sous_module] (
    [id]                BIGINT NOT NULL,
    [titre_sous_module] VARCHAR(255) NULL,
    [link]              VARCHAR(255) NULL,
    [module_id]         BIGINT NULL,
    [created_at]        DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    [updated_at]        DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    CONSTRAINT [PK_sous_module] PRIMARY KEY CLUSTERED ([id] ASC)
      WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,
            ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[permission] (
    [id]         BIGINT NOT NULL,
    [can_create] BIT NULL,
    [can_delete] BIT NULL,
    [can_update] BIT NULL,
    [can_view]   BIT NULL,
    [module_id]  BIGINT NULL,
    [role_id]    BIGINT NULL,
    [created_at] DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    [updated_at] DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    CONSTRAINT [PK_permission] PRIMARY KEY CLUSTERED ([id] ASC)
      WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,
            ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

-- ============================================================
-- 3. COMPANY DOMAIN
--    Tables: departement, poste
-- ============================================================

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[departement] (
    [id]              BIGINT IDENTITY(1,1) NOT NULL,
    [nom]             VARCHAR(255) NULL,
    [description]     VARCHAR(255) NULL,
    [email]           VARCHAR(255) NULL,
    [telephone]       VARCHAR(255) NULL,
    [responsable]     VARCHAR(255) NULL,
    [nombre_employes] INT NULL,
    [entreprise_id]   BIGINT NULL,
    [created_at]      DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    [updated_at]      DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    CONSTRAINT [PK_departement] PRIMARY KEY CLUSTERED ([id] ASC)
      WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,
            ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[poste] (
    [id]              BIGINT IDENTITY(1,1) NOT NULL,
    [nom]             VARCHAR(255) NULL,
    [description]     VARCHAR(255) NULL,
    [entreprise_id]   BIGINT NULL,
    [departement_id]  BIGINT NULL,
    [created_at]      DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    [updated_at]      DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    CONSTRAINT [PK_poste] PRIMARY KEY CLUSTERED ([id] ASC)
      WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,
            ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

-- ============================================================
-- 4. CRM DOMAIN
--    Tables: categorie_client, client, prix_client
-- ============================================================

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[categorie_client] (
    [id]          BIGINT IDENTITY(1,1) NOT NULL,
    [nom]         VARCHAR(255) NULL,
    [description] VARCHAR(255) NULL,
    [created_at]  DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    [updated_at]  DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    CONSTRAINT [PK_categorie_client] PRIMARY KEY CLUSTERED ([id] ASC)
      WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,
            ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[client] (
    [id]                  BIGINT IDENTITY(1,1) NOT NULL,
    [nom]                 VARCHAR(255) NULL,
    [prenom]              VARCHAR(255) NULL,
    [email]               VARCHAR(255) NULL,
    [telephone]           VARCHAR(255) NULL,
    [adresse]             VARCHAR(255) NULL,
    [secteur_activite]    VARCHAR(255) NULL,
    [categorie_client_id] BIGINT NULL,
    [entreprise_id]       BIGINT NULL,
    [created_at]          DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    [updated_at]          DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    CONSTRAINT [PK_client] PRIMARY KEY CLUSTERED ([id] ASC)
      WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,
            ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

-- client_entreprises: legacy junction table kept for backward compatibility
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[client_entreprises] (
    [client_id]      BIGINT NOT NULL,
    [entreprises_id] BIGINT NOT NULL
) ON [PRIMARY]
GO

-- ============================================================
-- 5. SUPPLIER DOMAIN
--    Tables: categorie_fournisseur, fournisseur
-- ============================================================

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[categorie_fournisseur] (
    [id]          BIGINT IDENTITY(1,1) NOT NULL,
    [nom]         VARCHAR(255) NULL,
    [description] VARCHAR(255) NULL,
    [created_at]  DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    [updated_at]  DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    CONSTRAINT [PK_categorie_fournisseur] PRIMARY KEY CLUSTERED ([id] ASC)
      WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,
            ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[fournisseur] (
    [id]                            BIGINT IDENTITY(1,1) NOT NULL,
    [nom]                           VARCHAR(255) NULL,
    [prenom]                        VARCHAR(255) NULL,
    [email]                         VARCHAR(255) NULL,
    [telephone]                     VARCHAR(255) NULL,
    [adresse]                       VARCHAR(255) NULL,
    [ville]                         VARCHAR(255) NULL,
    [pays]                          VARCHAR(255) NULL,
    [code_postal]                   VARCHAR(50) NULL,
    [numero_identification_fiscale] VARCHAR(255) NULL,
    [registre_commerce]             VARCHAR(255) NULL,
    [categorie_fournisseur_id]      BIGINT NULL,
    [entreprise_id]                 BIGINT NULL,
    [created_at]                    DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    [updated_at]                    DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    CONSTRAINT [PK_fournisseur] PRIMARY KEY CLUSTERED ([id] ASC)
      WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,
            ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

-- ============================================================
-- 6. INVENTORY DOMAIN
--    Tables: mesure, tva, categorie_article, article,
--            article_vente, stock, mouvement_stock
-- ============================================================

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[mesure] (
    [id]         BIGINT IDENTITY(1,1) NOT NULL,
    [nom]        VARCHAR(255) NOT NULL,
    [symbole]    VARCHAR(50) NULL,
    [created_at] DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    [updated_at] DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    CONSTRAINT [PK_mesure] PRIMARY KEY CLUSTERED ([id] ASC)
      WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,
            ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[tva] (
    [id]         BIGINT IDENTITY(1,1) NOT NULL,
    [taux]       DECIMAL(5,2) NOT NULL,
    [nom]        VARCHAR(255) NULL,
    [created_at] DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    [updated_at] DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    CONSTRAINT [PK_tva] PRIMARY KEY CLUSTERED ([id] ASC)
      WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,
            ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[categorie_article] (
    [id]          BIGINT IDENTITY(1,1) NOT NULL,
    [nom]         VARCHAR(255) NULL,
    [description] VARCHAR(255) NULL,
    [created_at]  DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    [updated_at]  DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    CONSTRAINT [PK_categorie_article] PRIMARY KEY CLUSTERED ([id] ASC)
      WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,
            ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[article] (
    [id]                   BIGINT IDENTITY(1,1) NOT NULL,
    [reference]            VARCHAR(255) NULL,
    [nom]                  VARCHAR(255) NOT NULL,
    [description]          VARCHAR(500) NULL,
    [prix_achat]           DECIMAL(15,2) NULL,
    [prix_vente]           DECIMAL(15,2) NULL,
    [actif]                BIT NOT NULL DEFAULT 1,
    [categorie_article_id] BIGINT NULL,
    [mesure_id]            BIGINT NULL,
    [tva_id]               BIGINT NULL,
    [entreprise_id]        BIGINT NOT NULL,
    [created_at]           DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    [updated_at]           DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    CONSTRAINT [PK_article] PRIMARY KEY CLUSTERED ([id] ASC)
      WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,
            ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[article_vente] (
    [id]                   BIGINT IDENTITY(1,1) NOT NULL,
    [article_id]           BIGINT NOT NULL,
    [prix_vente_special]   DECIMAL(15,2) NULL,
    [description_vente]    VARCHAR(500) NULL,
    [created_at]           DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    [updated_at]           DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    CONSTRAINT [PK_article_vente] PRIMARY KEY CLUSTERED ([id] ASC)
      WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,
            ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[stock] (
    [id]                  BIGINT IDENTITY(1,1) NOT NULL,
    [article_id]          BIGINT NOT NULL,
    [entreprise_id]       BIGINT NOT NULL,
    [quantite_disponible] DECIMAL(15,3) NOT NULL DEFAULT 0,
    [quantite_minimale]   DECIMAL(15,3) NOT NULL DEFAULT 0,
    [quantite_maximale]   DECIMAL(15,3) NULL,
    [emplacement]         VARCHAR(255) NULL,
    [created_at]          DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    [updated_at]          DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    CONSTRAINT [PK_stock] PRIMARY KEY CLUSTERED ([id] ASC)
      WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,
            ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[mouvement_stock] (
    [id]                  BIGINT IDENTITY(1,1) NOT NULL,
    [article_id]          BIGINT NOT NULL,
    [entreprise_id]       BIGINT NOT NULL,
    [type_mouvement]      VARCHAR(50) NOT NULL,
    [quantite]            DECIMAL(15,3) NOT NULL,
    [date_mouvement]      DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    [reference_document]  VARCHAR(255) NULL,
    [description]         VARCHAR(500) NULL,
    [created_at]          DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    [updated_at]          DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    CONSTRAINT [PK_mouvement_stock] PRIMARY KEY CLUSTERED ([id] ASC)
      WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,
            ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

-- ============================================================
-- 7. PURCHASE DOMAIN
--    Tables: commande_achat, ligne_commande_achat, bon_reception
-- ============================================================

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[commande_achat] (
    [id]                    BIGINT IDENTITY(1,1) NOT NULL,
    [reference]             VARCHAR(255) NULL,
    [date_commande]         DATE NULL,
    [date_livraison_prevue] DATE NULL,
    [statut]                VARCHAR(50) NOT NULL DEFAULT 'BROUILLON',
    [montant_ht]            DECIMAL(15,2) NULL,
    [montant_tva]           DECIMAL(15,2) NULL,
    [montant_ttc]           DECIMAL(15,2) NULL,
    [remise]                DECIMAL(5,2) NULL DEFAULT 0,
    [description]           VARCHAR(500) NULL,
    [fournisseur_id]        BIGINT NULL,
    [entreprise_id]         BIGINT NOT NULL,
    [created_at]            DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    [updated_at]            DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    CONSTRAINT [PK_commande_achat] PRIMARY KEY CLUSTERED ([id] ASC)
      WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,
            ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[ligne_commande_achat] (
    [id]                 BIGINT IDENTITY(1,1) NOT NULL,
    [commande_achat_id]  BIGINT NOT NULL,
    [article_id]         BIGINT NOT NULL,
    [quantite]           DECIMAL(15,3) NOT NULL,
    [prix_unitaire_ht]   DECIMAL(15,2) NOT NULL,
    [taux_tva]           DECIMAL(5,2) NULL DEFAULT 0,
    [montant_ht]         DECIMAL(15,2) NULL,
    [montant_ttc]        DECIMAL(15,2) NULL,
    [created_at]         DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    [updated_at]         DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    CONSTRAINT [PK_ligne_commande_achat] PRIMARY KEY CLUSTERED ([id] ASC)
      WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,
            ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[bon_reception] (
    [id]                BIGINT IDENTITY(1,1) NOT NULL,
    [reference]         VARCHAR(255) NULL,
    [date_reception]    DATE NULL,
    [statut]            VARCHAR(50) NOT NULL DEFAULT 'EN_ATTENTE',
    [commande_achat_id] BIGINT NULL,
    [entreprise_id]     BIGINT NOT NULL,
    [description]       VARCHAR(500) NULL,
    [created_at]        DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    [updated_at]        DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    CONSTRAINT [PK_bon_reception] PRIMARY KEY CLUSTERED ([id] ASC)
      WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,
            ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

-- ============================================================
-- 8. SALES DOMAIN
--    Tables: commande_vente, ligne_commande_vente, bon_livraison
-- ============================================================

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[commande_vente] (
    [id]                    BIGINT IDENTITY(1,1) NOT NULL,
    [reference]             VARCHAR(255) NULL,
    [date_commande]         DATE NULL,
    [date_livraison_prevue] DATE NULL,
    [statut]                VARCHAR(50) NOT NULL DEFAULT 'BROUILLON',
    [montant_ht]            DECIMAL(15,2) NULL,
    [montant_tva]           DECIMAL(15,2) NULL,
    [montant_ttc]           DECIMAL(15,2) NULL,
    [remise]                DECIMAL(5,2) NULL DEFAULT 0,
    [description]           VARCHAR(500) NULL,
    [client_id]             BIGINT NULL,
    [entreprise_id]         BIGINT NOT NULL,
    [created_at]            DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    [updated_at]            DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    CONSTRAINT [PK_commande_vente] PRIMARY KEY CLUSTERED ([id] ASC)
      WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,
            ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[ligne_commande_vente] (
    [id]                  BIGINT IDENTITY(1,1) NOT NULL,
    [commande_vente_id]   BIGINT NOT NULL,
    [article_id]          BIGINT NOT NULL,
    [quantite]            DECIMAL(15,3) NOT NULL,
    [prix_unitaire_ht]    DECIMAL(15,2) NOT NULL,
    [taux_tva]            DECIMAL(5,2) NULL DEFAULT 0,
    [montant_ht]          DECIMAL(15,2) NULL,
    [montant_ttc]         DECIMAL(15,2) NULL,
    [created_at]          DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    [updated_at]          DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    CONSTRAINT [PK_ligne_commande_vente] PRIMARY KEY CLUSTERED ([id] ASC)
      WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,
            ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[bon_livraison] (
    [id]                BIGINT IDENTITY(1,1) NOT NULL,
    [reference]         VARCHAR(255) NULL,
    [date_livraison]    DATE NULL,
    [statut]            VARCHAR(50) NOT NULL DEFAULT 'EN_ATTENTE',
    [commande_vente_id] BIGINT NULL,
    [entreprise_id]     BIGINT NOT NULL,
    [description]       VARCHAR(500) NULL,
    [created_at]        DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    [updated_at]        DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    CONSTRAINT [PK_bon_livraison] PRIMARY KEY CLUSTERED ([id] ASC)
      WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,
            ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

-- ============================================================
-- 9. ACCOUNTING DOMAIN
--    Tables: categorie_depense, depense,
--            facture_achat, ligne_facture_achat,
--            facture_de_vente, ligne_facture_vente,
--            caracteristique_facture_entreprise
-- ============================================================

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[categorie_depense] (
    [id]          BIGINT IDENTITY(1,1) NOT NULL,
    [nom]         VARCHAR(255) NULL,
    [description] VARCHAR(255) NULL,
    [created_at]  DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    [updated_at]  DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    CONSTRAINT [PK_categorie_depense] PRIMARY KEY CLUSTERED ([id] ASC)
      WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,
            ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[depense] (
    [id]                   BIGINT IDENTITY(1,1) NOT NULL,
    [nom]                  VARCHAR(255) NULL,
    [description]          VARCHAR(255) NULL,
    [montant]              DECIMAL(15,2) NULL,
    [date_depense]         DATE NULL,
    [statut]               VARCHAR(50) NULL DEFAULT 'EN_ATTENTE',
    [categorie_depense_id] BIGINT NULL,
    [entreprise_id]        BIGINT NOT NULL,
    [created_at]           DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    [updated_at]           DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    CONSTRAINT [PK_depense] PRIMARY KEY CLUSTERED ([id] ASC)
      WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,
            ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[facture_achat] (
    [id]                BIGINT IDENTITY(1,1) NOT NULL,
    [referance]         VARCHAR(255) NULL,
    [date_achat]        VARCHAR(255) NULL,
    [description]       VARCHAR(255) NULL,
    [mode_paiement]     VARCHAR(255) NULL,
    [montantht]         FLOAT NULL,
    [montanttva]        FLOAT NULL,
    [montantttc]        FLOAT NULL,
    [montant_total]     FLOAT NULL,
    [remise]            FLOAT NULL,
    [statut]            VARCHAR(50) NULL DEFAULT 'EN_ATTENTE',
    [chemin_fichier]    VARCHAR(255) NULL,
    [fournisseur_id]    BIGINT NULL,
    [commande_achat_id] BIGINT NULL,
    [entreprise_id]     BIGINT NOT NULL,
    [created_at]        DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    [updated_at]        DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    CONSTRAINT [PK_facture_achat] PRIMARY KEY CLUSTERED ([id] ASC)
      WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,
            ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

-- ligne_facture_achat replaces detais_facture_achat
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[ligne_facture_achat] (
    [id]               BIGINT IDENTITY(1,1) NOT NULL,
    [facture_achat_id] BIGINT NOT NULL,
    [article_id]       BIGINT NULL,
    [article_nom]      VARCHAR(255) NULL,
    [quantite]         DECIMAL(15,3) NOT NULL,
    [prix_unitaire_ht] DECIMAL(15,2) NOT NULL,
    [taux_tva]         DECIMAL(5,2) NULL DEFAULT 0,
    [montant_ht]       DECIMAL(15,2) NULL,
    [montant_ttc]      DECIMAL(15,2) NULL,
    [created_at]       DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    [updated_at]       DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    CONSTRAINT [PK_ligne_facture_achat] PRIMARY KEY CLUSTERED ([id] ASC)
      WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,
            ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[facture_de_vente] (
    [id]                 BIGINT IDENTITY(1,1) NOT NULL,
    [referance]          VARCHAR(255) NULL,
    [date_achat]         VARCHAR(255) NULL,
    [description]        VARCHAR(255) NULL,
    [mode_paiement]      VARCHAR(255) NULL,
    [montantht]          FLOAT NULL,
    [montanttva]         FLOAT NULL,
    [montantttc]         FLOAT NULL,
    [montant_total]      FLOAT NULL,
    [remise]             FLOAT NULL,
    [statut]             VARCHAR(50) NULL DEFAULT 'EN_ATTENTE',
    [chemin_fichier]     VARCHAR(255) NULL,
    [client_id]          BIGINT NULL,
    [commande_vente_id]  BIGINT NULL,
    [entreprise_id]      BIGINT NOT NULL,
    [created_at]         DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    [updated_at]         DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    CONSTRAINT [PK_facture_de_vente] PRIMARY KEY CLUSTERED ([id] ASC)
      WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,
            ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

-- ligne_facture_vente replaces detais_facture_de_vente
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[ligne_facture_vente] (
    [id]                  BIGINT IDENTITY(1,1) NOT NULL,
    [facture_de_vente_id] BIGINT NOT NULL,
    [article_id]          BIGINT NULL,
    [article_nom]         VARCHAR(255) NULL,
    [quantite]            DECIMAL(15,3) NOT NULL,
    [prix_unitaire_ht]    DECIMAL(15,2) NOT NULL,
    [taux_tva]            DECIMAL(5,2) NULL DEFAULT 0,
    [montant_ht]          DECIMAL(15,2) NULL,
    [montant_ttc]         DECIMAL(15,2) NULL,
    [created_at]          DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    [updated_at]          DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    CONSTRAINT [PK_ligne_facture_vente] PRIMARY KEY CLUSTERED ([id] ASC)
      WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,
            ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[caracteristique_facture_entreprise] (
    [id]              BIGINT IDENTITY(1,1) NOT NULL,
    [color_footerb]   BIGINT NULL,
    [color_footerg]   BIGINT NULL,
    [color_footerr]   BIGINT NULL,
    [color_headerb]   BIGINT NULL,
    [color_headerg]   BIGINT NULL,
    [color_headerr]   BIGINT NULL,
    [fill_colorb]     BIGINT NULL,
    [fill_colorg]     BIGINT NULL,
    [fill_colorr]     BIGINT NULL,
    [font_name]       VARCHAR(255) NULL,
    [font_style]      VARCHAR(255) NULL,
    [message_footer]  VARCHAR(255) NULL,
    [text_colorb]     BIGINT NULL,
    [text_colorg]     BIGINT NULL,
    [text_colorr]     BIGINT NULL,
    [entreprise_id]   BIGINT NULL,
    [created_at]      DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    [updated_at]      DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    CONSTRAINT [PK_caracteristique_facture_entreprise] PRIMARY KEY CLUSTERED ([id] ASC)
      WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,
            ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

-- ============================================================
-- 10. DOCUMENT DOMAIN
--     Tables: categorie_document, file_chemin, document,
--             categorie_document_documents,
--             zone_selectionnee_de_document
-- ============================================================

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[categorie_document] (
    [id]          BIGINT IDENTITY(1,1) NOT NULL,
    [nom]         VARCHAR(255) NULL,
    [description] VARCHAR(255) NULL,
    [created_at]  DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    [updated_at]  DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    CONSTRAINT [PK_categorie_document] PRIMARY KEY CLUSTERED ([id] ASC)
      WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,
            ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[file_chemin] (
    [id]         BIGINT IDENTITY(1,1) NOT NULL,
    [urlpdf]     VARCHAR(255) NULL,
    [created_at] DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    [updated_at] DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    CONSTRAINT [PK_file_chemin] PRIMARY KEY CLUSTERED ([id] ASC)
      WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,
            ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[document] (
    [id]                    BIGINT IDENTITY(1,1) NOT NULL,
    [nom]                   VARCHAR(255) NULL,
    [type]                  VARCHAR(255) NULL,
    [date_ajout]            DATETIME2(6) NULL,
    [categorie_document_id] BIGINT NULL,
    [file_chemin_id]        BIGINT NULL,
    [created_at]            DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    [updated_at]            DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    CONSTRAINT [PK_document] PRIMARY KEY CLUSTERED ([id] ASC)
      WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,
            ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[categorie_document_documents] (
    [categorie_document_id] BIGINT NOT NULL,
    [documents_id]          BIGINT NOT NULL
) ON [PRIMARY]
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[zone_selectionnee_de_document] (
    [id]              BIGINT IDENTITY(1,1) NOT NULL,
    [canvas_height]   REAL NOT NULL,
    [canvas_width]    REAL NOT NULL,
    [height]          REAL NOT NULL,
    [page_number]     INT NOT NULL,
    [pdf_height]      REAL NOT NULL,
    [pdf_width]       REAL NOT NULL,
    [type]            VARCHAR(255) NULL,
    [type_de_document] VARCHAR(255) NULL,
    [width]           REAL NOT NULL,
    [x]               REAL NOT NULL,
    [y]               REAL NOT NULL,
    CONSTRAINT [PK_zone_selectionnee_de_document] PRIMARY KEY CLUSTERED ([id] ASC)
      WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,
            ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

-- prix_client created after both client and article
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[prix_client] (
    [id]            BIGINT IDENTITY(1,1) NOT NULL,
    [client_id]     BIGINT NOT NULL,
    [article_id]    BIGINT NOT NULL,
    [prix]          DECIMAL(15,2) NOT NULL,
    [date_debut]    DATE NULL,
    [date_fin]      DATE NULL,
    [entreprise_id] BIGINT NOT NULL,
    [created_at]    DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    [updated_at]    DATETIME2(6) NOT NULL DEFAULT GETDATE(),
    CONSTRAINT [PK_prix_client] PRIMARY KEY CLUSTERED ([id] ASC)
      WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,
            ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

-- ============================================================
-- 11. UNIQUE CONSTRAINTS AND INDEXES
-- ============================================================

SET ANSI_PADDING ON
GO

-- app_user: unique username
ALTER TABLE [dbo].[app_user]
    ADD CONSTRAINT [UQ_app_user_username] UNIQUE NONCLUSTERED ([username] ASC)
    WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,
          ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
GO

-- roles: unique name
CREATE UNIQUE NONCLUSTERED INDEX [UQ_roles_name] ON [dbo].[roles] ([name] ASC)
WHERE ([name] IS NOT NULL)
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,
      DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
GO

-- caracteristique_facture_entreprise: unique entreprise_id
CREATE UNIQUE NONCLUSTERED INDEX [UQ_car_fact_ent_entreprise_id] ON [dbo].[caracteristique_facture_entreprise] ([entreprise_id] ASC)
WHERE ([entreprise_id] IS NOT NULL)
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,
      DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
GO

-- document: unique file_chemin_id
CREATE UNIQUE NONCLUSTERED INDEX [UQ_document_file_chemin_id] ON [dbo].[document] ([file_chemin_id] ASC)
WHERE ([file_chemin_id] IS NOT NULL)
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,
      DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
GO

-- categorie_document_documents: unique documents_id
ALTER TABLE [dbo].[categorie_document_documents]
    ADD CONSTRAINT [UQ_cat_doc_docs_documents_id] UNIQUE NONCLUSTERED ([documents_id] ASC)
    WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,
          ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
GO

-- article: unique reference per entreprise
CREATE UNIQUE NONCLUSTERED INDEX [UQ_article_ref_entreprise] ON [dbo].[article] ([reference] ASC, [entreprise_id] ASC)
WHERE ([reference] IS NOT NULL)
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,
      DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
GO

-- stock: unique article per entreprise
CREATE UNIQUE NONCLUSTERED INDEX [UQ_stock_article_entreprise] ON [dbo].[stock] ([article_id] ASC, [entreprise_id] ASC)
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF,
      DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
GO

-- Indexes for multi-tenant queries
CREATE NONCLUSTERED INDEX [IX_app_user_entreprise_id] ON [dbo].[app_user] ([entreprise_id] ASC) ON [PRIMARY]
GO
CREATE NONCLUSTERED INDEX [IX_client_entreprise_id] ON [dbo].[client] ([entreprise_id] ASC) ON [PRIMARY]
GO
CREATE NONCLUSTERED INDEX [IX_fournisseur_entreprise_id] ON [dbo].[fournisseur] ([entreprise_id] ASC) ON [PRIMARY]
GO
CREATE NONCLUSTERED INDEX [IX_article_entreprise_id] ON [dbo].[article] ([entreprise_id] ASC) ON [PRIMARY]
GO
CREATE NONCLUSTERED INDEX [IX_stock_entreprise_id] ON [dbo].[stock] ([entreprise_id] ASC) ON [PRIMARY]
GO
CREATE NONCLUSTERED INDEX [IX_commande_achat_entreprise_id] ON [dbo].[commande_achat] ([entreprise_id] ASC) ON [PRIMARY]
GO
CREATE NONCLUSTERED INDEX [IX_commande_vente_entreprise_id] ON [dbo].[commande_vente] ([entreprise_id] ASC) ON [PRIMARY]
GO
CREATE NONCLUSTERED INDEX [IX_facture_achat_entreprise_id] ON [dbo].[facture_achat] ([entreprise_id] ASC) ON [PRIMARY]
GO
CREATE NONCLUSTERED INDEX [IX_facture_de_vente_entreprise_id] ON [dbo].[facture_de_vente] ([entreprise_id] ASC) ON [PRIMARY]
GO
CREATE NONCLUSTERED INDEX [IX_depense_entreprise_id] ON [dbo].[depense] ([entreprise_id] ASC) ON [PRIMARY]
GO

-- Indexes for FK lookups
CREATE NONCLUSTERED INDEX [IX_client_categorie_client_id] ON [dbo].[client] ([categorie_client_id] ASC) ON [PRIMARY]
GO
CREATE NONCLUSTERED INDEX [IX_facture_de_vente_client_id] ON [dbo].[facture_de_vente] ([client_id] ASC) ON [PRIMARY]
GO
CREATE NONCLUSTERED INDEX [IX_facture_achat_fournisseur_id] ON [dbo].[facture_achat] ([fournisseur_id] ASC) ON [PRIMARY]
GO
CREATE NONCLUSTERED INDEX [IX_ligne_commande_achat_article_id] ON [dbo].[ligne_commande_achat] ([article_id] ASC) ON [PRIMARY]
GO
CREATE NONCLUSTERED INDEX [IX_ligne_commande_vente_article_id] ON [dbo].[ligne_commande_vente] ([article_id] ASC) ON [PRIMARY]
GO

-- Indexes for statut filter queries
CREATE NONCLUSTERED INDEX [IX_commande_achat_statut] ON [dbo].[commande_achat] ([statut] ASC) ON [PRIMARY]
GO
CREATE NONCLUSTERED INDEX [IX_commande_vente_statut] ON [dbo].[commande_vente] ([statut] ASC) ON [PRIMARY]
GO
CREATE NONCLUSTERED INDEX [IX_facture_achat_statut] ON [dbo].[facture_achat] ([statut] ASC) ON [PRIMARY]
GO
CREATE NONCLUSTERED INDEX [IX_facture_de_vente_statut] ON [dbo].[facture_de_vente] ([statut] ASC) ON [PRIMARY]
GO

-- ============================================================
-- 12. CHECK CONSTRAINTS
-- ============================================================

ALTER TABLE [dbo].[mouvement_stock] WITH CHECK
    ADD CONSTRAINT [CHK_mouvement_stock_type]
    CHECK ([type_mouvement] IN ('ENTREE', 'SORTIE', 'RETOUR', 'AJUSTEMENT'))
GO
ALTER TABLE [dbo].[mouvement_stock] CHECK CONSTRAINT [CHK_mouvement_stock_type]
GO

ALTER TABLE [dbo].[commande_achat] WITH CHECK
    ADD CONSTRAINT [CHK_commande_achat_statut]
    CHECK ([statut] IN ('BROUILLON', 'EN_ATTENTE', 'CONFIRMEE', 'RECUE', 'ANNULEE'))
GO
ALTER TABLE [dbo].[commande_achat] CHECK CONSTRAINT [CHK_commande_achat_statut]
GO

ALTER TABLE [dbo].[bon_reception] WITH CHECK
    ADD CONSTRAINT [CHK_bon_reception_statut]
    CHECK ([statut] IN ('EN_ATTENTE', 'PARTIEL', 'COMPLET', 'ANNULE'))
GO
ALTER TABLE [dbo].[bon_reception] CHECK CONSTRAINT [CHK_bon_reception_statut]
GO

ALTER TABLE [dbo].[commande_vente] WITH CHECK
    ADD CONSTRAINT [CHK_commande_vente_statut]
    CHECK ([statut] IN ('BROUILLON', 'EN_ATTENTE', 'CONFIRMEE', 'LIVREE', 'ANNULEE'))
GO
ALTER TABLE [dbo].[commande_vente] CHECK CONSTRAINT [CHK_commande_vente_statut]
GO

ALTER TABLE [dbo].[bon_livraison] WITH CHECK
    ADD CONSTRAINT [CHK_bon_livraison_statut]
    CHECK ([statut] IN ('EN_ATTENTE', 'PARTIEL', 'LIVRE', 'ANNULE'))
GO
ALTER TABLE [dbo].[bon_livraison] CHECK CONSTRAINT [CHK_bon_livraison_statut]
GO

ALTER TABLE [dbo].[depense] WITH CHECK
    ADD CONSTRAINT [CHK_depense_statut]
    CHECK ([statut] IN ('EN_ATTENTE', 'APPROUVEE', 'PAYEE', 'REJETEE'))
GO
ALTER TABLE [dbo].[depense] CHECK CONSTRAINT [CHK_depense_statut]
GO

ALTER TABLE [dbo].[facture_achat] WITH CHECK
    ADD CONSTRAINT [CHK_facture_achat_statut]
    CHECK ([statut] IN ('EN_ATTENTE', 'PAYEE', 'PARTIELLEMENT_PAYEE', 'ANNULEE'))
GO
ALTER TABLE [dbo].[facture_achat] CHECK CONSTRAINT [CHK_facture_achat_statut]
GO

ALTER TABLE [dbo].[facture_de_vente] WITH CHECK
    ADD CONSTRAINT [CHK_facture_de_vente_statut]
    CHECK ([statut] IN ('EN_ATTENTE', 'PAYEE', 'PARTIELLEMENT_PAYEE', 'ANNULEE'))
GO
ALTER TABLE [dbo].[facture_de_vente] CHECK CONSTRAINT [CHK_facture_de_vente_statut]
GO

-- ============================================================
-- 13. FOREIGN KEY CONSTRAINTS
-- ============================================================

-- Auth domain
ALTER TABLE [dbo].[app_user] WITH CHECK
    ADD CONSTRAINT [FK_app_user_entreprise] FOREIGN KEY ([entreprise_id]) REFERENCES [dbo].[entreprise] ([id])
GO
ALTER TABLE [dbo].[app_user] CHECK CONSTRAINT [FK_app_user_entreprise]
GO

ALTER TABLE [dbo].[user_roles] WITH CHECK
    ADD CONSTRAINT [FK_user_roles_user] FOREIGN KEY ([user_id]) REFERENCES [dbo].[app_user] ([id])
GO
ALTER TABLE [dbo].[user_roles] CHECK CONSTRAINT [FK_user_roles_user]
GO

ALTER TABLE [dbo].[user_roles] WITH CHECK
    ADD CONSTRAINT [FK_user_roles_role] FOREIGN KEY ([role_id]) REFERENCES [dbo].[roles] ([id])
GO
ALTER TABLE [dbo].[user_roles] CHECK CONSTRAINT [FK_user_roles_role]
GO

ALTER TABLE [dbo].[module] WITH CHECK
    ADD CONSTRAINT [FK_module_module_tittle] FOREIGN KEY ([module_tittle_id]) REFERENCES [dbo].[module_tittle] ([id])
GO
ALTER TABLE [dbo].[module] CHECK CONSTRAINT [FK_module_module_tittle]
GO

ALTER TABLE [dbo].[sous_module] WITH CHECK
    ADD CONSTRAINT [FK_sous_module_module] FOREIGN KEY ([module_id]) REFERENCES [dbo].[module] ([id])
GO
ALTER TABLE [dbo].[sous_module] CHECK CONSTRAINT [FK_sous_module_module]
GO

ALTER TABLE [dbo].[permission] WITH CHECK
    ADD CONSTRAINT [FK_permission_role] FOREIGN KEY ([role_id]) REFERENCES [dbo].[roles] ([id])
GO
ALTER TABLE [dbo].[permission] CHECK CONSTRAINT [FK_permission_role]
GO

ALTER TABLE [dbo].[permission] WITH CHECK
    ADD CONSTRAINT [FK_permission_module] FOREIGN KEY ([module_id]) REFERENCES [dbo].[module] ([id])
GO
ALTER TABLE [dbo].[permission] CHECK CONSTRAINT [FK_permission_module]
GO

-- Company domain
ALTER TABLE [dbo].[departement] WITH CHECK
    ADD CONSTRAINT [FK_departement_entreprise] FOREIGN KEY ([entreprise_id]) REFERENCES [dbo].[entreprise] ([id])
GO
ALTER TABLE [dbo].[departement] CHECK CONSTRAINT [FK_departement_entreprise]
GO

ALTER TABLE [dbo].[poste] WITH CHECK
    ADD CONSTRAINT [FK_poste_entreprise] FOREIGN KEY ([entreprise_id]) REFERENCES [dbo].[entreprise] ([id])
GO
ALTER TABLE [dbo].[poste] CHECK CONSTRAINT [FK_poste_entreprise]
GO

ALTER TABLE [dbo].[poste] WITH CHECK
    ADD CONSTRAINT [FK_poste_departement] FOREIGN KEY ([departement_id]) REFERENCES [dbo].[departement] ([id])
GO
ALTER TABLE [dbo].[poste] CHECK CONSTRAINT [FK_poste_departement]
GO

-- CRM domain
ALTER TABLE [dbo].[client] WITH CHECK
    ADD CONSTRAINT [FK_client_categorie_client] FOREIGN KEY ([categorie_client_id]) REFERENCES [dbo].[categorie_client] ([id])
GO
ALTER TABLE [dbo].[client] CHECK CONSTRAINT [FK_client_categorie_client]
GO

ALTER TABLE [dbo].[client] WITH CHECK
    ADD CONSTRAINT [FK_client_entreprise] FOREIGN KEY ([entreprise_id]) REFERENCES [dbo].[entreprise] ([id])
GO
ALTER TABLE [dbo].[client] CHECK CONSTRAINT [FK_client_entreprise]
GO

ALTER TABLE [dbo].[client_entreprises] WITH CHECK
    ADD CONSTRAINT [FK_client_ent_client] FOREIGN KEY ([client_id]) REFERENCES [dbo].[client] ([id])
GO
ALTER TABLE [dbo].[client_entreprises] CHECK CONSTRAINT [FK_client_ent_client]
GO

ALTER TABLE [dbo].[client_entreprises] WITH CHECK
    ADD CONSTRAINT [FK_client_ent_entreprise] FOREIGN KEY ([entreprises_id]) REFERENCES [dbo].[entreprise] ([id])
GO
ALTER TABLE [dbo].[client_entreprises] CHECK CONSTRAINT [FK_client_ent_entreprise]
GO

ALTER TABLE [dbo].[prix_client] WITH CHECK
    ADD CONSTRAINT [FK_prix_client_client] FOREIGN KEY ([client_id]) REFERENCES [dbo].[client] ([id])
GO
ALTER TABLE [dbo].[prix_client] CHECK CONSTRAINT [FK_prix_client_client]
GO

ALTER TABLE [dbo].[prix_client] WITH CHECK
    ADD CONSTRAINT [FK_prix_client_article] FOREIGN KEY ([article_id]) REFERENCES [dbo].[article] ([id])
GO
ALTER TABLE [dbo].[prix_client] CHECK CONSTRAINT [FK_prix_client_article]
GO

ALTER TABLE [dbo].[prix_client] WITH CHECK
    ADD CONSTRAINT [FK_prix_client_entreprise] FOREIGN KEY ([entreprise_id]) REFERENCES [dbo].[entreprise] ([id])
GO
ALTER TABLE [dbo].[prix_client] CHECK CONSTRAINT [FK_prix_client_entreprise]
GO

-- Supplier domain
ALTER TABLE [dbo].[fournisseur] WITH CHECK
    ADD CONSTRAINT [FK_fournisseur_categorie] FOREIGN KEY ([categorie_fournisseur_id]) REFERENCES [dbo].[categorie_fournisseur] ([id])
GO
ALTER TABLE [dbo].[fournisseur] CHECK CONSTRAINT [FK_fournisseur_categorie]
GO

ALTER TABLE [dbo].[fournisseur] WITH CHECK
    ADD CONSTRAINT [FK_fournisseur_entreprise] FOREIGN KEY ([entreprise_id]) REFERENCES [dbo].[entreprise] ([id])
GO
ALTER TABLE [dbo].[fournisseur] CHECK CONSTRAINT [FK_fournisseur_entreprise]
GO

-- Inventory domain
ALTER TABLE [dbo].[article] WITH CHECK
    ADD CONSTRAINT [FK_article_categorie] FOREIGN KEY ([categorie_article_id]) REFERENCES [dbo].[categorie_article] ([id])
GO
ALTER TABLE [dbo].[article] CHECK CONSTRAINT [FK_article_categorie]
GO

ALTER TABLE [dbo].[article] WITH CHECK
    ADD CONSTRAINT [FK_article_mesure] FOREIGN KEY ([mesure_id]) REFERENCES [dbo].[mesure] ([id])
GO
ALTER TABLE [dbo].[article] CHECK CONSTRAINT [FK_article_mesure]
GO

ALTER TABLE [dbo].[article] WITH CHECK
    ADD CONSTRAINT [FK_article_tva] FOREIGN KEY ([tva_id]) REFERENCES [dbo].[tva] ([id])
GO
ALTER TABLE [dbo].[article] CHECK CONSTRAINT [FK_article_tva]
GO

ALTER TABLE [dbo].[article] WITH CHECK
    ADD CONSTRAINT [FK_article_entreprise] FOREIGN KEY ([entreprise_id]) REFERENCES [dbo].[entreprise] ([id])
GO
ALTER TABLE [dbo].[article] CHECK CONSTRAINT [FK_article_entreprise]
GO

ALTER TABLE [dbo].[article_vente] WITH CHECK
    ADD CONSTRAINT [FK_article_vente_article] FOREIGN KEY ([article_id]) REFERENCES [dbo].[article] ([id])
GO
ALTER TABLE [dbo].[article_vente] CHECK CONSTRAINT [FK_article_vente_article]
GO

ALTER TABLE [dbo].[stock] WITH CHECK
    ADD CONSTRAINT [FK_stock_article] FOREIGN KEY ([article_id]) REFERENCES [dbo].[article] ([id])
GO
ALTER TABLE [dbo].[stock] CHECK CONSTRAINT [FK_stock_article]
GO

ALTER TABLE [dbo].[stock] WITH CHECK
    ADD CONSTRAINT [FK_stock_entreprise] FOREIGN KEY ([entreprise_id]) REFERENCES [dbo].[entreprise] ([id])
GO
ALTER TABLE [dbo].[stock] CHECK CONSTRAINT [FK_stock_entreprise]
GO

ALTER TABLE [dbo].[mouvement_stock] WITH CHECK
    ADD CONSTRAINT [FK_mouvement_stock_article] FOREIGN KEY ([article_id]) REFERENCES [dbo].[article] ([id])
GO
ALTER TABLE [dbo].[mouvement_stock] CHECK CONSTRAINT [FK_mouvement_stock_article]
GO

ALTER TABLE [dbo].[mouvement_stock] WITH CHECK
    ADD CONSTRAINT [FK_mouvement_stock_entreprise] FOREIGN KEY ([entreprise_id]) REFERENCES [dbo].[entreprise] ([id])
GO
ALTER TABLE [dbo].[mouvement_stock] CHECK CONSTRAINT [FK_mouvement_stock_entreprise]
GO

-- Purchase domain
ALTER TABLE [dbo].[commande_achat] WITH CHECK
    ADD CONSTRAINT [FK_commande_achat_fournisseur] FOREIGN KEY ([fournisseur_id]) REFERENCES [dbo].[fournisseur] ([id])
GO
ALTER TABLE [dbo].[commande_achat] CHECK CONSTRAINT [FK_commande_achat_fournisseur]
GO

ALTER TABLE [dbo].[commande_achat] WITH CHECK
    ADD CONSTRAINT [FK_commande_achat_entreprise] FOREIGN KEY ([entreprise_id]) REFERENCES [dbo].[entreprise] ([id])
GO
ALTER TABLE [dbo].[commande_achat] CHECK CONSTRAINT [FK_commande_achat_entreprise]
GO

ALTER TABLE [dbo].[ligne_commande_achat] WITH CHECK
    ADD CONSTRAINT [FK_lca_commande_achat] FOREIGN KEY ([commande_achat_id]) REFERENCES [dbo].[commande_achat] ([id])
GO
ALTER TABLE [dbo].[ligne_commande_achat] CHECK CONSTRAINT [FK_lca_commande_achat]
GO

ALTER TABLE [dbo].[ligne_commande_achat] WITH CHECK
    ADD CONSTRAINT [FK_lca_article] FOREIGN KEY ([article_id]) REFERENCES [dbo].[article] ([id])
GO
ALTER TABLE [dbo].[ligne_commande_achat] CHECK CONSTRAINT [FK_lca_article]
GO

ALTER TABLE [dbo].[bon_reception] WITH CHECK
    ADD CONSTRAINT [FK_bon_reception_commande_achat] FOREIGN KEY ([commande_achat_id]) REFERENCES [dbo].[commande_achat] ([id])
GO
ALTER TABLE [dbo].[bon_reception] CHECK CONSTRAINT [FK_bon_reception_commande_achat]
GO

ALTER TABLE [dbo].[bon_reception] WITH CHECK
    ADD CONSTRAINT [FK_bon_reception_entreprise] FOREIGN KEY ([entreprise_id]) REFERENCES [dbo].[entreprise] ([id])
GO
ALTER TABLE [dbo].[bon_reception] CHECK CONSTRAINT [FK_bon_reception_entreprise]
GO

-- Sales domain
ALTER TABLE [dbo].[commande_vente] WITH CHECK
    ADD CONSTRAINT [FK_commande_vente_client] FOREIGN KEY ([client_id]) REFERENCES [dbo].[client] ([id])
GO
ALTER TABLE [dbo].[commande_vente] CHECK CONSTRAINT [FK_commande_vente_client]
GO

ALTER TABLE [dbo].[commande_vente] WITH CHECK
    ADD CONSTRAINT [FK_commande_vente_entreprise] FOREIGN KEY ([entreprise_id]) REFERENCES [dbo].[entreprise] ([id])
GO
ALTER TABLE [dbo].[commande_vente] CHECK CONSTRAINT [FK_commande_vente_entreprise]
GO

ALTER TABLE [dbo].[ligne_commande_vente] WITH CHECK
    ADD CONSTRAINT [FK_lcv_commande_vente] FOREIGN KEY ([commande_vente_id]) REFERENCES [dbo].[commande_vente] ([id])
GO
ALTER TABLE [dbo].[ligne_commande_vente] CHECK CONSTRAINT [FK_lcv_commande_vente]
GO

ALTER TABLE [dbo].[ligne_commande_vente] WITH CHECK
    ADD CONSTRAINT [FK_lcv_article] FOREIGN KEY ([article_id]) REFERENCES [dbo].[article] ([id])
GO
ALTER TABLE [dbo].[ligne_commande_vente] CHECK CONSTRAINT [FK_lcv_article]
GO

ALTER TABLE [dbo].[bon_livraison] WITH CHECK
    ADD CONSTRAINT [FK_bon_livraison_commande_vente] FOREIGN KEY ([commande_vente_id]) REFERENCES [dbo].[commande_vente] ([id])
GO
ALTER TABLE [dbo].[bon_livraison] CHECK CONSTRAINT [FK_bon_livraison_commande_vente]
GO

ALTER TABLE [dbo].[bon_livraison] WITH CHECK
    ADD CONSTRAINT [FK_bon_livraison_entreprise] FOREIGN KEY ([entreprise_id]) REFERENCES [dbo].[entreprise] ([id])
GO
ALTER TABLE [dbo].[bon_livraison] CHECK CONSTRAINT [FK_bon_livraison_entreprise]
GO

-- Accounting domain
ALTER TABLE [dbo].[depense] WITH CHECK
    ADD CONSTRAINT [FK_depense_categorie_depense] FOREIGN KEY ([categorie_depense_id]) REFERENCES [dbo].[categorie_depense] ([id])
GO
ALTER TABLE [dbo].[depense] CHECK CONSTRAINT [FK_depense_categorie_depense]
GO

ALTER TABLE [dbo].[depense] WITH CHECK
    ADD CONSTRAINT [FK_depense_entreprise] FOREIGN KEY ([entreprise_id]) REFERENCES [dbo].[entreprise] ([id])
GO
ALTER TABLE [dbo].[depense] CHECK CONSTRAINT [FK_depense_entreprise]
GO

ALTER TABLE [dbo].[facture_achat] WITH CHECK
    ADD CONSTRAINT [FK_facture_achat_fournisseur] FOREIGN KEY ([fournisseur_id]) REFERENCES [dbo].[fournisseur] ([id])
GO
ALTER TABLE [dbo].[facture_achat] CHECK CONSTRAINT [FK_facture_achat_fournisseur]
GO

ALTER TABLE [dbo].[facture_achat] WITH CHECK
    ADD CONSTRAINT [FK_facture_achat_commande_achat] FOREIGN KEY ([commande_achat_id]) REFERENCES [dbo].[commande_achat] ([id])
GO
ALTER TABLE [dbo].[facture_achat] CHECK CONSTRAINT [FK_facture_achat_commande_achat]
GO

ALTER TABLE [dbo].[facture_achat] WITH CHECK
    ADD CONSTRAINT [FK_facture_achat_entreprise] FOREIGN KEY ([entreprise_id]) REFERENCES [dbo].[entreprise] ([id])
GO
ALTER TABLE [dbo].[facture_achat] CHECK CONSTRAINT [FK_facture_achat_entreprise]
GO

ALTER TABLE [dbo].[ligne_facture_achat] WITH CHECK
    ADD CONSTRAINT [FK_lfa_facture_achat] FOREIGN KEY ([facture_achat_id]) REFERENCES [dbo].[facture_achat] ([id])
GO
ALTER TABLE [dbo].[ligne_facture_achat] CHECK CONSTRAINT [FK_lfa_facture_achat]
GO

ALTER TABLE [dbo].[ligne_facture_achat] WITH CHECK
    ADD CONSTRAINT [FK_lfa_article] FOREIGN KEY ([article_id]) REFERENCES [dbo].[article] ([id])
GO
ALTER TABLE [dbo].[ligne_facture_achat] CHECK CONSTRAINT [FK_lfa_article]
GO

ALTER TABLE [dbo].[facture_de_vente] WITH CHECK
    ADD CONSTRAINT [FK_facture_de_vente_client] FOREIGN KEY ([client_id]) REFERENCES [dbo].[client] ([id])
GO
ALTER TABLE [dbo].[facture_de_vente] CHECK CONSTRAINT [FK_facture_de_vente_client]
GO

ALTER TABLE [dbo].[facture_de_vente] WITH CHECK
    ADD CONSTRAINT [FK_facture_de_vente_commande_vente] FOREIGN KEY ([commande_vente_id]) REFERENCES [dbo].[commande_vente] ([id])
GO
ALTER TABLE [dbo].[facture_de_vente] CHECK CONSTRAINT [FK_facture_de_vente_commande_vente]
GO

ALTER TABLE [dbo].[facture_de_vente] WITH CHECK
    ADD CONSTRAINT [FK_facture_de_vente_entreprise] FOREIGN KEY ([entreprise_id]) REFERENCES [dbo].[entreprise] ([id])
GO
ALTER TABLE [dbo].[facture_de_vente] CHECK CONSTRAINT [FK_facture_de_vente_entreprise]
GO

ALTER TABLE [dbo].[ligne_facture_vente] WITH CHECK
    ADD CONSTRAINT [FK_lfv_facture_de_vente] FOREIGN KEY ([facture_de_vente_id]) REFERENCES [dbo].[facture_de_vente] ([id])
GO
ALTER TABLE [dbo].[ligne_facture_vente] CHECK CONSTRAINT [FK_lfv_facture_de_vente]
GO

ALTER TABLE [dbo].[ligne_facture_vente] WITH CHECK
    ADD CONSTRAINT [FK_lfv_article] FOREIGN KEY ([article_id]) REFERENCES [dbo].[article] ([id])
GO
ALTER TABLE [dbo].[ligne_facture_vente] CHECK CONSTRAINT [FK_lfv_article]
GO

ALTER TABLE [dbo].[caracteristique_facture_entreprise] WITH CHECK
    ADD CONSTRAINT [FK_car_fact_ent_entreprise] FOREIGN KEY ([entreprise_id]) REFERENCES [dbo].[entreprise] ([id])
GO
ALTER TABLE [dbo].[caracteristique_facture_entreprise] CHECK CONSTRAINT [FK_car_fact_ent_entreprise]
GO

-- Document domain
ALTER TABLE [dbo].[document] WITH CHECK
    ADD CONSTRAINT [FK_document_categorie_document] FOREIGN KEY ([categorie_document_id]) REFERENCES [dbo].[categorie_document] ([id])
GO
ALTER TABLE [dbo].[document] CHECK CONSTRAINT [FK_document_categorie_document]
GO

ALTER TABLE [dbo].[document] WITH CHECK
    ADD CONSTRAINT [FK_document_file_chemin] FOREIGN KEY ([file_chemin_id]) REFERENCES [dbo].[file_chemin] ([id])
GO
ALTER TABLE [dbo].[document] CHECK CONSTRAINT [FK_document_file_chemin]
GO

ALTER TABLE [dbo].[categorie_document_documents] WITH CHECK
    ADD CONSTRAINT [FK_cat_doc_docs_categorie] FOREIGN KEY ([categorie_document_id]) REFERENCES [dbo].[categorie_document] ([id])
GO
ALTER TABLE [dbo].[categorie_document_documents] CHECK CONSTRAINT [FK_cat_doc_docs_categorie]
GO

ALTER TABLE [dbo].[categorie_document_documents] WITH CHECK
    ADD CONSTRAINT [FK_cat_doc_docs_document] FOREIGN KEY ([documents_id]) REFERENCES [dbo].[document] ([id])
GO
ALTER TABLE [dbo].[categorie_document_documents] CHECK CONSTRAINT [FK_cat_doc_docs_document]
GO

-- ============================================================
-- 14. INITIAL DATA
-- ============================================================

-- Entreprise: Symatique
SET IDENTITY_INSERT [dbo].[entreprise] ON
INSERT [dbo].[entreprise] ([id], [nom], [raison_sociale], [adresse], [telephone], [email], [site_web],
    [secteur_activite], [description], [numero_identification_fiscale], [registre_commerce], [statut_juridique],
    [date_creation], [date_derniere_modification], [created_at], [updated_at])
VALUES (1, N'symatique', NULL, N'tn', N'96488540', NULL, NULL, NULL, NULL, NULL, NULL, NULL,
    CAST(N'2024-11-11' AS Date), NULL, GETDATE(), GETDATE())
SET IDENTITY_INSERT [dbo].[entreprise] OFF
GO

-- Admin user (password: admin / BCrypt hash)
SET IDENTITY_INSERT [dbo].[app_user] ON
INSERT [dbo].[app_user] ([id], [email], [username], [password], [enabled], [image_url], [phone],
    [removed_at], [entreprise_id], [created_at], [updated_at])
VALUES (1, N'admin@gmail.com', N'admin',
    N'$2a$10$SJyqX8iXSgQ9gAr8M5V60.fJyX9v2I.X4QDBtjUqJyvJfzjkaQxOG',
    1, NULL, NULL, NULL, 1, GETDATE(), GETDATE())
SET IDENTITY_INSERT [dbo].[app_user] OFF
GO

-- Roles
SET IDENTITY_INSERT [dbo].[roles] ON
INSERT [dbo].[roles] ([id], [name], [created_on], [created_at], [updated_at])
VALUES (1, N'SUPER_ADMIN', CAST(N'2025-08-26' AS Date), GETDATE(), GETDATE())
INSERT [dbo].[roles] ([id], [name], [created_on], [created_at], [updated_at])
VALUES (2, N'ADMIN', CAST(N'2025-08-26' AS Date), GETDATE(), GETDATE())
SET IDENTITY_INSERT [dbo].[roles] OFF
GO

-- User roles
INSERT [dbo].[user_roles] ([user_id], [role_id]) VALUES (1, 1)
GO

-- Module titles (ERP)
INSERT [dbo].[module_tittle] ([id], [module_tittle])
VALUES (1, N'Parametrage')
INSERT [dbo].[module_tittle] ([id], [module_tittle])
VALUES (100, N'gestion de Client')
INSERT [dbo].[module_tittle] ([id], [module_tittle])
VALUES (200, N'gestion de Fournisseur')
INSERT [dbo].[module_tittle] ([id], [module_tittle])
VALUES (300, N'gestion Article')
INSERT [dbo].[module_tittle] ([id], [module_tittle])
VALUES (400, N'gestion Achat')
INSERT [dbo].[module_tittle] ([id], [module_tittle])
VALUES (500, N'gestion Vente')
INSERT [dbo].[module_tittle] ([id], [module_tittle])
VALUES (600, N'gestion Comptabilite')
INSERT [dbo].[module_tittle] ([id], [module_tittle])
VALUES (700, N'gestion Document')
INSERT [dbo].[module_tittle] ([id], [module_tittle])
VALUES (800, N'gestion Module')
GO

-- Modules (ERP)
INSERT [dbo].[module] ([id], [title], [status], [module_tittle_id])
VALUES (1, N'gestion entreprise', 1, 1)
INSERT [dbo].[module] ([id], [title], [status], [module_tittle_id])
VALUES (2, N'gestion users', 1, 1)
INSERT [dbo].[module] ([id], [title], [status], [module_tittle_id])
VALUES (3, N'gestion departement', 1, 1)
INSERT [dbo].[module] ([id], [title], [status], [module_tittle_id])
VALUES (100, N'gestion de Client', 1, 100)
INSERT [dbo].[module] ([id], [title], [status], [module_tittle_id])
VALUES (101, N'gestion de Categorie Client', 1, 100)
INSERT [dbo].[module] ([id], [title], [status], [module_tittle_id])
VALUES (200, N'gestion de Fournisseur', 1, 200)
INSERT [dbo].[module] ([id], [title], [status], [module_tittle_id])
VALUES (201, N'gestion de Categorie Fournisseur', 1, 200)
INSERT [dbo].[module] ([id], [title], [status], [module_tittle_id])
VALUES (300, N'gestion Article Achat', 1, 300)
INSERT [dbo].[module] ([id], [title], [status], [module_tittle_id])
VALUES (301, N'gestion Article Vente', 1, 300)
INSERT [dbo].[module] ([id], [title], [status], [module_tittle_id])
VALUES (302, N'gestion Stock', 1, 300)
INSERT [dbo].[module] ([id], [title], [status], [module_tittle_id])
VALUES (400, N'Commande Achat', 1, 400)
INSERT [dbo].[module] ([id], [title], [status], [module_tittle_id])
VALUES (401, N'Facture Achat', 1, 400)
INSERT [dbo].[module] ([id], [title], [status], [module_tittle_id])
VALUES (402, N'Bon Reception', 1, 400)
INSERT [dbo].[module] ([id], [title], [status], [module_tittle_id])
VALUES (500, N'Commande Vente', 1, 500)
INSERT [dbo].[module] ([id], [title], [status], [module_tittle_id])
VALUES (501, N'Facture Vente', 1, 500)
INSERT [dbo].[module] ([id], [title], [status], [module_tittle_id])
VALUES (502, N'Bon Livraison', 1, 500)
INSERT [dbo].[module] ([id], [title], [status], [module_tittle_id])
VALUES (600, N'Depense', 1, 600)
INSERT [dbo].[module] ([id], [title], [status], [module_tittle_id])
VALUES (601, N'Categorie Depense', 1, 600)
INSERT [dbo].[module] ([id], [title], [status], [module_tittle_id])
VALUES (700, N'Document', 1, 700)
INSERT [dbo].[module] ([id], [title], [status], [module_tittle_id])
VALUES (701, N'Select Zone', 1, 700)
INSERT [dbo].[module] ([id], [title], [status], [module_tittle_id])
VALUES (800, N'Module titre', 1, 800)
INSERT [dbo].[module] ([id], [title], [status], [module_tittle_id])
VALUES (801, N'Module', 1, 800)
INSERT [dbo].[module] ([id], [title], [status], [module_tittle_id])
VALUES (802, N'Sous Module', 1, 800)
GO

-- Sous-modules (ERP navigation links)
INSERT [dbo].[sous_module] ([id], [titre_sous_module], [link], [module_id])
VALUES (1, N'Role & permission', N'user-management/roles-permissions', 2)
INSERT [dbo].[sous_module] ([id], [titre_sous_module], [link], [module_id])
VALUES (2, N'Entreprise', N'entreprise/listEntreprise', 1)
INSERT [dbo].[sous_module] ([id], [titre_sous_module], [link], [module_id])
VALUES (3, N'Departement', N'departement/listDepartement', 3)
INSERT [dbo].[sous_module] ([id], [titre_sous_module], [link], [module_id])
VALUES (4, N'users', N'user-management/users', 2)
INSERT [dbo].[sous_module] ([id], [titre_sous_module], [link], [module_id])
VALUES (100, N'Client', N'client/listClient', 100)
INSERT [dbo].[sous_module] ([id], [titre_sous_module], [link], [module_id])
VALUES (101, N'Categorie Client', N'client/listCategorieClient', 101)
INSERT [dbo].[sous_module] ([id], [titre_sous_module], [link], [module_id])
VALUES (200, N'Fournisseur', N'fournisseur/listFournisseur', 200)
INSERT [dbo].[sous_module] ([id], [titre_sous_module], [link], [module_id])
VALUES (201, N'Categorie Fournisseur', N'fournisseur/listCategorieFournisseur', 201)
INSERT [dbo].[sous_module] ([id], [titre_sous_module], [link], [module_id])
VALUES (300, N'Article Achat', N'article-achat/listArticleAchat', 300)
INSERT [dbo].[sous_module] ([id], [titre_sous_module], [link], [module_id])
VALUES (301, N'Article Vente', N'article-vente/listArticleVente', 301)
INSERT [dbo].[sous_module] ([id], [titre_sous_module], [link], [module_id])
VALUES (302, N'Stock', N'stock/listStockDashboard', 302)
INSERT [dbo].[sous_module] ([id], [titre_sous_module], [link], [module_id])
VALUES (400, N'Commande Achat', N'commande-achat/list', 400)
INSERT [dbo].[sous_module] ([id], [titre_sous_module], [link], [module_id])
VALUES (401, N'Facture Achat', N'facture/listFactureAchat', 401)
INSERT [dbo].[sous_module] ([id], [titre_sous_module], [link], [module_id])
VALUES (402, N'Bon Reception', N'achat/listBonReception', 402)
INSERT [dbo].[sous_module] ([id], [titre_sous_module], [link], [module_id])
VALUES (500, N'Commande Vente', N'commande-vente/list', 500)
INSERT [dbo].[sous_module] ([id], [titre_sous_module], [link], [module_id])
VALUES (501, N'Facture Vente', N'facture/listFactureVente', 501)
INSERT [dbo].[sous_module] ([id], [titre_sous_module], [link], [module_id])
VALUES (502, N'Bon Livraison', N'vente/listBonLivraison', 502)
INSERT [dbo].[sous_module] ([id], [titre_sous_module], [link], [module_id])
VALUES (600, N'Depense', N'comptabilite/listDepense', 600)
INSERT [dbo].[sous_module] ([id], [titre_sous_module], [link], [module_id])
VALUES (601, N'Categorie Depense', N'comptabilite/listCategorieDepense', 601)
INSERT [dbo].[sous_module] ([id], [titre_sous_module], [link], [module_id])
VALUES (700, N'Document', N'documentation/listDocument', 700)
INSERT [dbo].[sous_module] ([id], [titre_sous_module], [link], [module_id])
VALUES (701, N'Select Zone', N'documentation/selectZone', 701)
INSERT [dbo].[sous_module] ([id], [titre_sous_module], [link], [module_id])
VALUES (800, N'Module titre', N'module/listModuleTitre', 800)
INSERT [dbo].[sous_module] ([id], [titre_sous_module], [link], [module_id])
VALUES (801, N'Module', N'module/listModule', 801)
INSERT [dbo].[sous_module] ([id], [titre_sous_module], [link], [module_id])
VALUES (802, N'Sous Module', N'module/listSousModule', 802)
GO

-- Permissions for Super_admin (role_id = 1) - full access to all ERP modules
INSERT [dbo].[permission] ([id], [can_create], [can_delete], [can_update], [can_view], [module_id], [role_id])
VALUES (1, 1, 1, 1, 1, 2, 1)
INSERT [dbo].[permission] ([id], [can_create], [can_delete], [can_update], [can_view], [module_id], [role_id])
VALUES (2, 1, 1, 1, 1, 3, 1)
INSERT [dbo].[permission] ([id], [can_create], [can_delete], [can_update], [can_view], [module_id], [role_id])
VALUES (3, 1, 1, 1, 1, 1, 1)
INSERT [dbo].[permission] ([id], [can_create], [can_delete], [can_update], [can_view], [module_id], [role_id])
VALUES (100, 1, 1, 1, 1, 100, 1)
INSERT [dbo].[permission] ([id], [can_create], [can_delete], [can_update], [can_view], [module_id], [role_id])
VALUES (101, 1, 1, 1, 1, 101, 1)
INSERT [dbo].[permission] ([id], [can_create], [can_delete], [can_update], [can_view], [module_id], [role_id])
VALUES (200, 1, 1, 1, 1, 200, 1)
INSERT [dbo].[permission] ([id], [can_create], [can_delete], [can_update], [can_view], [module_id], [role_id])
VALUES (201, 1, 1, 1, 1, 201, 1)
INSERT [dbo].[permission] ([id], [can_create], [can_delete], [can_update], [can_view], [module_id], [role_id])
VALUES (300, 1, 1, 1, 1, 300, 1)
INSERT [dbo].[permission] ([id], [can_create], [can_delete], [can_update], [can_view], [module_id], [role_id])
VALUES (301, 1, 1, 1, 1, 301, 1)
INSERT [dbo].[permission] ([id], [can_create], [can_delete], [can_update], [can_view], [module_id], [role_id])
VALUES (302, 1, 1, 1, 1, 302, 1)
INSERT [dbo].[permission] ([id], [can_create], [can_delete], [can_update], [can_view], [module_id], [role_id])
VALUES (400, 1, 1, 1, 1, 400, 1)
INSERT [dbo].[permission] ([id], [can_create], [can_delete], [can_update], [can_view], [module_id], [role_id])
VALUES (401, 1, 1, 1, 1, 401, 1)
INSERT [dbo].[permission] ([id], [can_create], [can_delete], [can_update], [can_view], [module_id], [role_id])
VALUES (402, 1, 1, 1, 1, 402, 1)
INSERT [dbo].[permission] ([id], [can_create], [can_delete], [can_update], [can_view], [module_id], [role_id])
VALUES (500, 1, 1, 1, 1, 500, 1)
INSERT [dbo].[permission] ([id], [can_create], [can_delete], [can_update], [can_view], [module_id], [role_id])
VALUES (501, 1, 1, 1, 1, 501, 1)
INSERT [dbo].[permission] ([id], [can_create], [can_delete], [can_update], [can_view], [module_id], [role_id])
VALUES (502, 1, 1, 1, 1, 502, 1)
INSERT [dbo].[permission] ([id], [can_create], [can_delete], [can_update], [can_view], [module_id], [role_id])
VALUES (600, 1, 1, 1, 1, 600, 1)
INSERT [dbo].[permission] ([id], [can_create], [can_delete], [can_update], [can_view], [module_id], [role_id])
VALUES (601, 1, 1, 1, 1, 601, 1)
INSERT [dbo].[permission] ([id], [can_create], [can_delete], [can_update], [can_view], [module_id], [role_id])
VALUES (700, 1, 1, 1, 1, 700, 1)
INSERT [dbo].[permission] ([id], [can_create], [can_delete], [can_update], [can_view], [module_id], [role_id])
VALUES (701, 1, 1, 1, 1, 701, 1)
INSERT [dbo].[permission] ([id], [can_create], [can_delete], [can_update], [can_view], [module_id], [role_id])
VALUES (800, 1, 1, 1, 1, 800, 1)
INSERT [dbo].[permission] ([id], [can_create], [can_delete], [can_update], [can_view], [module_id], [role_id])
VALUES (801, 1, 1, 1, 1, 801, 1)
INSERT [dbo].[permission] ([id], [can_create], [can_delete], [can_update], [can_view], [module_id], [role_id])
VALUES (802, 1, 1, 1, 1, 802, 1)
GO

-- Permissions for ADMIN (role_id = 2) - full CRUD access to client, fournisseur, article modules
INSERT [dbo].[permission] ([id], [can_create], [can_delete], [can_update], [can_view], [module_id], [role_id])
VALUES (1100, 1, 1, 1, 1, 100, 2)
INSERT [dbo].[permission] ([id], [can_create], [can_delete], [can_update], [can_view], [module_id], [role_id])
VALUES (1101, 1, 1, 1, 1, 101, 2)
INSERT [dbo].[permission] ([id], [can_create], [can_delete], [can_update], [can_view], [module_id], [role_id])
VALUES (1200, 1, 1, 1, 1, 200, 2)
INSERT [dbo].[permission] ([id], [can_create], [can_delete], [can_update], [can_view], [module_id], [role_id])
VALUES (1201, 1, 1, 1, 1, 201, 2)
INSERT [dbo].[permission] ([id], [can_create], [can_delete], [can_update], [can_view], [module_id], [role_id])
VALUES (1300, 1, 1, 1, 1, 300, 2)
INSERT [dbo].[permission] ([id], [can_create], [can_delete], [can_update], [can_view], [module_id], [role_id])
VALUES (1301, 1, 1, 1, 1, 301, 2)
GO

-- Document categories
SET IDENTITY_INSERT [dbo].[categorie_document] ON
INSERT [dbo].[categorie_document] ([id], [nom], [description], [created_at], [updated_at])
VALUES (1, N'Contrats', N'Documents relatifs aux contrats commerciaux', GETDATE(), GETDATE())
SET IDENTITY_INSERT [dbo].[categorie_document] OFF
GO

-- File paths
SET IDENTITY_INSERT [dbo].[file_chemin] ON
INSERT [dbo].[file_chemin] ([id], [urlpdf], [created_at], [updated_at])
VALUES (1, N'https://res.cloudinary.com/dpvqw1nd4/raw/upload/v1756217174/h5spawidlh8osgdqweio', GETDATE(), GETDATE())
INSERT [dbo].[file_chemin] ([id], [urlpdf], [created_at], [updated_at])
VALUES (2, N'https://res.cloudinary.com/dpvqw1nd4/raw/upload/v1756217234/djgbulpcstyxgyfefrlu', GETDATE(), GETDATE())
INSERT [dbo].[file_chemin] ([id], [urlpdf], [created_at], [updated_at])
VALUES (3, N'https://res.cloudinary.com/dpvqw1nd4/raw/upload/v1756217366/knk7c5oxsrpoclustuiu', GETDATE(), GETDATE())
INSERT [dbo].[file_chemin] ([id], [urlpdf], [created_at], [updated_at])
VALUES (4, N'https://res.cloudinary.com/dpvqw1nd4/raw/upload/v1756217687/mmcrevttklvmlnpznsyg', GETDATE(), GETDATE())
INSERT [dbo].[file_chemin] ([id], [urlpdf], [created_at], [updated_at])
VALUES (52, N'https://res.cloudinary.com/dpvqw1nd4/raw/upload/v1756217987/h4gc0zl6mswshtjrsvqf', GETDATE(), GETDATE())
INSERT [dbo].[file_chemin] ([id], [urlpdf], [created_at], [updated_at])
VALUES (53, N'https://res.cloudinary.com/dpvqw1nd4/raw/upload/v1756218205/jfobvmz0sa178hhkchcz', GETDATE(), GETDATE())
INSERT [dbo].[file_chemin] ([id], [urlpdf], [created_at], [updated_at])
VALUES (54, N'https://res.cloudinary.com/dpvqw1nd4/raw/upload/v1756218337/f2wv2jhgb79avdnvrdz4', GETDATE(), GETDATE())
INSERT [dbo].[file_chemin] ([id], [urlpdf], [created_at], [updated_at])
VALUES (55, N'https://res.cloudinary.com/dpvqw1nd4/raw/upload/v1756218505/szaqqycua9umqyxnhc2z', GETDATE(), GETDATE())
INSERT [dbo].[file_chemin] ([id], [urlpdf], [created_at], [updated_at])
VALUES (102, N'https://res.cloudinary.com/dpvqw1nd4/raw/upload/v1756218636/lgf0pjr4ks7th9q0ieig', GETDATE(), GETDATE())
SET IDENTITY_INSERT [dbo].[file_chemin] OFF
GO

-- Documents
SET IDENTITY_INSERT [dbo].[document] ON
INSERT [dbo].[document] ([id], [nom], [type], [date_ajout], [categorie_document_id], [file_chemin_id], [created_at], [updated_at])
VALUES (1, N'test', N'facture', CAST(N'2025-08-26T15:06:01.2973530' AS DateTime2), 1, NULL, GETDATE(), GETDATE())
INSERT [dbo].[document] ([id], [nom], [type], [date_ajout], [categorie_document_id], [file_chemin_id], [created_at], [updated_at])
VALUES (2, N'test', N'facture', CAST(N'2025-08-26T15:06:14.6360230' AS DateTime2), 1, 1, GETDATE(), GETDATE())
INSERT [dbo].[document] ([id], [nom], [type], [date_ajout], [categorie_document_id], [file_chemin_id], [created_at], [updated_at])
VALUES (3, N'testss', N'Facture', CAST(N'2025-08-26T15:07:14.4250410' AS DateTime2), 1, 2, GETDATE(), GETDATE())
INSERT [dbo].[document] ([id], [nom], [type], [date_ajout], [categorie_document_id], [file_chemin_id], [created_at], [updated_at])
VALUES (4, N'testdzd', N'facture', CAST(N'2025-08-26T15:09:27.3896270' AS DateTime2), 1, 3, GETDATE(), GETDATE())
INSERT [dbo].[document] ([id], [nom], [type], [date_ajout], [categorie_document_id], [file_chemin_id], [created_at], [updated_at])
VALUES (5, N'testcec', N'FACTURE D''ACHAT', CAST(N'2025-08-26T15:14:46.1697440' AS DateTime2), 1, NULL, GETDATE(), GETDATE())
INSERT [dbo].[document] ([id], [nom], [type], [date_ajout], [categorie_document_id], [file_chemin_id], [created_at], [updated_at])
VALUES (6, N'testsss', N'FACTURE D''ACHAT', CAST(N'2025-08-26T15:19:49.5840280' AS DateTime2), 1, NULL, GETDATE(), GETDATE())
INSERT [dbo].[document] ([id], [nom], [type], [date_ajout], [categorie_document_id], [file_chemin_id], [created_at], [updated_at])
VALUES (7, N'vf', N'FACTURE D''ACHAT', CAST(N'2025-08-26T15:23:55.2210820' AS DateTime2), 1, NULL, GETDATE(), GETDATE())
INSERT [dbo].[document] ([id], [nom], [type], [date_ajout], [categorie_document_id], [file_chemin_id], [created_at], [updated_at])
VALUES (8, N'dzxs', N'FACTURE D''ACHAT', CAST(N'2025-08-26T15:25:43.1889590' AS DateTime2), 1, NULL, GETDATE(), GETDATE())
INSERT [dbo].[document] ([id], [nom], [type], [date_ajout], [categorie_document_id], [file_chemin_id], [created_at], [updated_at])
VALUES (9, N'fc', N'facture', CAST(N'2025-08-26T15:28:08.4293200' AS DateTime2), 1, NULL, GETDATE(), GETDATE())
INSERT [dbo].[document] ([id], [nom], [type], [date_ajout], [categorie_document_id], [file_chemin_id], [created_at], [updated_at])
VALUES (10, N'vvf', N'facture', CAST(N'2025-08-26T15:28:26.3671270' AS DateTime2), 1, NULL, GETDATE(), GETDATE())
INSERT [dbo].[document] ([id], [nom], [type], [date_ajout], [categorie_document_id], [file_chemin_id], [created_at], [updated_at])
VALUES (11, N'testccc', N'FACTURE D''ACHAT', CAST(N'2025-08-26T15:30:37.3571930' AS DateTime2), 1, 102, GETDATE(), GETDATE())
SET IDENTITY_INSERT [dbo].[document] OFF
GO

-- Zone selectionnee de document
SET IDENTITY_INSERT [dbo].[zone_selectionnee_de_document] ON
INSERT [dbo].[zone_selectionnee_de_document] ([id], [canvas_height], [canvas_width], [height], [page_number],
    [pdf_height], [pdf_width], [type], [type_de_document], [width], [x], [y])
VALUES (1, 1262, 892, 27, 1, 841.89, 595.28, N'article', N'FACTURE', 109, 60, 524)
INSERT [dbo].[zone_selectionnee_de_document] ([id], [canvas_height], [canvas_width], [height], [page_number],
    [pdf_height], [pdf_width], [type], [type_de_document], [width], [x], [y])
VALUES (2, 1262, 892, 32, 1, 841.89, 595.28, N'quantite', N'FACTURE', 32, 486, 514)
INSERT [dbo].[zone_selectionnee_de_document] ([id], [canvas_height], [canvas_width], [height], [page_number],
    [pdf_height], [pdf_width], [type], [type_de_document], [width], [x], [y])
VALUES (3, 1262, 892, 27, 1, 841.89, 595.28, N'prixUnitaireHT', N'FACTURE', 54, 618, 520)
INSERT [dbo].[zone_selectionnee_de_document] ([id], [canvas_height], [canvas_width], [height], [page_number],
    [pdf_height], [pdf_width], [type], [type_de_document], [width], [x], [y])
VALUES (4, 1262, 892, 24, 1, 841.89, 595.28, N'montantHT', N'FACTURE', 67, 753, 679)
SET IDENTITY_INSERT [dbo].[zone_selectionnee_de_document] OFF
GO

-- ============================================================
-- 15. SAMPLE STOCK DATA
-- ============================================================

-- MEASUREMENTS
SET IDENTITY_INSERT [dbo].[mesure] ON
INSERT [dbo].[mesure] ([id], [nom], [symbole], [created_at], [updated_at])
VALUES 
  (1, N'Piece', N'Pce', GETDATE(), GETDATE()),
  (2, N'Kilogram', N'Kg', GETDATE(), GETDATE()),
  (3, N'Liter', N'L', GETDATE(), GETDATE()),
  (4, N'Box', N'Box', GETDATE(), GETDATE())
SET IDENTITY_INSERT [dbo].[mesure] OFF
GO

-- VAT RATES
SET IDENTITY_INSERT [dbo].[tva] ON
INSERT [dbo].[tva] ([id], [taux], [nom], [created_at], [updated_at])
VALUES 
  (1, 0.00, N'No VAT', GETDATE(), GETDATE()),
  (2, 19.00, N'Standard VAT', GETDATE(), GETDATE())
SET IDENTITY_INSERT [dbo].[tva] OFF
GO

-- ARTICLE CATEGORIES
SET IDENTITY_INSERT [dbo].[categorie_article] ON
INSERT [dbo].[categorie_article] ([id], [nom], [description], [created_at], [updated_at])
VALUES 
  (1, N'Electronics', N'Electronic equipment and devices', GETDATE(), GETDATE()),
  (2, N'Office Supplies', N'Office equipment and supplies', GETDATE(), GETDATE()),
  (3, N'Groceries', N'Food and beverage products', GETDATE(), GETDATE())
SET IDENTITY_INSERT [dbo].[categorie_article] OFF
GO

-- ARTICLES (Products)
SET IDENTITY_INSERT [dbo].[article] ON
INSERT [dbo].[article] ([id], [reference], [nom], [description], [prix_achat], [prix_vente], [actif], [categorie_article_id], [mesure_id], [tva_id], [entreprise_id], [created_at], [updated_at])
VALUES 
  (1, N'ELEC-001', N'Dell Laptop XPS 13', N'High performance laptop for professionals', 800.00, 1000.00, 1, 1, 1, 2, 1, GETDATE(), GETDATE()),
  (2, N'OFFC-001', N'Ergonomic Office Chair', N'Comfortable office chair with lumbar support', 120.00, 150.00, 1, 2, 1, 2, 1, GETDATE(), GETDATE()),
  (3, N'GROC-001', N'Premium Coffee Beans', N'Arabica coffee beans 1kg bag', 15.00, 25.00, 1, 3, 2, 2, 1, GETDATE(), GETDATE()),
  (4, N'OFFC-002', N'A4 Printer Paper', N'500 sheets per box', 3.50, 8.00, 1, 2, 4, 2, 1, GETDATE(), GETDATE()),
  (5, N'ELEC-002', N'USB-C Cable', N'1.5m USB Type-C cable', 2.00, 5.00, 1, 1, 1, 2, 1, GETDATE(), GETDATE())
SET IDENTITY_INSERT [dbo].[article] OFF
GO

-- ARTICLE VENTE (Sales variants)
SET IDENTITY_INSERT [dbo].[article_vente] ON
INSERT [dbo].[article_vente] ([id], [article_id], [prix_vente_special], [description_vente], [created_at], [updated_at])
VALUES 
  (1, 1, 950.00, N'Special promotional price for bulk orders', GETDATE(), GETDATE()),
  (2, 2, 140.00, N'Discounted for corporate clients', GETDATE(), GETDATE()),
  (3, 3, 22.00, N'Wholesale price', GETDATE(), GETDATE()),
  (4, 4, 7.50, N'Bundle discount', GETDATE(), GETDATE()),
  (5, 5, 4.50, N'Multi-pack discount', GETDATE(), GETDATE())
SET IDENTITY_INSERT [dbo].[article_vente] OFF
GO

-- STOCK INVENTORY
SET IDENTITY_INSERT [dbo].[stock] ON
INSERT [dbo].[stock] ([id], [article_id], [entreprise_id], [quantite_disponible], [quantite_minimale], [quantite_maximale], [emplacement], [created_at], [updated_at])
VALUES 
  (1, 1, 1, 50.000, 10.000, 200.000, N'Warehouse A1', GETDATE(), GETDATE()),
  (2, 2, 1, 100.000, 20.000, 300.000, N'Warehouse A2', GETDATE(), GETDATE()),
  (3, 3, 1, 500.000, 50.000, 1000.000, N'Storage B1', GETDATE(), GETDATE()),
  (4, 4, 1, 1000.000, 100.000, 2000.000, N'Storage B2', GETDATE(), GETDATE()),
  (5, 5, 1, 2000.000, 200.000, 5000.000, N'Storage C1', GETDATE(), GETDATE())
SET IDENTITY_INSERT [dbo].[stock] OFF
GO

-- STOCK MOVEMENTS - Historical transactions
SET IDENTITY_INSERT [dbo].[mouvement_stock] ON

-- Laptop movements
INSERT [dbo].[mouvement_stock] ([id], [article_id], [entreprise_id], [type_mouvement], [quantite], [date_mouvement], [reference_document], [description], [created_at], [updated_at])
VALUES (1, 1, 1, N'ENTREE', 100.000, DATEADD(day, -30, GETDATE()), N'PO-2026-001', N'Initial purchase from supplier', GETDATE(), GETDATE())
INSERT [dbo].[mouvement_stock] ([id], [article_id], [entreprise_id], [type_mouvement], [quantite], [date_mouvement], [reference_document], [description], [created_at], [updated_at])
VALUES (2, 1, 1, N'SORTIE', 25.000, DATEADD(day, -25, GETDATE()), N'SO-2026-001', N'Sales to customer ABC Corp', GETDATE(), GETDATE())
INSERT [dbo].[mouvement_stock] ([id], [article_id], [entreprise_id], [type_mouvement], [quantite], [date_mouvement], [reference_document], [description], [created_at], [updated_at])
VALUES (3, 1, 1, N'SORTIE', 15.000, DATEADD(day, -20, GETDATE()), N'SO-2026-002', N'Sales to customer XYZ Inc', GETDATE(), GETDATE())
INSERT [dbo].[mouvement_stock] ([id], [article_id], [entreprise_id], [type_mouvement], [quantite], [date_mouvement], [reference_document], [description], [created_at], [updated_at])
VALUES (4, 1, 1, N'RESERVATION', 10.000, DATEADD(day, -15, GETDATE()), N'RES-2026-001', N'Reserved for pending order', GETDATE(), GETDATE())
INSERT [dbo].[mouvement_stock] ([id], [article_id], [entreprise_id], [type_mouvement], [quantite], [date_mouvement], [reference_document], [description], [created_at], [updated_at])
VALUES (5, 1, 1, N'AJUSTEMENT', 5.000, DATEADD(day, -5, GETDATE()), N'ADJ-2026-001', N'Inventory count correction', GETDATE(), GETDATE())

-- Office Chair movements
INSERT [dbo].[mouvement_stock] ([id], [article_id], [entreprise_id], [type_mouvement], [quantite], [date_mouvement], [reference_document], [description], [created_at], [updated_at])
VALUES (6, 2, 1, N'ENTREE', 150.000, DATEADD(day, -28, GETDATE()), N'PO-2026-002', N'Bulk order from manufacturer', GETDATE(), GETDATE())
INSERT [dbo].[mouvement_stock] ([id], [article_id], [entreprise_id], [type_mouvement], [quantite], [date_mouvement], [reference_document], [description], [created_at], [updated_at])
VALUES (7, 2, 1, N'SORTIE', 20.000, DATEADD(day, -22, GETDATE()), N'SO-2026-003', N'Office equipment order', GETDATE(), GETDATE())
INSERT [dbo].[mouvement_stock] ([id], [article_id], [entreprise_id], [type_mouvement], [quantite], [date_mouvement], [reference_document], [description], [created_at], [updated_at])
VALUES (8, 2, 1, N'SORTIE', 10.000, DATEADD(day, -18, GETDATE()), N'SO-2026-004', N'Retail store delivery', GETDATE(), GETDATE())
INSERT [dbo].[mouvement_stock] ([id], [article_id], [entreprise_id], [type_mouvement], [quantite], [date_mouvement], [reference_document], [description], [created_at], [updated_at])
VALUES (9, 2, 1, N'RESERVATION', 20.000, DATEADD(day, -12, GETDATE()), N'RES-2026-002', N'Corporate client bulk order', GETDATE(), GETDATE())
INSERT [dbo].[mouvement_stock] ([id], [article_id], [entreprise_id], [type_mouvement], [quantite], [date_mouvement], [reference_document], [description], [created_at], [updated_at])
VALUES (10, 2, 1, N'SORTIE', 0.000, DATEADD(day, -8, GETDATE()), N'SO-2026-005', N'Return from customer', GETDATE(), GETDATE())

-- Coffee Beans movements
INSERT [dbo].[mouvement_stock] ([id], [article_id], [entreprise_id], [type_mouvement], [quantite], [date_mouvement], [reference_document], [description], [created_at], [updated_at])
VALUES (11, 3, 1, N'ENTREE', 750.000, DATEADD(day, -35, GETDATE()), N'PO-2026-003', N'Import from coffee supplier', GETDATE(), GETDATE())
INSERT [dbo].[mouvement_stock] ([id], [article_id], [entreprise_id], [type_mouvement], [quantite], [date_mouvement], [reference_document], [description], [created_at], [updated_at])
VALUES (12, 3, 1, N'SORTIE', 100.000, DATEADD(day, -30, GETDATE()), N'SO-2026-006', N'Wholesale to cafe chain', GETDATE(), GETDATE())
INSERT [dbo].[mouvement_stock] ([id], [article_id], [entreprise_id], [type_mouvement], [quantite], [date_mouvement], [reference_document], [description], [created_at], [updated_at])
VALUES (13, 3, 1, N'SORTIE', 75.000, DATEADD(day, -24, GETDATE()), N'SO-2026-007', N'Retail store sales', GETDATE(), GETDATE())
INSERT [dbo].[mouvement_stock] ([id], [article_id], [entreprise_id], [type_mouvement], [quantite], [date_mouvement], [reference_document], [description], [created_at], [updated_at])
VALUES (14, 3, 1, N'SORTIE', 25.000, DATEADD(day, -16, GETDATE()), N'SO-2026-008', N'Online orders', GETDATE(), GETDATE())
INSERT [dbo].[mouvement_stock] ([id], [article_id], [entreprise_id], [type_mouvement], [quantite], [date_mouvement], [reference_document], [description], [created_at], [updated_at])
VALUES (15, 3, 1, N'RESERVATION', 50.000, DATEADD(day, -10, GETDATE()), N'RES-2026-003', N'Standing order from restaurant', GETDATE(), GETDATE())

-- Printer Paper movements
INSERT [dbo].[mouvement_stock] ([id], [article_id], [entreprise_id], [type_mouvement], [quantite], [date_mouvement], [reference_document], [description], [created_at], [updated_at])
VALUES (16, 4, 1, N'ENTREE', 1500.000, DATEADD(day, -32, GETDATE()), N'PO-2026-004', N'Bulk purchase from distributor', GETDATE(), GETDATE())
INSERT [dbo].[mouvement_stock] ([id], [article_id], [entreprise_id], [type_mouvement], [quantite], [date_mouvement], [reference_document], [description], [created_at], [updated_at])
VALUES (17, 4, 1, N'SORTIE', 200.000, DATEADD(day, -28, GETDATE()), N'SO-2026-009', N'Office supply order', GETDATE(), GETDATE())
INSERT [dbo].[mouvement_stock] ([id], [article_id], [entreprise_id], [type_mouvement], [quantite], [date_mouvement], [reference_document], [description], [created_at], [updated_at])
VALUES (18, 4, 1, N'SORTIE', 150.000, DATEADD(day, -20, GETDATE()), N'SO-2026-010', N'Corporate order fulfillment', GETDATE(), GETDATE())
INSERT [dbo].[mouvement_stock] ([id], [article_id], [entreprise_id], [type_mouvement], [quantite], [date_mouvement], [reference_document], [description], [created_at], [updated_at])
VALUES (19, 4, 1, N'SORTIE', 100.000, DATEADD(day, -14, GETDATE()), N'SO-2026-011', N'Reseller shipment', GETDATE(), GETDATE())
INSERT [dbo].[mouvement_stock] ([id], [article_id], [entreprise_id], [type_mouvement], [quantite], [date_mouvement], [reference_document], [description], [created_at], [updated_at])
VALUES (20, 4, 1, N'RESERVATION', 100.000, DATEADD(day, -7, GETDATE()), N'RES-2026-004', N'Scheduled delivery next week', GETDATE(), GETDATE())

-- USB Cables movements
INSERT [dbo].[mouvement_stock] ([id], [article_id], [entreprise_id], [type_mouvement], [quantite], [date_mouvement], [reference_document], [description], [created_at], [updated_at])
VALUES (21, 5, 1, N'ENTREE', 3000.000, DATEADD(day, -40, GETDATE()), N'PO-2026-005', N'Large shipment from electronics supplier', GETDATE(), GETDATE())
INSERT [dbo].[mouvement_stock] ([id], [article_id], [entreprise_id], [type_mouvement], [quantite], [date_mouvement], [reference_document], [description], [created_at], [updated_at])
VALUES (22, 5, 1, N'SORTIE', 400.000, DATEADD(day, -35, GETDATE()), N'SO-2026-012', N'Tech store order', GETDATE(), GETDATE())
INSERT [dbo].[mouvement_stock] ([id], [article_id], [entreprise_id], [type_mouvement], [quantite], [date_mouvement], [reference_document], [description], [created_at], [updated_at])
VALUES (23, 5, 1, N'SORTIE', 250.000, DATEADD(day, -28, GETDATE()), N'SO-2026-013', N'Online retailer shipment', GETDATE(), GETDATE())
INSERT [dbo].[mouvement_stock] ([id], [article_id], [entreprise_id], [type_mouvement], [quantite], [date_mouvement], [reference_document], [description], [created_at], [updated_at])
VALUES (24, 5, 1, N'SORTIE', 150.000, DATEADD(day, -19, GETDATE()), N'SO-2026-014', N'Electronics bundle kit sales', GETDATE(), GETDATE())
INSERT [dbo].[mouvement_stock] ([id], [article_id], [entreprise_id], [type_mouvement], [quantite], [date_mouvement], [reference_document], [description], [created_at], [updated_at])
VALUES (25, 5, 1, N'RESERVATION', 200.000, DATEADD(day, -6, GETDATE()), N'RES-2026-005', N'Bulk corporate tech order', GETDATE(), GETDATE())

SET IDENTITY_INSERT [dbo].[mouvement_stock] OFF
GO

-- ============================================================
-- 16. RESTORE DATABASE
-- ============================================================

USE [master]
GO
ALTER DATABASE [BusinessManager] SET READ_WRITE
GO
