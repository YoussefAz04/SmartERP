# Fiche soutenance - partie IA / analytique

## 1. Pitch tres court

L'application est un Business Manager avec un module analytique/IA. Ce module transforme les donnees de vente, stock, achats et fournisseurs en:

- KPI de pilotage: CA, commandes, panier moyen, marge, alertes stock.
- Analyse de ventes: tendance mensuelle, croissance, anomalies.
- Prevision de CA: prochain mois, 3 prochains mois, 6 prochains mois.
- Intelligence produits: top produits, produits lents, familles de produits.
- Recommandations strategiques: risques de rupture, marge faible, ventes faibles, fournisseur a risque, tresorerie.
- Suivi de precision: les predictions sont enregistrees puis comparees au CA reel quand le mois est termine.

Phrase simple a dire:

> "La partie IA ne se contente pas d'afficher des statistiques. Elle apprend des historiques de vente, choisit un modele adapte selon la quantite de donnees, genere des previsions avec intervalles, puis transforme les resultats en recommandations metier actionnables."

## 2. Architecture generale

Stack:

- Frontend: Angular.
- Backend: Spring Boot Java 17.
- Base de donnees: SQL Server via Spring Data JPA.
- Librairie ML: Smile (`smile-core`), declaree dans `backend/pom.xml`.

Flux principal:

1. L'utilisateur ouvre le tableau de bord IA Angular.
2. `AiDashboardComponent` appelle `AiIntelligenceService.getAiDashboard()`.
3. Le frontend fait un GET vers `/api/analytics/dashboard`.
4. `AnalyticsController` extrait l'entreprise depuis le token JWT.
5. `AiDashboardService` orchestre tous les calculs.
6. Les services metier et ML consultent les repositories.
7. Le backend retourne un `AiDashboardDto`.
8. Angular construit les graphiques ApexCharts et les cartes KPI.

Fichiers importants:

- Front service API: `frontend/src/app/core/service/ai-intelligence/ai-intelligence.service.ts`
- Front ecran IA: `frontend/src/app/core-component/ai-intelligence/ai-dashboard/ai-dashboard.component.ts`
- Controleur REST: `backend/src/main/java/BuissnesManager/BuissnesManager/controllers/AnalyticsController.java`
- Orchestrateur: `backend/src/main/java/BuissnesManager/BuissnesManager/service/AiDashboardService.java`
- Analyse ventes/produits: `backend/src/main/java/BuissnesManager/BuissnesManager/service/AnalyticService.java`
- Prevision: `backend/src/main/java/BuissnesManager/BuissnesManager/service/ForecastService.java`
- Moteur ML prevision: `backend/src/main/java/BuissnesManager/BuissnesManager/service/ml/MlForecastEngine.java`
- Recommandations: `backend/src/main/java/BuissnesManager/BuissnesManager/service/RecommendationService.java`
- Moteur ML recommandations: `backend/src/main/java/BuissnesManager/BuissnesManager/service/ml/MlRecommendationEngine.java`
- Scoring fournisseurs: `backend/src/main/java/BuissnesManager/BuissnesManager/service/ml/SupplierRatingEngine.java`
- Historique des predictions: `backend/src/main/java/BuissnesManager/BuissnesManager/service/PredictionLogService.java`

## 3. Endpoints IA

Controleur: `AnalyticsController`, route racine `/api/analytics`.

Endpoints:

- `GET /dashboard`: renvoie tout le tableau de bord IA.
- `GET /sales`: analyse des ventes.
- `GET /forecast`: prevision des ventes.
- `GET /products`: intelligence produits.
- `GET /recommendations`: recommandations strategiques.
- `GET /predictions`: historique et precision des predictions.
- `POST /predictions/reconcile`: compare predictions terminees avec CA reel.

Securite:

- Le controleur est protege par `@PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")`.
- L'entreprise est extraite du token JWT via `jwtUtil.getEntrepriseIdFromToken(...)`.
- Cela evite qu'un utilisateur voie les donnees d'une autre entreprise.

