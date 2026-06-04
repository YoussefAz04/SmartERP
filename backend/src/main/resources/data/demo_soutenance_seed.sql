-- ============================================================
-- Demo soutenance seed - SQL Server
-- Compatible with the Spring Boot backend entities.
--
-- Login:
--   username: admin.soutenance
--   password: 1234
--
-- Important:
--   Run the backend once before executing this script so Hibernate can create
--   or update the schema columns used by the current entities.
-- ============================================================

SET QUOTED_IDENTIFIER ON;
SET ANSI_NULLS ON;
SET XACT_ABORT ON;
SET NOCOUNT ON;

-- If a previous failed execution left IDENTITY_INSERT enabled in this SSMS
-- session, reset it before starting. SQL Server allows only one table with
-- IDENTITY_INSERT ON per session.
IF OBJECT_ID('mesure', 'U') IS NOT NULL SET IDENTITY_INSERT mesure OFF;
IF OBJECT_ID('entreprise', 'U') IS NOT NULL SET IDENTITY_INSERT entreprise OFF;
IF OBJECT_ID('App_User', 'U') IS NOT NULL SET IDENTITY_INSERT App_User OFF;
IF OBJECT_ID('categorie_article', 'U') IS NOT NULL SET IDENTITY_INSERT categorie_article OFF;
IF OBJECT_ID('categorie_fournisseur', 'U') IS NOT NULL SET IDENTITY_INSERT categorie_fournisseur OFF;
IF OBJECT_ID('fournisseur', 'U') IS NOT NULL SET IDENTITY_INSERT fournisseur OFF;
IF OBJECT_ID('categorie_client', 'U') IS NOT NULL SET IDENTITY_INSERT categorie_client OFF;
IF OBJECT_ID('client', 'U') IS NOT NULL SET IDENTITY_INSERT client OFF;
IF OBJECT_ID('article', 'U') IS NOT NULL SET IDENTITY_INSERT article OFF;
IF OBJECT_ID('stock', 'U') IS NOT NULL SET IDENTITY_INSERT stock OFF;
IF OBJECT_ID('article_vente', 'U') IS NOT NULL SET IDENTITY_INSERT article_vente OFF;
IF OBJECT_ID('commande_achat', 'U') IS NOT NULL SET IDENTITY_INSERT commande_achat OFF;
IF OBJECT_ID('ligne_commande_achat', 'U') IS NOT NULL SET IDENTITY_INSERT ligne_commande_achat OFF;
IF OBJECT_ID('commande_vente', 'U') IS NOT NULL SET IDENTITY_INSERT commande_vente OFF;
IF OBJECT_ID('ligne_commande_vente', 'U') IS NOT NULL SET IDENTITY_INSERT ligne_commande_vente OFF;
IF OBJECT_ID('facture_achat', 'U') IS NOT NULL SET IDENTITY_INSERT facture_achat OFF;
IF OBJECT_ID('facture_de_vente', 'U') IS NOT NULL SET IDENTITY_INSERT facture_de_vente OFF;
IF OBJECT_ID('stock_mouvement', 'U') IS NOT NULL SET IDENTITY_INSERT stock_mouvement OFF;

