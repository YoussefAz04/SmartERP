-- ============================================================
-- ADMIN PERMISSIONS FIX SCRIPT
-- Purpose: Fix missing CRUD permissions for ADMIN role
-- Database: BusinessManager
-- Safe to run multiple times (idempotent)
-- ============================================================

USE [BusinessManager]
GO

-- ============================================================
-- Step 1: Fix role names and ensure ADMIN role exists
-- ============================================================

-- Fix Super_admin -> SUPER_ADMIN if needed
UPDATE [dbo].[roles] SET [name] = N'SUPER_ADMIN' WHERE [name] = N'Super_admin'
GO

-- Add ADMIN role if it does not already exist
IF NOT EXISTS (SELECT 1 FROM [dbo].[roles] WHERE [name] = N'ADMIN')
BEGIN
    INSERT [dbo].[roles] ([name], [created_on], [created_at], [updated_at])
    VALUES (N'ADMIN', CAST(GETDATE() AS Date), GETDATE(), GETDATE())
    PRINT '✓ ADMIN role created'
END
ELSE
BEGIN
    PRINT '✓ ADMIN role already exists'
END
GO

-- ============================================================
-- Step 2: Fix module_tittle titles (delete modules first to
--         respect FK_module_module_tittle constraint)
-- ============================================================

-- Remove modules that reference the module_tittles we need to fix
DELETE FROM [dbo].[permission] WHERE [module_id] IN (100, 101, 200, 201, 300, 301)
DELETE FROM [dbo].[sous_module] WHERE [module_id] IN (100, 101, 200, 201, 300, 301)
DELETE FROM [dbo].[module] WHERE [id] IN (100, 101, 200, 201, 300, 301)
DELETE FROM [dbo].[module_tittle] WHERE [id] IN (100, 200, 300)
PRINT '✓ Cleaned up old module_tittles and modules'
GO

-- Insert correct module_tittle entries (no SET IDENTITY_INSERT needed — not an identity column)
INSERT [dbo].[module_tittle] ([id], [module_tittle]) VALUES (100, N'gestion de Client')
INSERT [dbo].[module_tittle] ([id], [module_tittle]) VALUES (200, N'gestion de Fournisseur')
INSERT [dbo].[module_tittle] ([id], [module_tittle]) VALUES (300, N'gestion Article')
PRINT '✓ Module titles inserted'
GO

-- ============================================================
-- Step 3: Re-insert modules
-- ============================================================

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
PRINT '✓ Modules re-inserted'
GO

-- ============================================================
-- Step 4: Re-insert sous_modules
-- ============================================================

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
PRINT '✓ Sous-modules re-inserted'
GO

-- ============================================================
-- Step 5: Re-insert SUPER_ADMIN permissions for these modules
-- ============================================================

DECLARE @SuperAdminRoleId BIGINT
SELECT @SuperAdminRoleId = [id] FROM [dbo].[roles] WHERE [name] = N'SUPER_ADMIN'

IF @SuperAdminRoleId IS NOT NULL
BEGIN
    -- Use IDs that won't conflict with existing ones
    IF NOT EXISTS (SELECT 1 FROM [dbo].[permission] WHERE [module_id] = 100 AND [role_id] = @SuperAdminRoleId)
        INSERT [dbo].[permission] ([id], [can_create], [can_delete], [can_update], [can_view], [module_id], [role_id])
        VALUES (100, 1, 1, 1, 1, 100, @SuperAdminRoleId)
    IF NOT EXISTS (SELECT 1 FROM [dbo].[permission] WHERE [module_id] = 101 AND [role_id] = @SuperAdminRoleId)
        INSERT [dbo].[permission] ([id], [can_create], [can_delete], [can_update], [can_view], [module_id], [role_id])
        VALUES (101, 1, 1, 1, 1, 101, @SuperAdminRoleId)
    IF NOT EXISTS (SELECT 1 FROM [dbo].[permission] WHERE [module_id] = 200 AND [role_id] = @SuperAdminRoleId)
        INSERT [dbo].[permission] ([id], [can_create], [can_delete], [can_update], [can_view], [module_id], [role_id])
        VALUES (200, 1, 1, 1, 1, 200, @SuperAdminRoleId)
    IF NOT EXISTS (SELECT 1 FROM [dbo].[permission] WHERE [module_id] = 201 AND [role_id] = @SuperAdminRoleId)
        INSERT [dbo].[permission] ([id], [can_create], [can_delete], [can_update], [can_view], [module_id], [role_id])
        VALUES (201, 1, 1, 1, 1, 201, @SuperAdminRoleId)
    IF NOT EXISTS (SELECT 1 FROM [dbo].[permission] WHERE [module_id] = 300 AND [role_id] = @SuperAdminRoleId)
        INSERT [dbo].[permission] ([id], [can_create], [can_delete], [can_update], [can_view], [module_id], [role_id])
        VALUES (300, 1, 1, 1, 1, 300, @SuperAdminRoleId)
    IF NOT EXISTS (SELECT 1 FROM [dbo].[permission] WHERE [module_id] = 301 AND [role_id] = @SuperAdminRoleId)
        INSERT [dbo].[permission] ([id], [can_create], [can_delete], [can_update], [can_view], [module_id], [role_id])
        VALUES (301, 1, 1, 1, 1, 301, @SuperAdminRoleId)
    PRINT '✓ SUPER_ADMIN permissions restored'