## 4. Orchestration du dashboard

`AiDashboardService.getAiDashboard(entrepriseId)` construit l'objet complet:

- `analyticService.analyzeSales(...)`
- `forecastService.generateForecast(...)`
- `analyticService.analyzeProducts(...)`
- `recommendationService.generateRecommendations(...)`
- KPI summary
- comparaisons MTD/YTD
- details KPI
- performance fournisseurs

Idee importante:

> "Le dashboard separe l'orchestration et les algorithmes. Le service dashboard assemble les resultats, mais les calculs specialises sont dans des services dedies."

## 5. Analyse des ventes

Service: `AnalyticService.analyzeSales`.

Donnees:

- CA mensuel de l'annee courante via `commandeVenteRepository.getMonthlySales`.

Calculs:

- CA total.
- CA moyen sur les mois actifs.
- meilleur mois.
- croissance estimee.
- courbe de tendance.
- anomalies.

Techniques IA/statistiques:

- LOESS pour lisser la tendance mensuelle.
- Regression lineaire OLS sur le log du CA pour estimer la croissance.
- Bootstrap pour donner une fourchette d'incertitude.
- Detection d'anomalies via residus autour de la tendance.

Phrase orale:

> "Pour ne pas reagir uniquement au bruit mensuel, je lisse la courbe avec LOESS. Ensuite j'estime la croissance avec une regression log-lineaire, ce qui est adapte a une evolution proportionnelle du chiffre d'affaires."

## 6. Intelligence produits

Service: `AnalyticService.analyzeProducts`.

Donnees:

- Top produits vendus: nom, quantite vendue, CA.
- Performance par categorie.
- Nombre de SKU en stock.

Calculs:

- classement des produits.
- part de CA par produit.
- top 10.
- produits a rotation lente.
- performance par categorie.

IA:

- Si assez de produits, clustering K-Means.
- Features utilisees: revenu, quantite vendue, valeur moyenne.
- Les donnees sont standardisees avant K-Means.
- Le meilleur nombre de clusters est choisi parmi plusieurs candidats avec un score de silhouette.

Phrase orale:

> "Pour classer les produits, je ne me limite pas a un tri. Quand le volume de donnees est suffisant, j'utilise K-Means pour segmenter les produits selon leur performance commerciale."

## 7. Prevision des ventes

Services:

- `ForecastService`
- `MlForecastEngine`

Donnees:

- CA mensuel de l'annee courante.
- CA mensuel de l'annee precedente si disponible.

Strategie:

- Si l'historique a au moins 12 observations, le moteur ML est utilise.
- Sinon, le systeme utilise une methode Holt/Holt-Winters plus simple.
- Les resultats sont mis en cache par jour avec `@Cacheable`.

### Mode ML

Moteur: `MlForecastEngine`.

Etapes:

1. Nettoyage des outliers dans `ForecastService` via `SeriesCleaner`.
2. Transformation Box-Cox si assez de donnees, pour stabiliser la variance.
3. Construction d'une matrice de features.
4. Entrainement de plusieurs modeles candidats.
5. Validation walk-forward.
6. Choix du meilleur modele selon MAPE, sMAPE et MASE.
7. Prevision recursive sur 6 mois.
8. Intervalles de prediction par bootstrap des residus.

Features:

- `lag1`: CA du mois precedent.
- `lag2`: CA deux mois avant si serie large.
- `t`: tendance temporelle.
- `sin_m`, `cos_m`: saisonnalite annuelle.
- `sin_2m`, `cos_2m`: saisonnalite semestrielle si assez de donnees.
- features calendrier via `CalendarFeatureService`.

Modeles candidats:

- OLS.
- Ridge.
- ElasticNet.
- RandomForest si au moins 36 observations.
- Naive flat en fallback si serie degeneree.

Validation:

- Walk-forward: on entraine sur le passe et on predit le futur proche, comme dans un vrai scenario.
- Metriques: MAPE, sMAPE, RMSE, MASE.

