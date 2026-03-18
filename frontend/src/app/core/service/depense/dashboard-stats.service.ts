// // dashboard-stats.service.ts
// import { Injectable } from "@angular/core";
// import { forkJoin, map, Observable } from "rxjs";
// import { DepenseService } from "./depense.service";
// import { BudgetService } from "../Budget/budget.service";
// import { BudgetPerformanceDto } from "../../../model/budgetPerformanceDto.model";
//
// export interface DashboardStats {
//   totalDepenses: number;
//   budgetTotal: number;
//   budgetRestant: number;
//   tauxUtilisation: number;
//   performance: BudgetPerformanceDto[];
//   depensesParCategorie: { categorie: string; montant: number }[];
//   evolutionMensuelle: { mois: string; montant: number }[];
// }
//
// @Injectable({
//   providedIn: "root"
// })
// export class DashboardStatsService {
//   constructor(
//     private depenseService: DepenseService,
//     private budgetService: BudgetService
//   ) {}
//
//   /**
//    * Récupère toutes les statistiques globales du dashboard pour un projet
//    */
//   getDashboardStats(idProjet: number): Observable<DashboardStats> {
//     return forkJoin({
//       totalDepenses: this.depenseService.getTotalDepenseByProjet(idProjet),
//       tauxUtilisation: this.depenseService.getTauxUtilisation(idProjet),
//       performance: this.depenseService.getPerformanceBudgets(),
//       depensesCategorie: this.depenseService.getDepenseByCategorie(0),
//       budget: this.budgetService.getBudgetsByProjet(idProjet)
//     }).pipe(
//       map(res => {
//         const budgetTotal = res.budget.montant;
//         const budgetRestant = budgetTotal - res.totalDepenses;
//
//         // Regrouper dépenses par catégorie
//         const depensesParCategorie: { [key: string]: number } = {};
//         res.depensesCategorie.forEach(d => {
//           const nomCat = d.categorieDepense?.nom || "Autres";
//           depensesParCategorie[nomCat] =
//             (depensesParCategorie[nomCat] || 0) + d.montant;
//         });
//
//         // Evolution mensuelle des dépenses
//         const evolutionMensuelle: { [mois: string]: number } = {};
//         res.depensesCategorie.forEach(d => {
//           const mois = new Date(d.dateDepense).toLocaleString("fr", {
//             month: "short",
//             year: "numeric"
//           });
//           evolutionMensuelle[mois] =
//             (evolutionMensuelle[mois] || 0) + d.montant;
//         });
//
//         return {
//           totalDepenses: res.totalDepenses,
//           budgetTotal,
//           budgetRestant,
//           tauxUtilisation: res.tauxUtilisation,
//           performance: res.performance,
//           depensesParCategorie: Object.entries(depensesParCategorie).map(
//             ([categorie, montant]) => ({ categorie, montant })
//           ),
//           evolutionMensuelle: Object.entries(evolutionMensuelle).map(
//             ([mois, montant]) => ({ mois, montant })
//           )
//         };
//       })
//     );
//   }
// }
