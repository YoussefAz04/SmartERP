// barres-verticales-projet.component.ts
import {Component, OnInit} from '@angular/core';
import {ProjetService} from "../../../core/service/projet/projet.service";
import {NgForOf, NgIf} from "@angular/common";
import { forkJoin, map } from 'rxjs';

export interface ProjetTache {
  projetId: number;
  nombreTaches: number;
  percentage: number;
}

@Component({
  selector: 'app-barres-verticales-projet',
  standalone: true,
  imports: [NgForOf, NgIf],
  templateUrl: './barres-verticales-projet.component.html',
  styleUrl: './barres-verticales-projet.component.scss'
})
export class BarresVerticalesProjetComponent implements OnInit {
  projetsTaches: ProjetTache[] = [];
  isLoading = true;
  hasError = false;
  maxTaches = 0;

  constructor(private projetService: ProjetService) {}

  ngOnInit(): void {
    this.loadData();
  }

  private loadData(): void {
    this.isLoading = true;
    this.hasError = false;

    this.projetService.getAllProjets().subscribe({
      next: (projets) => {
        const projetsIds = projets
          .map(p => p.id)
          .filter(id => id != null) as number[];

        if (projetsIds.length === 0) {
          this.isLoading = false;
          return;
        }

        // Utilisation de forkJoin pour charger toutes les données en parallèle
        const requests = projetsIds.map(id =>
          this.projetService.nombreDeTacheByProjet(id).pipe(
            map(count => ({ projetId: id, nombreTaches: count, percentage: 0 }))
          )
        );

        forkJoin(requests).subscribe({
          next: (results) => {
            this.projetsTaches = results.sort((a, b) => a.projetId - b.projetId);
            this.calculatePercentages();
            this.isLoading = false;
          },
          error: (err) => {
            console.error('Erreur lors du chargement des données', err);
            this.hasError = true;
            this.isLoading = false;
          }
        });
      },
      error: (err) => {
        console.error('Erreur chargement projets', err);
        this.hasError = true;
        this.isLoading = false;
      }
    });
  }

  private calculatePercentages(): void {
    this.maxTaches = Math.max(...this.projetsTaches.map(pt => pt.nombreTaches));
    this.projetsTaches.forEach(pt => {
      pt.percentage = this.maxTaches > 0 ? (pt.nombreTaches / this.maxTaches) * 100 : 0;
    });
  }

  retry(): void {
    this.loadData();
  }

  protected readonly Math = Math;
}
