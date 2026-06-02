# SmartERP - ERP Intelligent

SmartERP est une application web ERP développée dans le cadre d'un projet de fin d'etudes. Elle vise a aider les petites et moyennes entreprises a gérer leurs opérations commerciales quotidiennes tout en exploitant les données pour améliorer la prise de décision.

Le projet couvre la gestion des utilisateurs, des permissions, des clients, des fournisseurs, des articles, des commandes, des factures, du stock, des notifications, de l'audit et d'un module d'intelligence artificielle pour l'analyse commerciale, la prévision des ventes et les recommandations d'achat.

## Objectif du projet

L'objectif principal est de construire une plateforme ERP moderne, sécurisée et intelligente. L'application ne se limite pas a enregistrer les opérations : elle permet aussi de suivre le stock en temps réel, de garder une trace des actions sensibles et d'exploiter les données commerciales pour aider les responsables a anticiper l'activité.

Concrètement, SmartERP permet de :

- gérer plusieurs entreprises depuis une même plateforme ;
- créer des administrateurs et des employés avec des permissions précises ;
- gérer les clients, fournisseurs, articles d'achat et articles de vente ;
- créer des commandes d'achat et de vente ;
- générer des factures ;
- suivre le stock et les mouvements de stock ;
- recevoir des notifications importantes, comme les alertes de stock faible ;
- consulter un journal d'audit pour la traçabilité ;
- analyser les ventes et prévoir leur évolution ;
- obtenir des recommandations d'achat basées sur les données.

## Architecture générale

Le projet est organisé en deux parties principales :

- `backend` : API REST développée avec Spring Boot.
- `frontend` : interface web développée avec Angular.

La base de données utilisée est SQL Server.

L'architecture suit une logique en couches :

- Couche présentation : Angular affiche les pages, formulaires, tableaux, tableaux de bord et résultats du module IA.
- Couche contrôleur : Spring Boot expose les endpoints REST et reçoit les requêtes du frontend.
- Couche service : contient la logique métier, par exemple les règles de stock, de facturation, de permissions et d'intelligence artificielle.
- Couche repository : communique avec SQL Server via Spring Data JPA.
- Couche sécurité : gère l'authentification JWT, les rôles et les permissions.

## Technologies utilisées

### Backend

- Java 17
- Spring Boot 3
- Spring Security
- Spring Data JPA
- SQL Server
- JWT
- Lombok
- Springdoc OpenAPI
- SMILE pour certaines parties de Machine Learning

### Frontend

- Angular 17
- TypeScript
- Angular Material
- Bootstrap
- PrimeNG
- ApexCharts
- SweetAlert2
- jsPDF / pdf-lib

## Structure du backend

Le backend se trouve dans le dossier `backend`.

Les dossiers les plus importants sont :

- `controllers` : contient les contrôleurs REST. Chaque contrôleur reçoit les requêtes HTTP et appelle le service correspondant.
- `service` : contient la logique métier de l'application.
- `service/ml` : contient les classes liées a l'intelligence artificielle et a la prévision.
- `repository` : contient les interfaces Spring Data JPA pour accéder a la base de données.
- `entity` : contient les entités métier persistées en base de données.
- `dto` : contient les objets de transfert utilisés entre le frontend et le backend.
- `security` : contient la configuration de sécurité, la gestion JWT et les règles d'accès.
- `config` : contient les fichiers de configuration technique.
- `exception` : contient la gestion des erreurs applicatives.

Quelques contrôleurs importants :

- `AuthController` : connexion, authentification et gestion des tokens.
- `UserController` : gestion des utilisateurs.
- `EntrepriseController` : gestion des entreprises.
- `ClientController` : gestion des clients.
- `FournisseurController` : gestion des fournisseurs.
- `ArticleAchatController` et `ArticleVenteController` : gestion des articles.
- `CommandeAchatController` et `CommandeVenteController` : gestion des commandes.
- `FactureController` et `FactureDeVenteController` : gestion des factures.
- `StockController` et `StockMouvementController` : stock et mouvements.
- `NotificationController` : notifications.
- `AuditLogController` : journal d'audit.
- `AnalyticsController` : analyses et données du module IA.