Phrase orale:

> "J'ai evite de valider le modele sur les memes donnees que l'entrainement. La validation walk-forward respecte l'ordre temporel: on entraine sur les mois passes et on teste sur les mois suivants."

### Mode fallback Holt/Holt-Winters

Si le ML ne peut pas apprendre:

- Holt lineaire si peu d'historique.
- Holt-Winters avec saisonnalite multiplicative si l'annee precedente existe.
- Indices saisonniers bornes entre 0.60 et 1.50 pour eviter les predictions extremes.
- Tendance limitee pour eviter une croissance irrealiste.

Phrase orale:

> "Le fallback est volontaire: avec peu de donnees, un modele complexe peut sur-apprendre. Holt/Holt-Winters donne une prevision plus stable."

## 8. Recommandations strategiques

Service: `RecommendationService`.

Deux types:

1. Recommandations ML:
   - risque de rupture stock avec regression logistique.
   - opportunites produits avec regression OLS.

2. Recommandations par regles metier:
   - stock critique.
   - stock faible.
   - ventes du mois en retard.
   - marge faible.
   - commandes fournisseurs en retard.
   - concentration clients.
   - dependance categorie/fournisseur.
   - panier moyen en baisse.
   - tresorerie.
   - donnees insuffisantes.

Risque de rupture:

- Donnees par article: stock actuel, demande moyenne mensuelle, variance, delai fournisseur, jours de couverture.
- Label provisoire: risque si jours de couverture < horizon 30 jours + delai fournisseur.
- Modele: regression logistique Smile.
- Sortie: probabilite de rupture.
- Le systeme recommande une quantite a commander.

Opportunites produits:

- Modele OLS.
- Features: quantite, marge estimee, part categorie, tendance.
- Sortie: produits a mettre en avant.

Phrase orale:

> "Les recommandations combinent ML et regles metier. Le ML detecte les risques et opportunites, puis les regles metier traduisent ces signaux en actions compréhensibles pour un gestionnaire."

## 9. Performance fournisseurs

Service: `AiDashboardService.buildSupplierPerformanceMetrics`.
Moteur: `SupplierRatingEngine`.

Donnees:

- nombre de commandes fournisseur.
- total depense.
- taux de livraison.
- ratio d'annulation.
- details par statut.

Methode:

- Si moins de 4 fournisseurs: scoring simple base sur le taux de livraison.
- Sinon: OLS peer-group.
- Le modele estime le taux de livraison attendu selon le volume de commandes, le spend et les annulations.
- La note en etoiles depend de l'ecart entre performance reelle et performance attendue.

Phrase orale:

> "La note fournisseur n'est pas seulement une moyenne brute. Elle compare le fournisseur a un groupe comparable, pour tenir compte du volume de commandes et du niveau d'activite."

## 10. Historique et precision des predictions

Service: `PredictionLogService`.

Principe:

- Quand une prevision est generee, elle est sauvegardee dans `prediction_log`.
- Une cle unique evite les doublons pour la meme entreprise, le meme mois cible et le meme horizon.
- Quand le mois cible est termine, `reconcile` charge le CA reel et calcule:
  - erreur absolue.
  - pourcentage d'erreur.
  - sMAPE.
  - biais.
  - couverture des intervalles.

Il existe aussi une reconciliation programmee:

- `@Scheduled(cron = "0 0 2 2 * *")`: le 2 de chaque mois a 02h00.

Phrase orale:

> "J'ai ajoute une boucle de feedback: la prediction est stockee, puis comparee au reel. Cela permet d'evaluer la qualite du modele dans le temps."

## 11. Frontend IA

Service Angular:

- `AiIntelligenceService`
- Base URL: `${environment.apiUrl}/api/analytics`
- Ajoute le Bearer token dans les headers.

Composant principal:

- `AiDashboardComponent`
- `ngOnInit()` appelle `loadDashboard()`.
- `loadDashboard()` recupere le dashboard.
- `buildCharts()` construit:
  - graphique ventes reelles + tendance.
  - graphique prevision + bornes haute/basse.
  - donut top produits.

Navigation depuis recommandations:

- Le bouton d'une recommandation redirige vers l'ecran utile:
  - stock.
  - commandes vente.
  - articles.
  - commandes achat.
  - clients.

Phrase orale:

> "Le frontend ne refait pas les calculs IA. Il consomme les DTO du backend et les transforme en graphiques, cartes KPI et actions de navigation."

## 12. Limites a reconnaitre intelligemment

Reponses honnetes:

- "La qualite des predictions depend de la qualite et du volume des donnees."
- "Avec peu d'historique, le systeme bascule vers un modele plus simple pour eviter le sur-apprentissage."
- "Les recommandations sont une aide a la decision, pas une decision automatique."
- "Le systeme ne remplace pas l'expertise humaine: il priorise les risques et opportunites."
- "MAPE peut etre instable quand le CA reel est proche de zero, donc j'utilise aussi sMAPE et MASE."

## 13. Questions probables du jury

### Pourquoi utiliser Smile ?

Smile est une librairie Java de machine learning. Elle s'integre directement au backend Spring Boot, sans lancer un service Python separe. Cela simplifie le deploiement et garde toute la logique cote backend.

### Pourquoi plusieurs modeles ?

Parce qu'un seul modele ne marche pas toujours sur toutes les entreprises. Le moteur teste plusieurs candidats et choisit le meilleur selon une validation temporelle.

### Pourquoi un fallback ?

Parce qu'un modele ML complexe avec peu de donnees peut donner des resultats dangereux. Le fallback Holt/Holt-Winters est plus robuste sur petites series.

### Comment eviter l'overfitting ?

- validation walk-forward.
- regularisation Ridge/ElasticNet.
- selection par metriques hors echantillon.
- garde anti-overfit pour RandomForest.
- fallback si serie degeneree.

### Quelles donnees personnelles sont utilisees ?

La partie IA travaille surtout sur donnees business agregees: ventes, achats, stock, commandes, fournisseurs. L'acces est limite par le token JWT et l'entreprise extraite du token.

### Que signifie le score de fiabilite ?

C'est un score combine qui tient compte de la densite des donnees, de la qualite de validation du modele et de la stabilite des residus. Il indique le niveau de confiance, mais ce n'est pas une garantie absolue.

### Quelle est la difference entre analyse et prediction ?

L'analyse explique ce qui s'est passe: CA, tendance, top produits. La prediction estime ce qui va probablement arriver: CA futur, risque de rupture, opportunites.

## 14. Mini script de demonstration

1. "Je me connecte; le JWT contient l'entreprise."
2. "J'ouvre le tableau de bord analytique."
3. "Angular appelle `/api/analytics/dashboard`."
4. "Le backend extrait l'entreprise du token."
5. "Le dashboard service lance les analyses: ventes, previsions, produits, recommandations."
6. "La prevision choisit ML si assez de donnees, sinon Holt/Holt-Winters."
7. "Les recommandations transforment les signaux en actions: stock, marge, fournisseur, ventes."
8. "Le frontend affiche les graphiques et permet d'aller directement vers les ecrans metier."
9. "Les predictions sont stockees puis comparees au reel pour mesurer la precision."

## 15. Ce qu'il faut retenir par coeur

- Endpoint principal: `/api/analytics/dashboard`.
- Orchestrateur: `AiDashboardService`.
- Prevision: `ForecastService` + `MlForecastEngine`.
- Donnees prevision: CA mensuel.
- ML: features lag, tendance, saisonnalite, calendrier.
- Modeles: OLS, Ridge, ElasticNet, RandomForest, fallback Holt.
- Validation: walk-forward.
- Recommandations: ML + regles metier.
- Suivi qualite: `PredictionLogService`.
- Frontend: Angular affiche, backend calcule.