BEGIN TRY
    BEGIN TRANSACTION;

    DECLARE @entId BIGINT = 9901;
    DECLARE @adminUserId BIGINT = 9901;
    DECLARE @adminRoleId BIGINT;
    DECLARE @now DATETIME2(6) = SYSDATETIME();

    -- ------------------------------------------------------------
    -- Cleanup: delete only the demo soutenance dataset.
    -- ------------------------------------------------------------
    IF OBJECT_ID('prediction_log', 'U') IS NOT NULL
        DELETE FROM prediction_log WHERE entreprise_id = @entId;

    IF OBJECT_ID('audit_log', 'U') IS NOT NULL
        DELETE FROM audit_log WHERE entreprise_id = @entId;

    IF OBJECT_ID('notification', 'U') IS NOT NULL
        DELETE FROM notification WHERE entreprise_id = @entId;

    IF OBJECT_ID('ligne_facture_vente', 'U') IS NOT NULL
        DELETE FROM ligne_facture_vente WHERE facture_de_vente_id BETWEEN 9901 AND 9999;

    IF OBJECT_ID('ligne_facture_achat', 'U') IS NOT NULL
        DELETE FROM ligne_facture_achat WHERE facture_achat_id BETWEEN 9901 AND 9999;

    IF OBJECT_ID('DetaisFactureDeVente', 'U') IS NOT NULL
        DELETE FROM DetaisFactureDeVente WHERE id BETWEEN 9901 AND 9999;

    IF OBJECT_ID('DetaisFactureAchat', 'U') IS NOT NULL
        DELETE FROM DetaisFactureAchat WHERE id BETWEEN 9901 AND 9999;

    IF OBJECT_ID('facture_de_vente', 'U') IS NOT NULL
        DELETE FROM facture_de_vente
        WHERE entreprise_id = @entId
           OR id BETWEEN 9901 AND 9999
           OR referance LIKE 'FV-DEMO-%';

    IF OBJECT_ID('facture_achat', 'U') IS NOT NULL
        DELETE FROM facture_achat
        WHERE entreprise_id = @entId
           OR id BETWEEN 9901 AND 9999
           OR referance LIKE 'FA-DEMO-%';

    IF OBJECT_ID('ligne_commande_vente', 'U') IS NOT NULL
        DELETE FROM ligne_commande_vente WHERE commande_vente_id BETWEEN 9901 AND 9999;

    IF OBJECT_ID('ligne_commande_achat', 'U') IS NOT NULL
        DELETE FROM ligne_commande_achat WHERE commande_achat_id BETWEEN 9901 AND 9999;

    IF OBJECT_ID('bon_livraison', 'U') IS NOT NULL
        DELETE FROM bon_livraison WHERE entreprise_id = @entId OR commande_vente_id BETWEEN 9901 AND 9999;

    IF OBJECT_ID('bon_reception', 'U') IS NOT NULL
        DELETE FROM bon_reception WHERE entreprise_id = @entId OR commande_achat_id BETWEEN 9901 AND 9999;

    IF OBJECT_ID('commande_vente', 'U') IS NOT NULL
        DELETE FROM commande_vente
        WHERE entreprise_id = @entId
           OR id BETWEEN 9901 AND 9999
           OR reference LIKE 'CV-DEMO-%';

    IF OBJECT_ID('commande_achat', 'U') IS NOT NULL
        DELETE FROM commande_achat
        WHERE entreprise_id = @entId
           OR id BETWEEN 9901 AND 9999
           OR reference LIKE 'CA-DEMO-%';

    IF OBJECT_ID('stock_mouvement', 'U') IS NOT NULL
        DELETE FROM stock_mouvement WHERE stock_id BETWEEN 9901 AND 9920 OR utilisateur_id = @adminUserId;

    IF OBJECT_ID('article_vente', 'U') IS NOT NULL
        DELETE FROM article_vente
        WHERE id BETWEEN 9901 AND 9920
           OR article_achat_id BETWEEN 9901 AND 9920
           OR stock_id BETWEEN 9901 AND 9920
           OR sku LIKE 'SKU-DEMO-%';

    IF OBJECT_ID('stock', 'U') IS NOT NULL
        DELETE FROM stock WHERE entreprise_id = @entId OR id BETWEEN 9901 AND 9920;

    IF OBJECT_ID('article', 'U') IS NOT NULL
        DELETE FROM article
        WHERE entreprise_id = @entId
           OR id BETWEEN 9901 AND 9920
           OR reference LIKE 'DEMO-ACH-%'
           OR sku LIKE 'SKU-DEMO-%';

    IF OBJECT_ID('client_entreprises', 'U') IS NOT NULL
        DELETE FROM client_entreprises
        WHERE entreprises_id = @entId OR client_id BETWEEN 9901 AND 9905;

    IF OBJECT_ID('client', 'U') IS NOT NULL
        DELETE FROM client WHERE entreprise_id = @entId OR id BETWEEN 9901 AND 9905;

    IF OBJECT_ID('fournisseur', 'U') IS NOT NULL
        DELETE FROM fournisseur WHERE entreprise_id = @entId OR id BETWEEN 9901 AND 9904;

    IF OBJECT_ID('categorie_client', 'U') IS NOT NULL
        DELETE FROM categorie_client
        WHERE id BETWEEN 9901 AND 9902
           OR nom IN ('Demo Client Professionnel Soutenance', 'Demo Client Retail Soutenance');

    IF OBJECT_ID('categorie_fournisseur', 'U') IS NOT NULL
        DELETE FROM categorie_fournisseur
        WHERE id BETWEEN 9901 AND 9902
           OR nom IN ('Demo Grossiste Informatique Soutenance', 'Demo Fournitures Pro Soutenance');

    IF OBJECT_ID('mesure', 'U') IS NOT NULL
        DELETE FROM mesure
        WHERE entreprise_id = @entId
           OR id BETWEEN 9901 AND 9903
           OR code IN ('PCE', 'CTN', 'PK') AND entreprise_id = @entId;

    IF OBJECT_ID('categorie_article', 'U') IS NOT NULL
        DELETE FROM categorie_article
        WHERE entreprise_id = @entId
           OR id BETWEEN 9901 AND 9904
           OR code IN ('INFO', 'IMPR', 'BUREAU', 'CAISSE') AND entreprise_id = @entId;

    IF OBJECT_ID('refresh_tokens', 'U') IS NOT NULL
        DELETE FROM refresh_tokens WHERE user_id = @adminUserId;

    IF OBJECT_ID('user_roles', 'U') IS NOT NULL
        DELETE FROM user_roles
        WHERE user_id = @adminUserId
           OR user_id IN (
                SELECT id FROM App_User
                WHERE username = 'admin.soutenance'
                   OR email = 'admin.soutenance@demo.tn'
                   OR entreprise_id = @entId
           );

    IF OBJECT_ID('permission', 'U') IS NOT NULL
        DELETE FROM permission WHERE user_id = @adminUserId;

    IF OBJECT_ID('App_User', 'U') IS NOT NULL
        DELETE FROM App_User
        WHERE id = @adminUserId
           OR username = 'admin.soutenance'
           OR email = 'admin.soutenance@demo.tn'
           OR entreprise_id = @entId;

    IF OBJECT_ID('entreprise', 'U') IS NOT NULL
        DELETE FROM entreprise WHERE id = @entId OR nom = 'Entreprise Demo Soutenance';

    -- ------------------------------------------------------------
    -- Role ADMIN required by JWT / @PreAuthorize("hasRole('ADMIN')").
    -- ------------------------------------------------------------
    IF NOT EXISTS (SELECT 1 FROM roles WHERE UPPER(name) = 'ADMIN')
    BEGIN
        INSERT INTO roles (name, created_on, role_type)
        VALUES ('ADMIN', CAST(GETDATE() AS DATE), 'ADMIN');
    END
    ELSE
    BEGIN
        UPDATE roles SET role_type = 'ADMIN'
        WHERE UPPER(name) = 'ADMIN' AND (role_type IS NULL OR role_type <> 'ADMIN');
    END

    SELECT TOP 1 @adminRoleId = id
    FROM roles
    WHERE UPPER(name) = 'ADMIN'
    ORDER BY CASE WHEN name = 'ADMIN' THEN 0 ELSE 1 END, id;

    -- ------------------------------------------------------------
    -- Enterprise and admin user.
    -- ------------------------------------------------------------
    SET IDENTITY_INSERT entreprise ON;
    INSERT INTO entreprise (
        id, nom, raison_sociale, description, secteur_activite, statut_juridique,
        numero_identification_fiscale, registre_commerce, site_web, date_creation,
        date_derniere_modification, email, adresse, telephone, active, created_at, updated_at
    )
    VALUES (
        @entId, 'Entreprise Demo Soutenance', 'Entreprise Demo Soutenance SARL',
        'Entreprise de demonstration pour la soutenance technique Business Manager.',
        'Distribution informatique et fournitures professionnelles', 'SARL',
        '9901990/A/M/000', 'B99019902026', 'https://demo-soutenance.local',
        '2021-03-15', @now, 'contact@demo-soutenance.tn',
        '12 Avenue Habib Bourguiba, Tunis 1001', '+216 71 990 901',
        1, @now, @now
    );
    SET IDENTITY_INSERT entreprise OFF;

    SET IDENTITY_INSERT App_User ON;
    INSERT INTO App_User (
        id, email, username, password, enabled, image_url, phone,
        entreprise_id, created_at, updated_at
    )
    VALUES (
        @adminUserId, 'admin.soutenance@demo.tn', 'admin.soutenance', '1234',
        1, '', '+216 55 123 400', @entId, @now, @now
    );
    SET IDENTITY_INSERT App_User OFF;

    INSERT INTO user_roles (user_id, role_id)
    VALUES (@adminUserId, @adminRoleId);

    -- ------------------------------------------------------------
    -- Referential data.
    -- ------------------------------------------------------------
    SET IDENTITY_INSERT categorie_article ON;
    INSERT INTO categorie_article (id, code, libelle, description, created_at, updated_at, entreprise_id) VALUES
        (9901, 'INFO', 'Informatique', 'Ordinateurs, reseau et accessoires', @now, @now, @entId),
        (9902, 'IMPR', 'Impression', 'Imprimantes, toners et scanners', @now, @now, @entId),
        (9903, 'BUREAU', 'Fournitures bureau', 'Papeterie et consommables', @now, @now, @entId),
        (9904, 'CAISSE', 'Equipement point de vente', 'Caisse, onduleur et materiel magasin', @now, @now, @entId);
    SET IDENTITY_INSERT categorie_article OFF;

    SET IDENTITY_INSERT mesure ON;
    IF COL_LENGTH('mesure', 'nom') IS NOT NULL
       AND COL_LENGTH('mesure', 'symbole') IS NOT NULL
       AND COL_LENGTH('mesure', 'code') IS NOT NULL
       AND COL_LENGTH('mesure', 'libelle') IS NOT NULL
    BEGIN
        EXEC sp_executesql N'
            INSERT INTO mesure (id, nom, symbole, code, libelle, created_at, updated_at, entreprise_id) VALUES
                (9901, ''Piece'', ''PCE'', ''PCE'', ''Piece'', @pNow, @pNow, @pEntId),
                (9902, ''Carton'', ''CTN'', ''CTN'', ''Carton'', @pNow, @pNow, @pEntId),
                (9903, ''Pack'', ''PK'', ''PK'', ''Pack'', @pNow, @pNow, @pEntId);',
            N'@pNow DATETIME2(6), @pEntId BIGINT',
            @pNow = @now,
            @pEntId = @entId;
    END
    ELSE IF COL_LENGTH('mesure', 'nom') IS NOT NULL
       AND COL_LENGTH('mesure', 'symbole') IS NOT NULL
    BEGIN
        EXEC sp_executesql N'
            INSERT INTO mesure (id, nom, symbole, created_at, updated_at, entreprise_id) VALUES
                (9901, ''Piece'', ''PCE'', @pNow, @pNow, @pEntId),
                (9902, ''Carton'', ''CTN'', @pNow, @pNow, @pEntId),
                (9903, ''Pack'', ''PK'', @pNow, @pNow, @pEntId);',
            N'@pNow DATETIME2(6), @pEntId BIGINT',
            @pNow = @now,
            @pEntId = @entId;
    END
    ELSE
    BEGIN
        EXEC sp_executesql N'
            INSERT INTO mesure (id, code, libelle, created_at, updated_at, entreprise_id) VALUES
                (9901, ''PCE'', ''Piece'', @pNow, @pNow, @pEntId),
                (9902, ''CTN'', ''Carton'', @pNow, @pNow, @pEntId),
                (9903, ''PK'', ''Pack'', @pNow, @pNow, @pEntId);',
            N'@pNow DATETIME2(6), @pEntId BIGINT',
            @pNow = @now,
            @pEntId = @entId;
    END
    SET IDENTITY_INSERT mesure OFF;

    SET IDENTITY_INSERT categorie_fournisseur ON;
    INSERT INTO categorie_fournisseur (id, nom, description, created_at, updated_at) VALUES
        (9901, 'Demo Grossiste Informatique Soutenance', 'Fournisseurs de materiel informatique et reseau', @now, @now),
        (9902, 'Demo Fournitures Pro Soutenance', 'Fournisseurs bureau, impression et consommables', @now, @now);
    SET IDENTITY_INSERT categorie_fournisseur OFF;

    SET IDENTITY_INSERT fournisseur ON;
    INSERT INTO fournisseur (
        id, nom, prenom, email, telephone, adresse, secteur_activite,
        taux_livraison, notation, ville, pays, code_postal,
        numero_identification_fiscale, registre_commerce,
        created_at, updated_at, categorie_fournisseur_id, entreprise_id
    ) VALUES
        (9901, 'Techno Distribution TN', '', 'contact@techno-distribution.tn', '+216 71 220 110',
         'Zone industrielle La Charguia 2', 'Distribution informatique', 0.96, 4.7,
         'Tunis', 'Tunisie', '2035', '1122334/A/M/000', 'B1122332021',
         @now, @now, 9901, @entId),
        (9902, 'Sfax Office Supply', '', 'commercial@sfax-office.tn', '+216 74 411 900',
         'Route de Tunis km 5', 'Fournitures bureau', 0.91, 4.2,
         'Sfax', 'Tunisie', '3000', '5566778/A/M/000', 'B5566772020',
         @now, @now, 9902, @entId),
        (9903, 'Nabeul Impression Pro', '', 'sales@nabeul-impression.tn', '+216 72 280 640',
         'Avenue Habib Thameur', 'Impression et consommables', 0.88, 4.0,
         'Nabeul', 'Tunisie', '8000', '9988776/A/M/000', 'B9988772022',
         @now, @now, 9902, @entId),
        (9904, 'Carthage Equipement', '', 'contact@carthage-equipement.tn', '+216 71 700 880',
         'Rue du Lac Biwa, Les Berges du Lac', 'Equipement magasins', 0.94, 4.5,
         'Tunis', 'Tunisie', '1053', '4455667/A/M/000', 'B4455662023',
         @now, @now, 9901, @entId);
    SET IDENTITY_INSERT fournisseur OFF;

    SET IDENTITY_INSERT categorie_client ON;
    INSERT INTO categorie_client (id, nom, description) VALUES
        (9901, 'Demo Client Professionnel Soutenance', 'Entreprises et organisations B2B'),
        (9902, 'Demo Client Retail Soutenance', 'Commerces, pharmacies, hotels et ecoles');
    SET IDENTITY_INSERT categorie_client OFF;

    SET IDENTITY_INSERT client ON;
    INSERT INTO client (
        id, nom, prenom, email, telephone, adresse,
        secteur_activite, categorie_client_id, entreprise_id
    ) VALUES
        (9901, 'Alpha Market SARL', 'Service Achats', 'achats@alpha-market.tn',
         '+216 71 333 100', 'Centre Urbain Nord, Tunis', 'Grande distribution', 9901, @entId),
        (9902, 'Cabinet Ben Amor', 'Direction', 'contact@cabinet-benamor.tn',
         '+216 71 444 210', 'Rue Alain Savary, Tunis', 'Services professionnels', 9901, @entId),
        (9903, 'Hotel Les Palmiers', 'Economat', 'economat@lespalmiers.tn',
         '+216 73 500 300', 'Zone touristique, Sousse', 'Hotellerie', 9902, @entId),
        (9904, 'Pharmacie Centrale Sousse', 'Service Achat', 'achat@pharmacie-sousse.tn',
         '+216 73 220 900', 'Avenue Mohamed V, Sousse', 'Sante', 9902, @entId),
        (9905, 'Ecole Privee El Amal', 'Administration', 'admin@ecole-elamal.tn',
         '+216 72 310 700', 'Rue Ibn Khaldoun, Nabeul', 'Education', 9902, @entId);
    SET IDENTITY_INSERT client OFF;

    INSERT INTO client_entreprises (client_id, entreprises_id) VALUES
        (9901, @entId), (9902, @entId), (9903, @entId), (9904, @entId), (9905, @entId);

    -- ------------------------------------------------------------
    -- Purchase articles, stock, and sales articles.
    -- ------------------------------------------------------------
    SET IDENTITY_INSERT article ON;
    INSERT INTO article (
        id, reference, nom, description, prix_achat, prix_vente, quantite,
        sku, unite, actif, created_at, updated_at,
        categorie_article_id, mesure_id, fournisseur_id, entreprise_id
    ) VALUES
        (9901, 'DEMO-ACH-0001', 'Ordinateur Portable Pro 14', 'Laptop professionnel Core i5, 16Go RAM, SSD 512Go', 1850.00, 2450.00, 17, 'SKU-DEMO-LAPTOP14', 'piece', 1, @now, @now, 9901, 9901, 9901, @entId),
        (9902, 'DEMO-ACH-0002', 'Imprimante Laser WiFi', 'Imprimante laser monochrome reseau', 620.00, 890.00, 20, 'SKU-DEMO-PRINT01', 'piece', 1, @now, @now, 9902, 9901, 9903, @entId),
        (9903, 'DEMO-ACH-0003', 'Routeur Fibre AX3000', 'Routeur WiFi 6 pour PME', 210.00, 330.00, 28, 'SKU-DEMO-ROUTER01', 'piece', 1, @now, @now, 9901, 9901, 9901, @entId),
        (9904, 'DEMO-ACH-0004', 'Scanner Code-Barres USB', 'Scanner code-barres filaire pour caisse', 145.00, 230.00, 31, 'SKU-DEMO-SCAN01', 'piece', 1, @now, @now, 9904, 9901, 9904, @entId),
        (9905, 'DEMO-ACH-0005', 'Ramette Papier A4 carton', 'Carton de 10 ramettes papier A4 80g', 42.00, 58.00, 90, 'SKU-DEMO-PAPER01', 'carton', 1, @now, @now, 9903, 9902, 9902, @entId),
        (9906, 'DEMO-ACH-0006', 'Toner Laser Compatible', 'Toner laser compatible haute capacite', 135.00, 210.00, 42, 'SKU-DEMO-TONER01', 'piece', 1, @now, @now, 9902, 9901, 9903, @entId),
        (9907, 'DEMO-ACH-0007', 'Caisse Enregistreuse Tactile', 'Terminal point de vente tactile avec logiciel', 980.00, 1450.00, 15, 'SKU-DEMO-POS01', 'piece', 1, @now, @now, 9904, 9901, 9904, @entId),
        (9908, 'DEMO-ACH-0008', 'Support Ecran Ergonomique', 'Support ecran reglable bureau', 55.00, 89.00, 44, 'SKU-DEMO-SUPPORT01', 'piece', 1, @now, @now, 9903, 9901, 9902, @entId),
        (9909, 'DEMO-ACH-0009', 'Pack Etiquette Thermique', 'Pack etiquettes thermiques 1000 pieces', 18.00, 32.00, 110, 'SKU-DEMO-ETIQ01', 'pack', 1, @now, @now, 9903, 9903, 9902, @entId),
        (9910, 'DEMO-ACH-0010', 'Onduleur 1000VA', 'Onduleur line-interactive pour poste critique', 290.00, 450.00, 21, 'SKU-DEMO-UPS01', 'piece', 1, @now, @now, 9901, 9901, 9901, @entId),
        (9911, 'DEMO-ACH-0011', 'Ordinateur Portable Compact 13', 'Laptop leger pour commerciaux terrain', 1520.00, 2050.00, 18, 'SKU-DEMO-LAPTOP13', 'piece', 1, @now, @now, 9901, 9901, 9901, @entId),
        (9912, 'DEMO-ACH-0012', 'Ecran LED 24 pouces', 'Ecran bureautique Full HD HDMI', 310.00, 460.00, 32, 'SKU-DEMO-SCREEN24', 'piece', 1, @now, @now, 9901, 9901, 9901, @entId),
        (9913, 'DEMO-ACH-0013', 'Pack Clavier Souris Sans Fil', 'Pack clavier souris azerty sans fil', 38.00, 65.00, 95, 'SKU-DEMO-KBMOUSE', 'pack', 1, @now, @now, 9901, 9903, 9902, @entId),
        (9914, 'DEMO-ACH-0014', 'Disque SSD Externe 1To', 'SSD externe USB-C haute vitesse', 165.00, 260.00, 24, 'SKU-DEMO-SSD1T', 'piece', 1, @now, @now, 9901, 9901, 9901, @entId),
        (9915, 'DEMO-ACH-0015', 'Switch Reseau 8 Ports', 'Switch gigabit 8 ports metal', 95.00, 155.00, 38, 'SKU-DEMO-SWITCH8', 'piece', 1, @now, @now, 9901, 9901, 9901, @entId),
        (9916, 'DEMO-ACH-0016', 'Camera Surveillance IP', 'Camera IP interieur Full HD', 240.00, 390.00, 16, 'SKU-DEMO-CAMIP', 'piece', 1, @now, @now, 9904, 9901, 9904, @entId),
        (9917, 'DEMO-ACH-0017', 'Terminal Paiement Electronique', 'TPE mobile avec connexion 4G', 220.00, 360.00, 22, 'SKU-DEMO-TPE4G', 'piece', 1, @now, @now, 9904, 9901, 9904, @entId),
        (9918, 'DEMO-ACH-0018', 'Rouleaux Thermiques TPE', 'Carton de rouleaux thermiques pour caisse et TPE', 12.00, 22.00, 180, 'SKU-DEMO-ROLLTPE', 'carton', 1, @now, @now, 9903, 9902, 9902, @entId),
        (9919, 'DEMO-ACH-0019', 'Fauteuil Bureau Ergonomique', 'Fauteuil ergonomique dossier reglable', 180.00, 295.00, 14, 'SKU-DEMO-CHAIR01', 'piece', 1, @now, @now, 9903, 9901, 9902, @entId),
        (9920, 'DEMO-ACH-0020', 'Projecteur HD Salle Reunion', 'Projecteur HD pour salle de formation', 620.00, 980.00, 8, 'SKU-DEMO-PROJHD', 'piece', 1, @now, @now, 9901, 9901, 9901, @entId);
    SET IDENTITY_INSERT article OFF;

    SET IDENTITY_INSERT stock ON;
    INSERT INTO stock (
        id, quantite_entree, quantite_disponible, quantite_reserve, quantite_vendue,
        seuil_minimum, seuil_alerte, emplacement, date_dernier_inventaire,
        created_at, updated_at, article_id, entreprise_id
    ) VALUES
        (9901, 40, 17, 2, 21, 5, 10, 'A1-R01', @now, @now, @now, 9901, @entId),
        (9902, 35, 20, 0, 15, 4, 8, 'A1-R02', @now, @now, @now, 9902, @entId),
        (9903, 70, 28, 0, 42, 8, 15, 'A1-R03', @now, @now, @now, 9903, @entId),
        (9904, 45, 31, 0, 14, 5, 10, 'B1-R01', @now, @now, @now, 9904, @entId),
        (9905, 300, 90, 0, 210, 30, 60, 'B1-R02', @now, @now, @now, 9905, @entId),
        (9906, 120, 42, 0, 78, 15, 25, 'B1-R03', @now, @now, @now, 9906, @entId),
        (9907, 20, 15, 0, 5, 2, 4, 'C1-R01', @now, @now, @now, 9907, @entId),
        (9908, 90, 44, 0, 46, 10, 20, 'C1-R02', @now, @now, @now, 9908, @entId),
        (9909, 390, 110, 0, 280, 50, 100, 'C1-R03', @now, @now, @now, 9909, @entId),
        (9910, 35, 21, 0, 14, 4, 8, 'C1-R04', @now, @now, @now, 9910, @entId),
        (9911, 30, 18, 1, 11, 4, 8, 'A2-R01', @now, @now, @now, 9911, @entId),
        (9912, 52, 32, 0, 20, 6, 12, 'A2-R02', @now, @now, @now, 9912, @entId),
        (9913, 120, 95, 0, 25, 20, 40, 'A2-R03', @now, @now, @now, 9913, @entId),
        (9914, 28, 24, 0, 4, 4, 8, 'A2-R04', @now, @now, @now, 9914, @entId),
        (9915, 48, 38, 0, 10, 5, 10, 'A2-R05', @now, @now, @now, 9915, @entId),
        (9916, 20, 16, 0, 4, 3, 6, 'C2-R01', @now, @now, @now, 9916, @entId),
        (9917, 25, 22, 0, 3, 3, 6, 'C2-R02', @now, @now, @now, 9917, @entId),
        (9918, 260, 180, 0, 80, 30, 60, 'C2-R03', @now, @now, @now, 9918, @entId),
        (9919, 20, 14, 0, 6, 2, 4, 'B2-R01', @now, @now, @now, 9919, @entId),
        (9920, 10, 8, 0, 2, 1, 2, 'B2-R02', @now, @now, @now, 9920, @entId);
    SET IDENTITY_INSERT stock OFF;

    SET IDENTITY_INSERT article_vente ON;
    INSERT INTO article_vente (
        id, nom, description, prix_unitaire, quantite, unite, sku, markup,
        created_at, updated_at, article_achat_id, stock_id
    ) VALUES
        (9901, 'Ordinateur Portable Pro 14', 'Laptop professionnel pret a vendre', 2450.00, 17, 'piece', 'SKU-DEMO-LAPTOP14', 32.43, @now, @now, 9901, 9901),
        (9902, 'Imprimante Laser WiFi', 'Imprimante laser reseau', 890.00, 20, 'piece', 'SKU-DEMO-PRINT01', 43.55, @now, @now, 9902, 9902),
        (9903, 'Routeur Fibre AX3000', 'Routeur WiFi 6 entreprise', 330.00, 28, 'piece', 'SKU-DEMO-ROUTER01', 57.14, @now, @now, 9903, 9903),
        (9904, 'Scanner Code-Barres USB', 'Scanner pour point de vente', 230.00, 31, 'piece', 'SKU-DEMO-SCAN01', 58.62, @now, @now, 9904, 9904),
        (9905, 'Ramette Papier A4 carton', 'Carton de 10 ramettes', 58.00, 90, 'carton', 'SKU-DEMO-PAPER01', 38.10, @now, @now, 9905, 9905),
        (9906, 'Toner Laser Compatible', 'Toner haute capacite', 210.00, 42, 'piece', 'SKU-DEMO-TONER01', 55.56, @now, @now, 9906, 9906),
        (9907, 'Caisse Enregistreuse Tactile', 'Terminal de caisse tactile', 1450.00, 15, 'piece', 'SKU-DEMO-POS01', 47.96, @now, @now, 9907, 9907),
        (9908, 'Support Ecran Ergonomique', 'Support ecran reglable', 89.00, 44, 'piece', 'SKU-DEMO-SUPPORT01', 61.82, @now, @now, 9908, 9908),
        (9909, 'Pack Etiquette Thermique', 'Etiquettes thermiques 1000 pieces', 32.00, 110, 'pack', 'SKU-DEMO-ETIQ01', 77.78, @now, @now, 9909, 9909),
        (9910, 'Onduleur 1000VA', 'Onduleur line-interactive', 450.00, 21, 'piece', 'SKU-DEMO-UPS01', 55.17, @now, @now, 9910, 9910),
        (9911, 'Ordinateur Portable Compact 13', 'Laptop compact pret a vendre', 2050.00, 18, 'piece', 'SKU-DEMO-LAPTOP13', 34.87, @now, @now, 9911, 9911),
        (9912, 'Ecran LED 24 pouces', 'Ecran bureautique Full HD', 460.00, 32, 'piece', 'SKU-DEMO-SCREEN24', 48.39, @now, @now, 9912, 9912),
        (9913, 'Pack Clavier Souris Sans Fil', 'Pack clavier souris pour poste bureau', 65.00, 95, 'pack', 'SKU-DEMO-KBMOUSE', 71.05, @now, @now, 9913, 9913),
        (9914, 'Disque SSD Externe 1To', 'SSD externe USB-C', 260.00, 24, 'piece', 'SKU-DEMO-SSD1T', 57.58, @now, @now, 9914, 9914),
        (9915, 'Switch Reseau 8 Ports', 'Switch gigabit PME', 155.00, 38, 'piece', 'SKU-DEMO-SWITCH8', 63.16, @now, @now, 9915, 9915),
        (9916, 'Camera Surveillance IP', 'Camera IP interieur', 390.00, 16, 'piece', 'SKU-DEMO-CAMIP', 62.50, @now, @now, 9916, 9916),
        (9917, 'Terminal Paiement Electronique', 'TPE mobile 4G', 360.00, 22, 'piece', 'SKU-DEMO-TPE4G', 63.64, @now, @now, 9917, 9917),
        (9918, 'Rouleaux Thermiques TPE', 'Carton rouleaux thermiques', 22.00, 180, 'carton', 'SKU-DEMO-ROLLTPE', 83.33, @now, @now, 9918, 9918),
        (9919, 'Fauteuil Bureau Ergonomique', 'Fauteuil bureau reglable', 295.00, 14, 'piece', 'SKU-DEMO-CHAIR01', 63.89, @now, @now, 9919, 9919),
        (9920, 'Projecteur HD Salle Reunion', 'Projecteur HD formation', 980.00, 8, 'piece', 'SKU-DEMO-PROJHD', 58.06, @now, @now, 9920, 9920);
    SET IDENTITY_INSERT article_vente OFF;

    -- ------------------------------------------------------------
    -- Purchase orders and lines.
    -- ------------------------------------------------------------
    SET IDENTITY_INSERT commande_achat ON;
    INSERT INTO commande_achat (
        id, reference, date_commande, date_livraison_prevue, statut,
        montant_ht, montant_tva, montant_ttc, note,
        created_at, updated_at, fournisseur_id, entreprise_id
    ) VALUES
        (9901, 'CA-DEMO-202507-001', '2025-07-02', '2025-07-09', 'RECUE', 34740.00, 6600.60, 41340.60, 'Approvisionnement lancement demo', @now, @now, 9901, @entId),
        (9902, 'CA-DEMO-202509-002', '2025-09-05', '2025-09-13', 'RECUE', 7315.00, 1389.85, 8704.85, 'Fournitures bureau rentree', @now, @now, 9902, @entId),
        (9903, 'CA-DEMO-202511-003', '2025-11-18', '2025-11-25', 'RECUE', 17400.00, 3306.00, 20706.00, 'Stock impression Q4', @now, @now, 9903, @entId),
        (9904, 'CA-DEMO-202601-004', '2026-01-12', '2026-01-20', 'RECUE', 12190.00, 2316.10, 14506.10, 'Equipement magasins', @now, @now, 9904, @entId),
        (9905, 'CA-DEMO-202603-005', '2026-03-08', '2026-03-16', 'RECUE', 27775.00, 5277.25, 33052.25, 'Reassort informatique', @now, @now, 9901, @entId),
        (9906, 'CA-DEMO-202605-006', '2026-05-20', '2026-06-10', 'CONFIRMEE', 6810.00, 1293.90, 8103.90, 'Commande fournisseur en cours', @now, @now, 9902, @entId),
        (9907, 'CA-DEMO-202508-007', '2025-08-06', '2025-08-14', 'RECUE', 15880.00, 3017.20, 18897.20, 'Achat laptops compacts et ecrans', @now, @now, 9901, @entId),
        (9908, 'CA-DEMO-202510-008', '2025-10-07', '2025-10-15', 'RECUE', 3710.00, 704.90, 4414.90, 'Achat accessoires bureau et consommables caisse', @now, @now, 9902, @entId),
        (9909, 'CA-DEMO-202512-009', '2025-12-11', '2025-12-19', 'RECUE', 6585.00, 1251.15, 7836.15, 'Achat stockage et reseau fin annee', @now, @now, 9901, @entId),
        (9910, 'CA-DEMO-202602-010', '2026-02-05', '2026-02-13', 'RECUE', 10200.00, 1938.00, 12138.00, 'Reassort consommables fevrier', @now, @now, 9904, @entId),
        (9911, 'CA-DEMO-202604-011', '2026-04-10', '2026-04-18', 'RECUE', 14080.00, 2675.20, 16755.20, 'Achat projecteurs et impression avril', @now, @now, 9903, @entId),
        (9912, 'CA-DEMO-202606-012', '2026-06-01', '2026-06-09', 'CONFIRMEE', 18015.00, 3422.85, 21437.85, 'Commande juin avant soutenance', @now, @now, 9901, @entId);
    SET IDENTITY_INSERT commande_achat OFF;

    SET IDENTITY_INSERT ligne_commande_achat ON;
    INSERT INTO ligne_commande_achat (
        id, quantite, prix_unitaire_ht, taux_tva, montant_ht, montant_ttc,
        created_at, updated_at, article_id, commande_achat_id
    ) VALUES
        (9901, 15, 1850.00, 19.00, 27750.00, 33022.50, @now, @now, 9901, 9901),
        (9902, 25, 210.00, 19.00, 5250.00, 6247.50, @now, @now, 9903, 9901),
        (9903, 12, 145.00, 19.00, 1740.00, 2070.60, @now, @now, 9904, 9901),
        (9904, 90, 42.00, 19.00, 3780.00, 4498.20, @now, @now, 9905, 9902),
        (9905, 25, 55.00, 19.00, 1375.00, 1636.25, @now, @now, 9908, 9902),
        (9906, 120, 18.00, 19.00, 2160.00, 2570.40, @now, @now, 9909, 9902),
        (9907, 15, 620.00, 19.00, 9300.00, 11067.00, @now, @now, 9902, 9903),
        (9908, 60, 135.00, 19.00, 8100.00, 9639.00, @now, @now, 9906, 9903),
        (9909, 8, 980.00, 19.00, 7840.00, 9329.60, @now, @now, 9907, 9904),
        (9910, 15, 290.00, 19.00, 4350.00, 5176.50, @now, @now, 9910, 9904),
        (9911, 10, 1850.00, 19.00, 18500.00, 22015.00, @now, @now, 9901, 9905),
        (9912, 20, 210.00, 19.00, 4200.00, 4998.00, @now, @now, 9903, 9905),
        (9913, 15, 145.00, 19.00, 2175.00, 2588.25, @now, @now, 9904, 9905),
        (9914, 10, 290.00, 19.00, 2900.00, 3451.00, @now, @now, 9910, 9905),
        (9915, 80, 42.00, 19.00, 3360.00, 3998.40, @now, @now, 9905, 9906),
        (9916, 100, 18.00, 19.00, 1800.00, 2142.00, @now, @now, 9909, 9906),
        (9917, 30, 55.00, 19.00, 1650.00, 1963.50, @now, @now, 9908, 9906),
        (9918, 8, 1520.00, 19.00, 12160.00, 14470.40, @now, @now, 9911, 9907),
        (9919, 12, 310.00, 19.00, 3720.00, 4426.80, @now, @now, 9912, 9907),
        (9920, 25, 38.00, 19.00, 950.00, 1130.50, @now, @now, 9913, 9908),
        (9921, 80, 12.00, 19.00, 960.00, 1142.40, @now, @now, 9918, 9908),
        (9922, 10, 180.00, 19.00, 1800.00, 2142.00, @now, @now, 9919, 9908),
        (9923, 15, 165.00, 19.00, 2475.00, 2945.25, @now, @now, 9914, 9909),
        (9924, 18, 95.00, 19.00, 1710.00, 2034.90, @now, @now, 9915, 9909),
        (9925, 10, 240.00, 19.00, 2400.00, 2856.00, @now, @now, 9916, 9909),
        (9926, 12, 220.00, 19.00, 2640.00, 3141.60, @now, @now, 9917, 9910),
        (9927, 120, 42.00, 19.00, 5040.00, 5997.60, @now, @now, 9905, 9910),
        (9928, 140, 18.00, 19.00, 2520.00, 2998.80, @now, @now, 9909, 9910),
        (9929, 6, 620.00, 19.00, 3720.00, 4426.80, @now, @now, 9920, 9911),
        (9930, 8, 620.00, 19.00, 4960.00, 5902.40, @now, @now, 9902, 9911),
        (9931, 40, 135.00, 19.00, 5400.00, 6426.00, @now, @now, 9906, 9911),
        (9932, 6, 1850.00, 19.00, 11100.00, 13209.00, @now, @now, 9901, 9912),
        (9933, 10, 310.00, 19.00, 3100.00, 3689.00, @now, @now, 9912, 9912),
        (9934, 25, 95.00, 19.00, 2375.00, 2826.25, @now, @now, 9915, 9912),
        (9935, 120, 12.00, 19.00, 1440.00, 1713.60, @now, @now, 9918, 9912);
    SET IDENTITY_INSERT ligne_commande_achat OFF;

    -- ------------------------------------------------------------
    -- Sales orders: monthly data for dashboard and AI forecasting.
    -- ------------------------------------------------------------
    SET IDENTITY_INSERT commande_vente ON;
    INSERT INTO commande_vente (
        id, reference, date_commande, date_livraison_prevue, statut,
        montant_ht, montant_tva, montant_ttc, note,
        created_at, updated_at, client_id, entreprise_id
    ) VALUES
        (9901, 'CV-DEMO-202507-001', '2025-07-15', '2025-07-18', 'LIVREE', 10190.00, 1936.10, 12126.10, 'Vente initiale Alpha Market', @now, @now, 9901, @entId),
        (9902, 'CV-DEMO-202508-002', '2025-08-10', '2025-08-13', 'LIVREE', 3474.00, 660.06, 4134.06, 'Installation reseau cabinet', @now, @now, 9902, @entId),
        (9903, 'CV-DEMO-202509-003', '2025-09-06', '2025-09-10', 'LIVREE', 6510.00, 1236.90, 7746.90, 'Renouvellement impression hotel', @now, @now, 9903, @entId),
        (9904, 'CV-DEMO-202510-004', '2025-10-12', '2025-10-15', 'LIVREE', 4840.00, 919.60, 5759.60, 'Materiel caisse pharmacie', @now, @now, 9904, @entId),
        (9905, 'CV-DEMO-202511-005', '2025-11-20', '2025-11-24', 'LIVREE', 12312.00, 2339.28, 14651.28, 'Equipement salle informatique', @now, @now, 9905, @entId),
        (9906, 'CV-DEMO-202512-006', '2025-12-05', '2025-12-09', 'CONFIRMEE', 5910.00, 1122.90, 7032.90, 'Caisse et consommables Alpha', @now, @now, 9901, @entId),
        (9907, 'CV-DEMO-202601-007', '2026-01-14', '2026-01-18', 'LIVREE', 8100.00, 1539.00, 9639.00, 'Impression cabinet Ben Amor', @now, @now, 9902, @entId),
        (9908, 'CV-DEMO-202601-008', '2026-01-28', '2026-02-01', 'LIVREE', 7780.00, 1478.20, 9258.20, 'Renfort reseau hotel', @now, @now, 9903, @entId),
        (9909, 'CV-DEMO-202602-009', '2026-02-09', '2026-02-12', 'LIVREE', 3870.00, 735.30, 4605.30, 'Consommables pharmacie', @now, @now, 9904, @entId),
        (9910, 'CV-DEMO-202602-010', '2026-02-24', '2026-02-27', 'LIVREE', 10540.00, 2002.60, 12542.60, 'Materiel magasin Alpha', @now, @now, 9901, @entId),
        (9911, 'CV-DEMO-202603-011', '2026-03-08', '2026-03-12', 'LIVREE', 6110.00, 1160.90, 7270.90, 'Reseau et etiquettes ecole', @now, @now, 9905, @entId),
        (9912, 'CV-DEMO-202603-012', '2026-03-23', '2026-03-27', 'LIVREE', 6120.00, 1162.80, 7282.80, 'Reassort hotel mars', @now, @now, 9903, @entId),
        (9913, 'CV-DEMO-202604-013', '2026-04-05', '2026-04-09', 'LIVREE', 15638.00, 2971.22, 18609.22, 'Parc informatique cabinet', @now, @now, 9902, @entId),
        (9914, 'CV-DEMO-202604-014', '2026-04-19', '2026-04-22', 'LIVREE', 5710.00, 1084.90, 6794.90, 'Pharmacie etiquetage et scan', @now, @now, 9904, @entId),
        (9915, 'CV-DEMO-202605-015', '2026-05-07', '2026-05-10', 'LIVREE', 10710.00, 2034.90, 12744.90, 'Contrat consommables Alpha', @now, @now, 9901, @entId),
        (9916, 'CV-DEMO-202605-016', '2026-05-21', '2026-05-25', 'LIVREE', 14950.00, 2840.50, 17790.50, 'Equipement reception hotel', @now, @now, 9903, @entId),
        (9917, 'CV-DEMO-202606-017', '2026-06-02', '2026-06-06', 'CONFIRMEE', 4280.00, 813.20, 5093.20, 'Commande juin cabinet', @now, @now, 9902, @entId),
        (9918, 'CV-DEMO-202606-018', '2026-06-03', '2026-06-07', 'ANNULEE', 2895.00, 550.05, 3445.05, 'Commande annulee pour demonstration statut', @now, @now, 9905, @entId),
        (9919, 'CV-DEMO-202507-019', '2025-07-28', '2025-07-31', 'LIVREE', 6590.00, 1252.10, 7842.10, 'Vente complementaire juillet', @now, @now, 9902, @entId),
        (9920, 'CV-DEMO-202508-020', '2025-08-22', '2025-08-26', 'LIVREE', 3410.00, 647.90, 4057.90, 'Equipement caisse aout', @now, @now, 9904, @entId),
        (9921, 'CV-DEMO-202509-021', '2025-09-25', '2025-09-29', 'LIVREE', 2695.00, 512.05, 3207.05, 'Consommables et mobilier septembre', @now, @now, 9905, @entId),
        (9922, 'CV-DEMO-202510-022', '2025-10-28', '2025-10-31', 'LIVREE', 5800.00, 1102.00, 6902.00, 'Pack informatique octobre', @now, @now, 9901, @entId),
        (9923, 'CV-DEMO-202511-023', '2025-11-29', '2025-12-03', 'LIVREE', 4130.00, 784.70, 4914.70, 'Salle reunion novembre', @now, @now, 9903, @entId),
        (9924, 'CV-DEMO-202512-024', '2025-12-22', '2025-12-26', 'LIVREE', 7550.00, 1434.50, 8984.50, 'Commande fin annee', @now, @now, 9901, @entId),
        (9925, 'CV-DEMO-202601-025', '2026-01-25', '2026-01-29', 'LIVREE', 5090.00, 967.10, 6057.10, 'Equipement securite janvier', @now, @now, 9904, @entId),
        (9926, 'CV-DEMO-202602-026', '2026-02-18', '2026-02-22', 'LIVREE', 3080.00, 585.20, 3665.20, 'Postes bureau fevrier', @now, @now, 9902, @entId),
        (9927, 'CV-DEMO-202603-027', '2026-03-28', '2026-04-01', 'LIVREE', 3395.00, 645.05, 4040.05, 'Projection et reseau mars', @now, @now, 9905, @entId),
        (9928, 'CV-DEMO-202604-028', '2026-04-26', '2026-04-30', 'LIVREE', 6900.00, 1311.00, 8211.00, 'Parc bureautique avril', @now, @now, 9903, @entId),
        (9929, 'CV-DEMO-202605-029', '2026-05-28', '2026-06-01', 'LIVREE', 4160.00, 790.40, 4950.40, 'Caisse et consommables mai', @now, @now, 9901, @entId),
        (9930, 'CV-DEMO-202606-030', '2026-06-10', '2026-06-14', 'CONFIRMEE', 4300.00, 817.00, 5117.00, 'Commande juin en preparation', @now, @now, 9902, @entId);
    SET IDENTITY_INSERT commande_vente OFF;

    SET IDENTITY_INSERT ligne_commande_vente ON;
    INSERT INTO ligne_commande_vente (
        id, quantite, prix_unitaire_ht, taux_tva, montant_ht, montant_ttc,
        created_at, updated_at, article_id, commande_vente_id
    ) VALUES
        (9951, 3, 2450.00, 19.00, 7350.00, 8746.50, @now, @now, 9901, 9901),
        (9952, 8, 210.00, 19.00, 1680.00, 1999.20, @now, @now, 9906, 9901),
        (9953, 20, 58.00, 19.00, 1160.00, 1380.40, @now, @now, 9905, 9901),
        (9954, 6, 330.00, 19.00, 1980.00, 2356.20, @now, @now, 9903, 9902),
        (9955, 6, 89.00, 19.00, 534.00, 635.46, @now, @now, 9908, 9902),
        (9956, 30, 32.00, 19.00, 960.00, 1142.40, @now, @now, 9909, 9902),
        (9957, 3, 890.00, 19.00, 2670.00, 3177.30, @now, @now, 9902, 9903),
        (9958, 10, 210.00, 19.00, 2100.00, 2499.00, @now, @now, 9906, 9903),
        (9959, 30, 58.00, 19.00, 1740.00, 2070.60, @now, @now, 9905, 9903),
        (9960, 4, 230.00, 19.00, 920.00, 1094.80, @now, @now, 9904, 9904),
        (9961, 8, 330.00, 19.00, 2640.00, 3141.60, @now, @now, 9903, 9904),
        (9962, 40, 32.00, 19.00, 1280.00, 1523.20, @now, @now, 9909, 9904),
        (9963, 4, 2450.00, 19.00, 9800.00, 11662.00, @now, @now, 9901, 9905),
        (9964, 4, 450.00, 19.00, 1800.00, 2142.00, @now, @now, 9910, 9905),
        (9965, 8, 89.00, 19.00, 712.00, 847.28, @now, @now, 9908, 9905),
        (9966, 2, 1450.00, 19.00, 2900.00, 3451.00, @now, @now, 9907, 9906),
        (9967, 3, 230.00, 19.00, 690.00, 821.10, @now, @now, 9904, 9906),
        (9968, 40, 58.00, 19.00, 2320.00, 2760.80, @now, @now, 9905, 9906),
        (9969, 4, 890.00, 19.00, 3560.00, 4236.40, @now, @now, 9902, 9907),
        (9970, 14, 210.00, 19.00, 2940.00, 3498.60, @now, @now, 9906, 9907),
        (9971, 50, 32.00, 19.00, 1600.00, 1904.00, @now, @now, 9909, 9907),
        (9972, 2, 2450.00, 19.00, 4900.00, 5831.00, @now, @now, 9901, 9908),
        (9973, 6, 330.00, 19.00, 1980.00, 2356.20, @now, @now, 9903, 9908),
        (9974, 2, 450.00, 19.00, 900.00, 1071.00, @now, @now, 9910, 9908),
        (9975, 2, 230.00, 19.00, 460.00, 547.40, @now, @now, 9904, 9909),
        (9976, 12, 210.00, 19.00, 2520.00, 2998.80, @now, @now, 9906, 9909),
        (9977, 10, 89.00, 19.00, 890.00, 1059.10, @now, @now, 9908, 9909),
        (9978, 3, 2450.00, 19.00, 7350.00, 8746.50, @now, @now, 9901, 9910),
        (9979, 1, 1450.00, 19.00, 1450.00, 1725.50, @now, @now, 9907, 9910),
        (9980, 30, 58.00, 19.00, 1740.00, 2070.60, @now, @now, 9905, 9910),
        (9981, 10, 330.00, 19.00, 3300.00, 3927.00, @now, @now, 9903, 9911),
        (9982, 60, 32.00, 19.00, 1920.00, 2284.80, @now, @now, 9909, 9911),
        (9983, 10, 89.00, 19.00, 890.00, 1059.10, @now, @now, 9908, 9911),
        (9984, 3, 890.00, 19.00, 2670.00, 3177.30, @now, @now, 9902, 9912),
        (9985, 10, 210.00, 19.00, 2100.00, 2499.00, @now, @now, 9906, 9912),
        (9986, 3, 450.00, 19.00, 1350.00, 1606.50, @now, @now, 9910, 9912),
        (9987, 5, 2450.00, 19.00, 12250.00, 14577.50, @now, @now, 9901, 9913),
        (9988, 40, 58.00, 19.00, 2320.00, 2760.80, @now, @now, 9905, 9913),
        (9989, 12, 89.00, 19.00, 1068.00, 1270.92, @now, @now, 9908, 9913),
        (9990, 5, 230.00, 19.00, 1150.00, 1368.50, @now, @now, 9904, 9914),
        (9991, 8, 330.00, 19.00, 2640.00, 3141.60, @now, @now, 9903, 9914),
        (9992, 60, 32.00, 19.00, 1920.00, 2284.80, @now, @now, 9909, 9914),
        (9993, 5, 890.00, 19.00, 4450.00, 5295.50, @now, @now, 9902, 9915),
        (9994, 16, 210.00, 19.00, 3360.00, 3998.40, @now, @now, 9906, 9915),
        (9995, 50, 58.00, 19.00, 2900.00, 3451.00, @now, @now, 9905, 9915),
        (9996, 4, 2450.00, 19.00, 9800.00, 11662.00, @now, @now, 9901, 9916),
        (9997, 5, 450.00, 19.00, 2250.00, 2677.50, @now, @now, 9910, 9916),
        (9998, 2, 1450.00, 19.00, 2900.00, 3451.00, @now, @now, 9907, 9916),
        (9999, 4, 330.00, 19.00, 1320.00, 1570.80, @now, @now, 9903, 9917),
        (10000, 8, 210.00, 19.00, 1680.00, 1999.20, @now, @now, 9906, 9917),
        (10001, 40, 32.00, 19.00, 1280.00, 1523.20, @now, @now, 9909, 9917),
        (10002, 1, 2450.00, 19.00, 2450.00, 2915.50, @now, @now, 9901, 9918),
        (10003, 5, 89.00, 19.00, 445.00, 529.55, @now, @now, 9908, 9918),
        (10004, 2, 2050.00, 19.00, 4100.00, 4879.00, @now, @now, 9911, 9919),
        (10005, 4, 460.00, 19.00, 1840.00, 2189.60, @now, @now, 9912, 9919),
        (10006, 10, 65.00, 19.00, 650.00, 773.50, @now, @now, 9913, 9919),
        (10007, 1, 980.00, 19.00, 980.00, 1166.20, @now, @now, 9920, 9920),
        (10008, 3, 390.00, 19.00, 1170.00, 1392.30, @now, @now, 9916, 9920),
        (10009, 15, 22.00, 19.00, 330.00, 392.70, @now, @now, 9918, 9920),
        (10010, 6, 155.00, 19.00, 930.00, 1106.70, @now, @now, 9915, 9920),
        (10011, 2, 260.00, 19.00, 520.00, 618.80, @now, @now, 9914, 9921),
        (10012, 12, 65.00, 19.00, 780.00, 928.20, @now, @now, 9913, 9921),
        (10013, 1, 295.00, 19.00, 295.00, 351.05, @now, @now, 9919, 9921),
        (10014, 50, 22.00, 19.00, 1100.00, 1309.00, @now, @now, 9918, 9921),
        (10015, 2, 2050.00, 19.00, 4100.00, 4879.00, @now, @now, 9911, 9922),
        (10016, 3, 360.00, 19.00, 1080.00, 1285.20, @now, @now, 9917, 9922),
        (10017, 4, 155.00, 19.00, 620.00, 737.80, @now, @now, 9915, 9922),
        (10018, 1, 980.00, 19.00, 980.00, 1166.20, @now, @now, 9920, 9923),
        (10019, 6, 460.00, 19.00, 2760.00, 3284.40, @now, @now, 9912, 9923),
        (10020, 6, 65.00, 19.00, 390.00, 464.10, @now, @now, 9913, 9923),
        (10021, 3, 2050.00, 19.00, 6150.00, 7318.50, @now, @now, 9911, 9924),
        (10022, 2, 260.00, 19.00, 520.00, 618.80, @now, @now, 9914, 9924),
        (10023, 40, 22.00, 19.00, 880.00, 1047.20, @now, @now, 9918, 9924),
        (10024, 1, 1450.00, 19.00, 1450.00, 1725.50, @now, @now, 9907, 9925),
        (10025, 4, 390.00, 19.00, 1560.00, 1856.40, @now, @now, 9916, 9925),
        (10026, 4, 360.00, 19.00, 1440.00, 1713.60, @now, @now, 9917, 9925),
        (10027, 20, 32.00, 19.00, 640.00, 761.60, @now, @now, 9909, 9925),
        (10028, 4, 460.00, 19.00, 1840.00, 2189.60, @now, @now, 9912, 9926),
        (10029, 10, 65.00, 19.00, 650.00, 773.50, @now, @now, 9913, 9926),
        (10030, 2, 295.00, 19.00, 590.00, 702.10, @now, @now, 9919, 9926),
        (10031, 2, 980.00, 19.00, 1960.00, 2332.40, @now, @now, 9920, 9927),
        (10032, 5, 155.00, 19.00, 775.00, 922.25, @now, @now, 9915, 9927),
        (10033, 30, 22.00, 19.00, 660.00, 785.40, @now, @now, 9918, 9927),
        (10034, 1, 2050.00, 19.00, 2050.00, 2439.50, @now, @now, 9911, 9928),
        (10035, 8, 460.00, 19.00, 3680.00, 4379.20, @now, @now, 9912, 9928),
        (10036, 2, 260.00, 19.00, 520.00, 618.80, @now, @now, 9914, 9928),
        (10037, 10, 65.00, 19.00, 650.00, 773.50, @now, @now, 9913, 9928),
        (10038, 3, 390.00, 19.00, 1170.00, 1392.30, @now, @now, 9916, 9929),
        (10039, 3, 360.00, 19.00, 1080.00, 1285.20, @now, @now, 9917, 9929),
        (10040, 60, 22.00, 19.00, 1320.00, 1570.80, @now, @now, 9918, 9929),
        (10041, 2, 295.00, 19.00, 590.00, 702.10, @now, @now, 9919, 9929),
        (10042, 1, 980.00, 19.00, 980.00, 1166.20, @now, @now, 9920, 9930),
        (10043, 1, 2050.00, 19.00, 2050.00, 2439.50, @now, @now, 9911, 9930),
        (10044, 4, 155.00, 19.00, 620.00, 737.80, @now, @now, 9915, 9930),
        (10045, 10, 65.00, 19.00, 650.00, 773.50, @now, @now, 9913, 9930);
    SET IDENTITY_INSERT ligne_commande_vente OFF;

    -- ------------------------------------------------------------
    -- Invoices for the demo pages.
    -- ------------------------------------------------------------
    SET IDENTITY_INSERT facture_achat ON;
    INSERT INTO facture_achat (
        id, referance, date_achat, montant_total, montantht, montanttva,
        montantttc, remise, mode_paiement, statut, description, chemin_fichier,
        fournisseur_id, commande_achat_id, entreprise_id
    ) VALUES
        (9901, 'FA-DEMO-202507-001', '2025-07-09', 41340.60, 34740.00, 6600.60, 41340.60, 0.00, 'VIREMENT', 'PAYEE', 'Facture achat CA-DEMO-202507-001', NULL, 9901, 9901, @entId),
        (9902, 'FA-DEMO-202509-002', '2025-09-13', 8704.85, 7315.00, 1389.85, 8704.85, 0.00, 'CHEQUE', 'PAYEE', 'Facture achat CA-DEMO-202509-002', NULL, 9902, 9902, @entId),
        (9903, 'FA-DEMO-202511-003', '2025-11-25', 20706.00, 17400.00, 3306.00, 20706.00, 0.00, 'VIREMENT', 'PAYEE', 'Facture achat CA-DEMO-202511-003', NULL, 9903, 9903, @entId),
        (9904, 'FA-DEMO-202601-004', '2026-01-20', 14506.10, 12190.00, 2316.10, 14506.10, 0.00, 'TRAITE', 'PAYEE', 'Facture achat CA-DEMO-202601-004', NULL, 9904, 9904, @entId),
        (9905, 'FA-DEMO-202603-005', '2026-03-16', 33052.25, 27775.00, 5277.25, 33052.25, 0.00, 'VIREMENT', 'PAYEE', 'Facture achat CA-DEMO-202603-005', NULL, 9901, 9905, @entId),
        (9906, 'FA-DEMO-202508-006', '2025-08-14', 18897.20, 15880.00, 3017.20, 18897.20, 0.00, 'VIREMENT', 'PAYEE', 'Facture achat CA-DEMO-202508-007', NULL, 9901, 9907, @entId),
        (9907, 'FA-DEMO-202510-007', '2025-10-15', 4414.90, 3710.00, 704.90, 4414.90, 0.00, 'CHEQUE', 'PAYEE', 'Facture achat CA-DEMO-202510-008', NULL, 9902, 9908, @entId),
        (9908, 'FA-DEMO-202512-008', '2025-12-19', 7836.15, 6585.00, 1251.15, 7836.15, 0.00, 'VIREMENT', 'PAYEE', 'Facture achat CA-DEMO-202512-009', NULL, 9901, 9909, @entId),
        (9909, 'FA-DEMO-202602-009', '2026-02-13', 12138.00, 10200.00, 1938.00, 12138.00, 0.00, 'TRAITE', 'PAYEE', 'Facture achat CA-DEMO-202602-010', NULL, 9904, 9910, @entId),
        (9910, 'FA-DEMO-202604-010', '2026-04-18', 16755.20, 14080.00, 2675.20, 16755.20, 0.00, 'VIREMENT', 'PAYEE', 'Facture achat CA-DEMO-202604-011', NULL, 9903, 9911, @entId),
        (9911, 'FA-DEMO-202606-011', '2026-06-09', 21437.85, 18015.00, 3422.85, 21437.85, 0.00, 'VIREMENT', 'EN_ATTENTE', 'Facture achat CA-DEMO-202606-012', NULL, 9901, 9912, @entId);
    SET IDENTITY_INSERT facture_achat OFF;

    SET IDENTITY_INSERT facture_de_vente ON;
    INSERT INTO facture_de_vente (
        id, referance, date_achat, montant_total, montantht, montanttva,
        montantttc, remise, mode_paiement, statut, description, chemin_fichier,
        client_id, commande_vente_id, entreprise_id
    ) VALUES
        (9901, 'FV-DEMO-202507-001', '2025-07-18', 12126.10, 10190.00, 1936.10, 12126.10, 0.00, 'VIREMENT', 'PAYEE', 'Facture vente CV-DEMO-202507-001', NULL, 9901, 9901, @entId),
        (9903, 'FV-DEMO-202509-003', '2025-09-10', 7746.90, 6510.00, 1236.90, 7746.90, 0.00, 'CHEQUE', 'PAYEE', 'Facture vente CV-DEMO-202509-003', NULL, 9903, 9903, @entId),
        (9905, 'FV-DEMO-202511-005', '2025-11-24', 14651.28, 12312.00, 2339.28, 14651.28, 0.00, 'VIREMENT', 'PAYEE', 'Facture vente CV-DEMO-202511-005', NULL, 9905, 9905, @entId),
        (9907, 'FV-DEMO-202601-007', '2026-01-18', 9639.00, 8100.00, 1539.00, 9639.00, 0.00, 'VIREMENT', 'PAYEE', 'Facture vente CV-DEMO-202601-007', NULL, 9902, 9907, @entId),
        (9910, 'FV-DEMO-202602-010', '2026-02-27', 12542.60, 10540.00, 2002.60, 12542.60, 0.00, 'CARTE_BANCAIRE', 'PAYEE', 'Facture vente CV-DEMO-202602-010', NULL, 9901, 9910, @entId),
        (9913, 'FV-DEMO-202604-013', '2026-04-09', 18609.22, 15638.00, 2971.22, 18609.22, 0.00, 'VIREMENT', 'PAYEE', 'Facture vente CV-DEMO-202604-013', NULL, 9902, 9913, @entId),
        (9915, 'FV-DEMO-202605-015', '2026-05-10', 12744.90, 10710.00, 2034.90, 12744.90, 0.00, 'VIREMENT', 'PAYEE', 'Facture vente CV-DEMO-202605-015', NULL, 9901, 9915, @entId),
        (9916, 'FV-DEMO-202605-016', '2026-05-25', 17790.50, 14950.00, 2840.50, 17790.50, 0.00, 'TRAITE', 'PAYEE', 'Facture vente CV-DEMO-202605-016', NULL, 9903, 9916, @entId),
        (9917, 'FV-DEMO-202606-017', '2026-06-06', 5093.20, 4280.00, 813.20, 5093.20, 0.00, 'VIREMENT', 'EN_ATTENTE', 'Facture vente CV-DEMO-202606-017', NULL, 9902, 9917, @entId),
        (9918, 'FV-DEMO-202507-018', '2025-07-31', 7842.10, 6590.00, 1252.10, 7842.10, 0.00, 'VIREMENT', 'PAYEE', 'Facture vente CV-DEMO-202507-019', NULL, 9902, 9919, @entId),
        (9919, 'FV-DEMO-202508-019', '2025-08-26', 4057.90, 3410.00, 647.90, 4057.90, 0.00, 'CARTE_BANCAIRE', 'PAYEE', 'Facture vente CV-DEMO-202508-020', NULL, 9904, 9920, @entId),
        (9920, 'FV-DEMO-202509-020', '2025-09-29', 3207.05, 2695.00, 512.05, 3207.05, 0.00, 'CHEQUE', 'PAYEE', 'Facture vente CV-DEMO-202509-021', NULL, 9905, 9921, @entId),
        (9921, 'FV-DEMO-202510-021', '2025-10-31', 6902.00, 5800.00, 1102.00, 6902.00, 0.00, 'VIREMENT', 'PAYEE', 'Facture vente CV-DEMO-202510-022', NULL, 9901, 9922, @entId),
        (9922, 'FV-DEMO-202511-022', '2025-12-03', 4914.70, 4130.00, 784.70, 4914.70, 0.00, 'VIREMENT', 'PAYEE', 'Facture vente CV-DEMO-202511-023', NULL, 9903, 9923, @entId),
        (9923, 'FV-DEMO-202512-023', '2025-12-26', 8984.50, 7550.00, 1434.50, 8984.50, 0.00, 'TRAITE', 'PAYEE', 'Facture vente CV-DEMO-202512-024', NULL, 9901, 9924, @entId),
        (9924, 'FV-DEMO-202601-024', '2026-01-29', 6057.10, 5090.00, 967.10, 6057.10, 0.00, 'VIREMENT', 'PAYEE', 'Facture vente CV-DEMO-202601-025', NULL, 9904, 9925, @entId),
        (9925, 'FV-DEMO-202602-025', '2026-02-22', 3665.20, 3080.00, 585.20, 3665.20, 0.00, 'CARTE_BANCAIRE', 'PAYEE', 'Facture vente CV-DEMO-202602-026', NULL, 9902, 9926, @entId),
        (9926, 'FV-DEMO-202603-026', '2026-04-01', 4040.05, 3395.00, 645.05, 4040.05, 0.00, 'CHEQUE', 'PAYEE', 'Facture vente CV-DEMO-202603-027', NULL, 9905, 9927, @entId),
        (9927, 'FV-DEMO-202604-027', '2026-04-30', 8211.00, 6900.00, 1311.00, 8211.00, 0.00, 'VIREMENT', 'PAYEE', 'Facture vente CV-DEMO-202604-028', NULL, 9903, 9928, @entId),
        (9928, 'FV-DEMO-202605-028', '2026-06-01', 4950.40, 4160.00, 790.40, 4950.40, 0.00, 'VIREMENT', 'PAYEE', 'Facture vente CV-DEMO-202605-029', NULL, 9901, 9929, @entId),
        (9929, 'FV-DEMO-202606-029', '2026-06-14', 5117.00, 4300.00, 817.00, 5117.00, 0.00, 'VIREMENT', 'EN_ATTENTE', 'Facture vente CV-DEMO-202606-030', NULL, 9902, 9930, @entId);
    SET IDENTITY_INSERT facture_de_vente OFF;

    -- Newer backend versions read facture_de_vente amounts through the
    -- snake_case columns. Older databases still have the legacy columns
    -- montantht/montanttva/montantttc, so fill snake_case when present.
    IF COL_LENGTH('facture_de_vente', 'montant_ht') IS NOT NULL
       AND COL_LENGTH('facture_de_vente', 'montant_tva') IS NOT NULL
       AND COL_LENGTH('facture_de_vente', 'montant_ttc') IS NOT NULL
    BEGIN
        EXEC sp_executesql N'
            UPDATE facture_de_vente
            SET montant_ht = montantht,
                montant_tva = montanttva,
                montant_ttc = montantttc
            WHERE entreprise_id = @pEntId
              AND id BETWEEN 9901 AND 9999',
            N'@pEntId BIGINT',
            @pEntId = @entId;
    END;

    -- ------------------------------------------------------------
    -- Stock movements for stock screens.
    -- ------------------------------------------------------------
    SET IDENTITY_INSERT stock_mouvement ON;
    INSERT INTO stock_mouvement (
        id, type_mouvement, quantite, reference_commande, motif, date_mouvement,
        created_at, updated_at, stock_id, utilisateur_id
    ) VALUES
        (9901, 'ENTREE', 40, 'CA-DEMO-STOCK-INIT', 'Stock initial demo soutenance', '2025-07-09T09:00:00', @now, @now, 9901, @adminUserId),
        (9902, 'ENTREE', 35, 'CA-DEMO-STOCK-INIT', 'Stock initial demo soutenance', '2025-09-13T09:00:00', @now, @now, 9902, @adminUserId),
        (9903, 'ENTREE', 70, 'CA-DEMO-STOCK-INIT', 'Stock initial demo soutenance', '2025-07-09T09:00:00', @now, @now, 9903, @adminUserId),
        (9904, 'ENTREE', 45, 'CA-DEMO-STOCK-INIT', 'Stock initial demo soutenance', '2025-07-09T09:00:00', @now, @now, 9904, @adminUserId),
        (9905, 'ENTREE', 300, 'CA-DEMO-STOCK-INIT', 'Stock initial demo soutenance', '2025-09-13T09:00:00', @now, @now, 9905, @adminUserId),
        (9906, 'ENTREE', 120, 'CA-DEMO-STOCK-INIT', 'Stock initial demo soutenance', '2025-11-25T09:00:00', @now, @now, 9906, @adminUserId),
        (9907, 'ENTREE', 20, 'CA-DEMO-STOCK-INIT', 'Stock initial demo soutenance', '2026-01-20T09:00:00', @now, @now, 9907, @adminUserId),
        (9908, 'ENTREE', 90, 'CA-DEMO-STOCK-INIT', 'Stock initial demo soutenance', '2025-09-13T09:00:00', @now, @now, 9908, @adminUserId),
        (9909, 'ENTREE', 390, 'CA-DEMO-STOCK-INIT', 'Stock initial demo soutenance', '2025-09-13T09:00:00', @now, @now, 9909, @adminUserId),
        (9910, 'ENTREE', 35, 'CA-DEMO-STOCK-INIT', 'Stock initial demo soutenance', '2026-01-20T09:00:00', @now, @now, 9910, @adminUserId),
        (9921, 'ENTREE', 30, 'CA-DEMO-202508-007', 'Entree laptops compacts', '2025-08-14T09:00:00', @now, @now, 9911, @adminUserId),
        (9922, 'ENTREE', 52, 'CA-DEMO-202508-007', 'Entree ecrans LED', '2025-08-14T09:00:00', @now, @now, 9912, @adminUserId),
        (9923, 'ENTREE', 120, 'CA-DEMO-202510-008', 'Entree packs clavier souris', '2025-10-15T09:00:00', @now, @now, 9913, @adminUserId),
        (9924, 'ENTREE', 28, 'CA-DEMO-202512-009', 'Entree SSD externes', '2025-12-19T09:00:00', @now, @now, 9914, @adminUserId),
        (9925, 'ENTREE', 48, 'CA-DEMO-202512-009', 'Entree switchs reseau', '2025-12-19T09:00:00', @now, @now, 9915, @adminUserId),
        (9926, 'ENTREE', 20, 'CA-DEMO-202512-009', 'Entree cameras IP', '2025-12-19T09:00:00', @now, @now, 9916, @adminUserId),
        (9927, 'ENTREE', 25, 'CA-DEMO-202602-010', 'Entree terminaux paiement', '2026-02-13T09:00:00', @now, @now, 9917, @adminUserId),
        (9928, 'ENTREE', 260, 'CA-DEMO-202510-008', 'Entree rouleaux thermiques', '2025-10-15T09:00:00', @now, @now, 9918, @adminUserId),
        (9929, 'ENTREE', 20, 'CA-DEMO-202510-008', 'Entree fauteuils bureau', '2025-10-15T09:00:00', @now, @now, 9919, @adminUserId),
        (9930, 'ENTREE', 10, 'CA-DEMO-202604-011', 'Entree projecteurs HD', '2026-04-18T09:00:00', @now, @now, 9920, @adminUserId),
        (9911, 'SORTIE', 21, 'VENTES-DEMO-LAPTOP', 'Ventes demo cumulees', '2026-06-02T16:30:00', @now, @now, 9901, @adminUserId),
        (9912, 'SORTIE', 15, 'VENTES-DEMO-PRINT', 'Ventes demo cumulees', '2026-05-10T16:30:00', @now, @now, 9902, @adminUserId),
        (9913, 'SORTIE', 42, 'VENTES-DEMO-ROUTER', 'Ventes demo cumulees', '2026-06-02T16:30:00', @now, @now, 9903, @adminUserId),
        (9914, 'SORTIE', 14, 'VENTES-DEMO-SCAN', 'Ventes demo cumulees', '2026-04-22T16:30:00', @now, @now, 9904, @adminUserId),
        (9915, 'SORTIE', 210, 'VENTES-DEMO-PAPER', 'Ventes demo cumulees', '2026-05-10T16:30:00', @now, @now, 9905, @adminUserId),
        (9916, 'SORTIE', 78, 'VENTES-DEMO-TONER', 'Ventes demo cumulees', '2026-06-02T16:30:00', @now, @now, 9906, @adminUserId),
        (9917, 'SORTIE', 5, 'VENTES-DEMO-POS', 'Ventes demo cumulees', '2026-05-25T16:30:00', @now, @now, 9907, @adminUserId),
        (9918, 'SORTIE', 46, 'VENTES-DEMO-SUPPORT', 'Ventes demo cumulees', '2026-04-09T16:30:00', @now, @now, 9908, @adminUserId),
        (9919, 'SORTIE', 280, 'VENTES-DEMO-ETIQ', 'Ventes demo cumulees', '2026-06-02T16:30:00', @now, @now, 9909, @adminUserId),
        (9920, 'SORTIE', 14, 'VENTES-DEMO-UPS', 'Ventes demo cumulees', '2026-05-25T16:30:00', @now, @now, 9910, @adminUserId),
        (9931, 'SORTIE', 11, 'VENTES-DEMO-LAPTOP13', 'Ventes demo cumulees', '2026-06-10T16:30:00', @now, @now, 9911, @adminUserId),
        (9932, 'SORTIE', 20, 'VENTES-DEMO-SCREEN24', 'Ventes demo cumulees', '2026-04-30T16:30:00', @now, @now, 9912, @adminUserId),
        (9933, 'SORTIE', 25, 'VENTES-DEMO-KBMOUSE', 'Ventes demo cumulees', '2026-06-10T16:30:00', @now, @now, 9913, @adminUserId),
        (9934, 'SORTIE', 4, 'VENTES-DEMO-SSD1T', 'Ventes demo cumulees', '2026-04-30T16:30:00', @now, @now, 9914, @adminUserId),
        (9935, 'SORTIE', 10, 'VENTES-DEMO-SWITCH8', 'Ventes demo cumulees', '2026-06-10T16:30:00', @now, @now, 9915, @adminUserId),
        (9936, 'SORTIE', 4, 'VENTES-DEMO-CAMIP', 'Ventes demo cumulees', '2026-05-28T16:30:00', @now, @now, 9916, @adminUserId),
        (9937, 'SORTIE', 3, 'VENTES-DEMO-TPE4G', 'Ventes demo cumulees', '2026-05-28T16:30:00', @now, @now, 9917, @adminUserId),
        (9938, 'SORTIE', 80, 'VENTES-DEMO-ROLLTPE', 'Ventes demo cumulees', '2026-05-28T16:30:00', @now, @now, 9918, @adminUserId),
        (9939, 'SORTIE', 6, 'VENTES-DEMO-CHAIR01', 'Ventes demo cumulees', '2026-05-28T16:30:00', @now, @now, 9919, @adminUserId),
        (9940, 'SORTIE', 2, 'VENTES-DEMO-PROJHD', 'Ventes demo cumulees', '2026-06-10T16:30:00', @now, @now, 9920, @adminUserId);
    SET IDENTITY_INSERT stock_mouvement OFF;

    COMMIT TRANSACTION;

    PRINT 'Demo soutenance seed inserted successfully.';
    PRINT 'Login: username=admin.soutenance / password=1234';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;

    IF OBJECT_ID('entreprise', 'U') IS NOT NULL SET IDENTITY_INSERT entreprise OFF;
    IF OBJECT_ID('App_User', 'U') IS NOT NULL SET IDENTITY_INSERT App_User OFF;
    IF OBJECT_ID('categorie_article', 'U') IS NOT NULL SET IDENTITY_INSERT categorie_article OFF;
    IF OBJECT_ID('mesure', 'U') IS NOT NULL SET IDENTITY_INSERT mesure OFF;
    IF OBJECT_ID('categorie_fournisseur', 'U') IS NOT NULL SET IDENTITY_INSERT categorie_fournisseur OFF;
    IF OBJECT_ID('fournisseur', 'U') IS NOT NULL SET IDENTITY_INSERT fournisseur OFF;
    IF OBJECT_ID('categorie_client', 'U') IS NOT NULL SET IDENTITY_INSERT categorie_client OFF;
    IF OBJECT_ID('client', 'U') IS NOT NULL SET IDENTITY_INSERT client OFF;
    IF OBJECT_ID('article', 'U') IS NOT NULL SET IDENTITY_INSERT article OFF;
    IF OBJECT_ID('stock', 'U') IS NOT NULL SET IDENTITY_INSERT stock OFF;
    IF OBJECT_ID('article_vente', 'U') IS NOT NULL SET IDENTITY_INSERT article_vente OFF;
    IF OBJECT_ID('commande_achat', 'U') IS NOT NULL SET IDENTITY_INSERT commande_achat OFF;
    IF OBJECT_ID('ligne_commande_achat', 'U') IS NOT NULL SET IDENTITY_INSERT ligne_commande_achat OFF;
    IF OBJECT_ID('commande_vente', 'U') IS NOT NULL SET IDENTITY_INSERT commande_vente OFF;
    IF OBJECT_ID('ligne_commande_vente', 'U') IS NOT NULL SET IDENTITY_INSERT ligne_commande_vente OFF;
    IF OBJECT_ID('facture_achat', 'U') IS NOT NULL SET IDENTITY_INSERT facture_achat OFF;
    IF OBJECT_ID('facture_de_vente', 'U') IS NOT NULL SET IDENTITY_INSERT facture_de_vente OFF;
    IF OBJECT_ID('stock_mouvement', 'U') IS NOT NULL SET IDENTITY_INSERT stock_mouvement OFF;

    DECLARE @ErrorMessage NVARCHAR(4000) = ERROR_MESSAGE();
    DECLARE @ErrorSeverity INT = ERROR_SEVERITY();
    DECLARE @ErrorState INT = ERROR_STATE();
    RAISERROR(@ErrorMessage, @ErrorSeverity, @ErrorState);
END CATCH;