## Structure du frontend

Le frontend se trouve dans le dossier `frontend`.

Les dossiers les plus importants sont :

- `auth` : pages et logique liées a l'authentification.
- `core` : services, guards, interceptors et éléments techniques partagés.
- `core-component` : pages principales de l'application.
- `common-component` : composants réutilisables.
- `model` : modèles TypeScript utilisés dans l'interface.
- `shared` : éléments partagés entre plusieurs modules.

Le frontend communique avec le backend a travers des services Angular. Les requêtes envoyées au backend incluent le token JWT pour permettre au backend de vérifier l'utilisateur connecté et ses permissions.

## Fonctionnement global

Le fonctionnement général de l'application suit ce parcours :

1. L'utilisateur se connecte depuis l'interface Angular.
2. Le backend vérifie les identifiants.
3. Si les informations sont correctes, le backend génère un token JWT.
4. Le frontend utilise ce token pour les requêtes suivantes.
5. A chaque requête, Spring Boot vérifie le token, le rôle, les permissions et l'entreprise de l'utilisateur.
6. Le service métier exécute le traitement demandé.
7. Les données sont lues ou enregistrées dans SQL Server.
8. Le résultat est renvoyé au frontend et affiché a l'utilisateur.

Cette logique permet de sécuriser l'accès aux modules et de garantir que chaque entreprise consulte uniquement ses propres données.

## Rôles et permissions

L'application distingue principalement :

- Super Admin : gère les entreprises et peut affecter un administrateur a une entreprise.
- Administrateur : gère les utilisateurs de son entreprise et configure les permissions.
- Employé : accède uniquement aux modules autorisés par l'administrateur.

Les permissions permettent de contrôler l'accès aux modules comme les clients, fournisseurs, articles, commandes, factures, stock et autres fonctionnalités métier.

## Gestion commerciale

La partie commerciale couvre :

- les clients ;
- les fournisseurs ;
- les catégories ;
- les articles d'achat ;
- les articles de vente ;
- les commandes d'achat ;
- les commandes de vente ;
- les factures d'achat ;
- les factures de vente.

Lorsqu'une commande ou une facture est créée, le backend vérifie les données, calcule les montants nécessaires et enregistre les informations dans la base de données.

## Stock et mouvements

Le module stock permet de suivre les quantités disponibles des articles.

Chaque stock est lié a un article et a une entreprise. Le système peut afficher :

- la quantité entrée ;
- la quantité disponible ;
- la quantité réservée ;
- la quantité vendue ;
- le seuil minimum ;
- l'état du stock.

Les mouvements de stock permettent de comprendre l'origine des variations. Par exemple, une commande d'achat peut augmenter le stock, tandis qu'une vente peut le diminuer selon la règle métier appliquée.

Le système peut aussi générer une notification lorsqu'un article atteint un seuil faible.

## Notifications et audit

Les notifications servent a informer les utilisateurs des événements importants : stock faible, actions sur les commandes, ajout d'articles ou autres alertes utiles.

Le journal d'audit garde une trace des actions sensibles. Il permet de savoir qui a fait quoi, quand, dans quelle entreprise et sur quel module. Cette partie renforce la traçabilité et la sécurité de l'application.

## Module d'intelligence artificielle

Le module IA exploite les données commerciales et les données de stock afin de produire des indicateurs d'aide a la décision.

Il permet notamment de :

- analyser les ventes ;
- calculer le chiffre d'affaires total ;
- déterminer la moyenne mensuelle ;
- identifier le meilleur mois ;
- calculer le taux de croissance ;
- générer des prévisions de ventes ;
- classer les produits selon leur performance ;
- évaluer les fournisseurs ;
- générer des recommandations d'achat.

Le backend récupère les données nécessaires depuis SQL Server, les prépare, puis applique les traitements statistiques ou Machine Learning selon la quantité et la qualité des données disponibles.

Les classes du dossier `service/ml` participent a cette partie :

