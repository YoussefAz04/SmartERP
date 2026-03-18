// kanban-tache.component.ts
import {Component, OnInit} from '@angular/core';
import {TacheService} from "../../../core/service/taches/tache.service";
import {StatutTache, Tache} from "../../../model/tache.model";
import {Board} from "../../../model/board";
import {Column} from "../../../model/column.model";
import {
  CdkDrag,
  CdkDragDrop,
  CdkDropList,
  CdkDropListGroup,
  moveItemInArray,
  transferArrayItem
} from "@angular/cdk/drag-drop";
import {DatePipe, NgForOf, NgIf} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {Projet} from "../../../model/projet.model";
import {ProjetService} from "../../../core/service/projet/projet.service";
import {forkJoin} from 'rxjs';
import {Router} from "@angular/router";

@Component({
  selector: 'app-kanban-tache',
  standalone: true,
  imports: [
    CdkDropList,
    NgForOf,
    CdkDrag,
    CdkDropListGroup,
    NgIf,
    DatePipe,
    FormsModule
  ],
  templateUrl: './kanban-tache.component.html',
  styleUrl: './kanban-tache.component.scss'
})
export class KanbanTacheComponent implements OnInit {
  taches: Tache[] = [];
  board!: Board;
  idProjet: number | string = '';
  projets: Projet[] = [];
  isLoading = false;
  error: string | null = null;

  // Define column configurations
  private readonly columnConfigs = [
    { key: StatutTache.A_FAIRE, name: 'À faire', color: '#e3f2fd' },
    { key: StatutTache.EN_COURS, name: 'En cours', color: '#fff3e0' },
    { key: StatutTache.BLOQUEE, name: 'Bloquée', color: '#ffebee' },
    { key: StatutTache.TERMINEE, name: 'Terminée', color: '#e8f5e8' }
  ];

  constructor(
    private tacheService: TacheService,
    private projetService: ProjetService ,
    private router : Router
  ) {}

  ngOnInit(): void {
    this.initializeData();
  }


  private initializeData(): void {
    this.isLoading = true;
    this.error = null;

    // Load projects and initial tasks in parallel
    forkJoin({
      projets: this.projetService.getAllProjets(),
      taches: this.tacheService.getAllTaches()
    }).subscribe({
      next: ({ projets, taches }) => {
        this.projets = projets;
        this.taches = taches;
        this.buildBoard();
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading initial data:', err);
        this.error = 'Erreur lors du chargement des données';
        this.isLoading = false;
      }
    });
  }

  onProjetChange(): void {
    if (this.idProjet === '') {
      this.loadAllTaches();
    } else {
      this.getAllTacheByProjet();
    }
  }


  private loadAllTaches(): void {
    this.isLoading = true;
    this.tacheService.getAllTaches().subscribe({
      next: (data) => {
        this.taches = data;
        this.buildBoard();
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading all tasks:', err);
        this.error = 'Erreur lors du chargement des tâches';
        this.isLoading = false;
      }
    });
  }


  private getAllTacheByProjet(): void {
    if (!this.idProjet) return;

    this.isLoading = true;
    const projetId = typeof this.idProjet === 'string' ? parseInt(this.idProjet) : this.idProjet;

    this.tacheService.getAllTachesByProjet(projetId).subscribe({
      next: (data) => {
        this.taches = data;
        this.buildBoard();
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading tasks by project:', err);
        this.error = 'Erreur lors du chargement des tâches du projet';
        this.isLoading = false;
      }
    });
  }


  private buildBoard(): void {
    const columns = this.columnConfigs.map(config =>
      new Column(
        config.name,
        this.taches.filter(tache => tache.statut === config.key),
      )
    );

    this.board = new Board('Kanban Diagramme', columns);
  }


  drop(event: CdkDragDrop<Tache[]>): void {
    if (event.previousContainer === event.container) {
      // Reorder within the same column
      moveItemInArray(
        event.container.data,
        event.previousIndex,
        event.currentIndex
      );
    } else {
      // Move between columns
      this.moveTaskBetweenColumns(event);
    }
  }


  private moveTaskBetweenColumns(event: CdkDragDrop<Tache[]>): void {
    const movedTask = event.previousContainer.data[event.previousIndex];

    // Transfer the task
    transferArrayItem(
      event.previousContainer.data,
      event.container.data,
      event.previousIndex,
      event.currentIndex
    );

    // Find target column and update task status
    const targetColumn = this.board.columns.find(
      column => column.tasks === event.container.data
    );

    if (targetColumn && movedTask.id) {
      const newStatus = this.getStatusFromColumnName(targetColumn.name);
      if (newStatus) {
        this.updateTaskStatus(movedTask, newStatus);
      }
    }
  }


  private getStatusFromColumnName(columnName: string): StatutTache | null {
    const config = this.columnConfigs.find(config => config.name === columnName);
    return config ? config.key : null;
  }

  private updateTaskStatus(task: Tache, newStatus: StatutTache): void {
    const previousStatus = task.statut;
    task.statut = newStatus;

    this.tacheService.updateTacheStatut(task.id!, newStatus).subscribe({
      next: () => {
        console.log(`Tâche ${task.id} mise à jour: ${previousStatus} → ${newStatus}`);
      },
      error: (err) => {
        console.error('Erreur de mise à jour du statut:', err);
        // Revert the status change on error
        task.statut = previousStatus;
        this.buildBoard(); // Refresh the board to reflect the revert
        this.error = 'Erreur lors de la mise à jour du statut de la tâche';
      }
    });
  }


  getSelectedProjetName(): string {
    if (!this.idProjet) return 'Tous les projets';

    const projet = this.projets.find(p => p.id === Number(this.idProjet));
    return projet ? projet.nom : 'Projet inconnu';
  }




  clearError(): void {
    this.error = null;
  }

  addTacheProjet() {
    this.router.navigate(['projet/addTache',this.idProjet])
  }

}