END
GO

-- ============================================================
-- Step 6: Create CRUD permissions for ADMIN role
-- ============================================================

DECLARE @AdminRoleId BIGINT
SELECT @AdminRoleId = [id] FROM [dbo].[roles] WHERE [name] = N'ADMIN'

IF @AdminRoleId IS NOT NULL
BEGIN
    -- Use IDs in the 1100-1301 range (well above existing SUPER_ADMIN permission IDs which max at 802)
    IF NOT EXISTS (SELECT 1 FROM [dbo].[permission] WHERE [module_id] = 100 AND [role_id] = @AdminRoleId)
        INSERT [dbo].[permission] ([id], [can_create], [can_delete], [can_update], [can_view], [module_id], [role_id])
        VALUES (1100, 1, 1, 1, 1, 100, @AdminRoleId)
    IF NOT EXISTS (SELECT 1 FROM [dbo].[permission] WHERE [module_id] = 101 AND [role_id] = @AdminRoleId)
        INSERT [dbo].[permission] ([id], [can_create], [can_delete], [can_update], [can_view], [module_id], [role_id])
        VALUES (1101, 1, 1, 1, 1, 101, @AdminRoleId)
    IF NOT EXISTS (SELECT 1 FROM [dbo].[permission] WHERE [module_id] = 200 AND [role_id] = @AdminRoleId)
        INSERT [dbo].[permission] ([id], [can_create], [can_delete], [can_update], [can_view], [module_id], [role_id])
        VALUES (1200, 1, 1, 1, 1, 200, @AdminRoleId)
    IF NOT EXISTS (SELECT 1 FROM [dbo].[permission] WHERE [module_id] = 201 AND [role_id] = @AdminRoleId)
        INSERT [dbo].[permission] ([id], [can_create], [can_delete], [can_update], [can_view], [module_id], [role_id])
        VALUES (1201, 1, 1, 1, 1, 201, @AdminRoleId)
    IF NOT EXISTS (SELECT 1 FROM [dbo].[permission] WHERE [module_id] = 300 AND [role_id] = @AdminRoleId)
        INSERT [dbo].[permission] ([id], [can_create], [can_delete], [can_update], [can_view], [module_id], [role_id])
        VALUES (1300, 1, 1, 1, 1, 300, @AdminRoleId)
    IF NOT EXISTS (SELECT 1 FROM [dbo].[permission] WHERE [module_id] = 301 AND [role_id] = @AdminRoleId)
        INSERT [dbo].[permission] ([id], [can_create], [can_delete], [can_update], [can_view], [module_id], [role_id])
        VALUES (1301, 1, 1, 1, 1, 301, @AdminRoleId)
    PRINT '✓ ADMIN permissions created (6 modules with full CRUD)'
END
ELSE
BEGIN
    PRINT '✗ ERROR: ADMIN role not found — check Step 1'
END
GO

-- ============================================================
-- Step 7: Verification queries
-- ============================================================

PRINT ''
PRINT '========== VERIFICATION =========='
PRINT ''
PRINT 'Roles:'
SELECT [id], [name] FROM [dbo].[roles] WHERE [name] IN (N'ADMIN', N'SUPER_ADMIN')
GO

PRINT 'Module Titles (100, 200, 300):'
SELECT [id], [module_tittle] FROM [dbo].[module_tittle] WHERE [id] IN (100, 200, 300)
GO

PRINT 'Modules (100-301):'
SELECT [id], [title] FROM [dbo].[module] WHERE [id] IN (100, 101, 200, 201, 300, 301)
GO

PRINT 'ADMIN Permissions:'
SELECT
    m.[id]           AS [ModuleId],
    m.[title]        AS [Module],
    p.[can_create]   AS [Create],
    p.[can_view]     AS [View],
    p.[can_update]   AS [Update],
    p.[can_delete]   AS [Delete]
FROM [dbo].[permission] p
JOIN [dbo].[module] m  ON p.[module_id] = m.[id]
JOIN [dbo].[roles]  r  ON p.[role_id]  = r.[id]
WHERE r.[name] = N'ADMIN'
ORDER BY m.[id]
GO

PRINT ''
PRINT '✓ Fix complete!'
PRINT 'Next steps:'
PRINT '  1. Open browser DevTools (F12) → Console → run: localStorage.clear()'
PRINT '  2. Log out and log back in as an ADMIN user'
PRINT '  3. Verify CRUD buttons are visible on Clients, Fournisseurs, Article pages'
PRINT ''