- `SeriesCleaner` nettoie les séries de données.
- `BoxCox` transforme certaines valeurs pour stabiliser les variations.
- `LoessSmoother` aide a lisser les tendances.
- `FeatureScaler` normalise les variables.
- `CalendarFeatureService` ajoute des informations liées au calendrier.
- `MlForecastEngine` gère la prévision des ventes.
- `MlRecommendationEngine` aide a produire les recommandations.
- `SupplierRatingEngine` évalue les fournisseurs.

Si les données historiques sont suffisantes, le système peut utiliser une approche Machine Learning. Si les données sont faibles, il applique une méthode statistique plus simple afin de produire une estimation exploitable.

## Configuration

Le fichier principal de configuration backend se trouve ici :

`backend/src/main/resources/application.properties`

Avant de lancer le projet, il faut adapter :

- l'URL SQL Server ;
- le nom de la base de données ;
- l'utilisateur et le mot de passe SQL Server ;
- les paramètres mail si l'envoi d'emails est utilisé ;
- le secret JWT ;
- l'URL du frontend.

Important : les valeurs présentes dans ce fichier sont destinées au développement local ou a la démonstration. Pour un environnement réel, il faut remplacer les mots de passe et secrets par des variables d'environnement sécurisées.

## Lancement du backend

Prérequis :

- Java 17 installé.
- Maven installé.
- SQL Server démarré.
- Base de données créée.

Commandes :

```bash
cd backend
mvn spring-boot:run
```

Par défaut, le backend démarre sur :

```text
http://localhost:8020
```

Si Springdoc est actif, la documentation des API peut être consultée sur :

```text
http://localhost:8020/swagger-ui/index.html
```

## Lancement du frontend

Prérequis :

- Node.js installé.
- npm installé.

Commandes :

```bash
cd frontend
npm install
npm start
```

Par défaut, l'interface Angular démarre sur :

```text
http://localhost:4200
```

## Base de données

Le projet utilise SQL Server.

Le fichier `scriptYoussef.sql` peut servir a préparer la base de données si nécessaire. Il est aussi possible de laisser Hibernate mettre a jour la structure selon la configuration du projet.

La base utilisée dans la configuration locale s'appelle généralement :

```text
BusinessManager
```

## Build du projet

Pour générer le build backend :

```bash
cd backend
mvn clean package
```

Pour générer le build frontend :

```bash
cd frontend
npm run build
```

## Scénario de démonstration conseillé

Pour présenter le projet de manière claire, il est conseillé de suivre ce scénario :

1. Connexion a la plateforme.
2. Présentation des rôles : Super Admin, Administrateur et Employé.
3. Création ou consultation d'une entreprise.
4. Création d'un utilisateur et affectation des permissions.
5. Ajout de clients, fournisseurs et articles.
6. Création d'une commande d'achat.
7. Création d'une commande de vente.
8. Consultation des factures.
9. Consultation du stock et des mouvements.
10. Déclenchement ou consultation d'une notification.
11. Consultation du journal d'audit.
12. Consultation du tableau de bord IA.
13. Présentation des prévisions et recommandations d'achat.

Ce scénario montre le cycle complet : gestion métier, traçabilité, stock et aide a la décision.

## Points importants a expliquer pendant la soutenance

- Le frontend Angular est séparé du backend Spring Boot.
- Le backend expose des API REST sécurisées par JWT.
- Les permissions contrôlent l'accès aux modules.
- Les données sont isolées par entreprise.
- Le stock est mis a jour selon les opérations commerciales.
- Les mouvements expliquent l'origine des variations de stock.
- Les notifications alertent les utilisateurs sur les événements importants.
- L'audit conserve l'historique des actions sensibles.
- Le module IA exploite les données réelles de l'ERP pour produire des analyses, prévisions et recommandations.

## Auteur

Projet réalisé dans le cadre d'un Projet de Fin d'Études.

Nom du projet : SmartERP - ERP Intelligent  
Type : Application web ERP avec intelligence artificielle  
Stack : Angular, Spring Boot, SQL Server, JWT, SMILE
