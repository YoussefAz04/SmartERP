import {Component, OnInit, ViewChild} from '@angular/core';
import {GanttModule} from "@syncfusion/ej2-angular-gantt";

import { TypeSousTache} from "../../../model/sousTache.model";
import {Tache} from "../../../model/tache.model";
import {DatePipe, NgForOf, NgIf} from "@angular/common";
import {ProjetService} from "../../../core/service/projet/projet.service";
import {TacheService} from "../../../core/service/taches/tache.service";
import {ActivatedRoute} from "@angular/router";
import {MatTooltip} from "@angular/material/tooltip";

@Component({
  selector: 'app-gantt',
  templateUrl: './gantt.component.html',
  styleUrl:'./gantt.component.scss',
  imports: [
    GanttModule,
    NgIf,
    DatePipe,
    NgForOf,
    MatTooltip
  ],
  standalone: true
})
export class GanttComponent implements OnInit {
  @ViewChild('gantt', { static: false }) ganttChart!: GanttComponent;

  taches: Tache[] = [];
  ganttData: any[] = [];
  showSubTasks = true;
  selectedTaskId: number | null = null;
  selectedTask: Tache | null = null;

  // Configuration Syncfusion Gantt
  public taskSettings: object = {};
  public columns: object[] = [];
  public timelineSettings: object = {};
  public labelSettings: object = {};
  public projectStartDate: Date = new Date('2025-07-01');
  public projectEndDate: Date = new Date('2025-10-30');
  public height: string = '500px';
  public gridLines: string = 'Both';
  public toolbar: string[] = ['Add', 'Edit', 'Update', 'Delete', 'Cancel', 'ExpandAll', 'CollapseAll', 'ZoomIn', 'ZoomOut', 'ZoomToFit'];

  constructor(private projetService: ProjetService,
              private tacheService: TacheService,
              private routerActivated : ActivatedRoute) {
    this.initializeGanttSettings();
  }
  idProjet !: number ;

  ngOnInit() {
    this.idProjet = this.routerActivated.snapshot.params['id'];
    this.loadSampleData();
  }

  initializeGanttSettings() {
    // Configuration des tâches
    this.taskSettings = {
      id: 'TaskID',
      name: 'TaskName',
      startDate: 'StartDate',
      endDate: 'EndDate',
      duration: 'Duration',
      progress: 'Progress',
      dependency: 'Predecessor',
      parentID: 'ParentID',
      child: 'SubTasks'
    };

    // Configuration des colonnes
    this.columns = [
      { field: 'TaskID', headerText: 'ID', width: '80' },
      { field: 'TaskName', headerText: 'Nom de la tâche', width: '250' },
      { field: 'StartDate', headerText: 'Date début', width: '120' },
      { field: 'EndDate', headerText: 'Date fin', width: '120' },
      { field: 'Duration', headerText: 'Durée', width: '100' },
      { field: 'Progress', headerText: 'Progrès', width: '100' },
      { field: 'Priority', headerText: 'Priorité', width: '100' },
      { field: 'Status', headerText: 'Statut', width: '100' }
    ];

    // Configuration de la timeline
    this.timelineSettings = {
      topTier: {
        unit: 'Month',
        format: 'MMM yyyy'
      },
      bottomTier: {
        unit: 'Week',
        format: 'dd'
      }
    };

    // Configuration des labels
    this.labelSettings = {
      leftLabel: 'TaskName',
      rightLabel: 'Progress'
    };
  }

  loadSampleData() {
    this.tacheService.getAllTachesByProjet(this.idProjet).subscribe(data => {
      this.taches = data;
      this.transformDataForGantt();
      this.calculateTimeRange();
    });
  }

  transformDataForGantt() {
    this.ganttData = this.taches.map(tache => {
      // Conversion des dates string en Date objects
      const startDate = new Date(tache.dateDebut);
      const endDate = new Date(tache.dateFin);

      const ganttTask = {
        TaskID: tache.id,
        TaskName: tache.titre,
        StartDate: startDate,
        EndDate: endDate,
        Duration: tache.duree,
        Progress: tache.progres,
        Predecessor: tache.predecesseur || null,
        Priority: tache.priorite,
        Status: tache.statut,
        Description: tache.description,
        SubTasks: [] as any[]
      };

      // Ajout des sous-tâches si elles existent
      if (tache.sousTaches && tache.sousTaches.length > 0) {
        ganttTask.SubTasks = tache.sousTaches.map(sousTache => ({
          TaskID: sousTache.id,
          TaskName: sousTache.titre,
          StartDate: new Date(sousTache.dateDebut),
          EndDate: new Date(sousTache.dateFin),
          Duration: sousTache.duree,
          Progress: sousTache.progres,
          ParentID: tache.id,
          Type: sousTache.type,
          Description: sousTache.description,
          Priority: this.getSubTaskPriority(sousTache.type),
          Status: this.getSubTaskStatus(sousTache.progres)
        }));
      }

      return ganttTask;
    });
  }

  calculateTimeRange() {
    if (this.taches.length === 0) return;

    const allDates: Date[] = [];
    this.taches.forEach(task => {
      allDates.push(new Date(task.dateDebut), new Date(task.dateFin));
      if (task.sousTaches) {
        task.sousTaches.forEach(subTask => {
          allDates.push(new Date(subTask.dateDebut), new Date(subTask.dateFin));
        });
      }
    });

    this.projectStartDate = new Date(Math.min(...allDates.map(d => d.getTime())));
    this.projectEndDate = new Date(Math.max(...allDates.map(d => d.getTime())));
  }

  // Méthodes utilitaires pour les sous-tâches
  getSubTaskPriority(type: TypeSousTache): string {
    switch (type) {
      case TypeSousTache.TECHNIQUE: return 'Élevée';
      case TypeSousTache.TEST: return 'Élevée';
      case TypeSousTache.DOCUMENTATION: return 'Moyenne';
      case TypeSousTache.REVUE: return 'Moyenne';
      default: return 'Faible';
    }
  }

  getSubTaskStatus(progress: number): string {
    if (progress === 100) return 'Terminée';
    if (progress > 0) return 'En cours';
    return 'À faire';
  }

  // Gestionnaires d'événements Syncfusion
  onTaskbarClick(args: any): void {
    this.selectedTaskId = args.data.TaskID;
    this.selectedTask = this.taches.find(t => t.id === args.data.TaskID) || null;
  }

  onActionBegin(args: any): void {
    // Gestion des actions avant leur exécution
    console.log('Action begin:', args.requestType);
  }

  onActionComplete(args: any): void {
    // Gestion des actions après leur exécution
    console.log('Action complete:', args.requestType);
  }






  selectTask(taskId: number) {
    if (this.selectedTaskId === taskId) {
      this.selectedTaskId = null;
      this.selectedTask = null;
    } else {
      this.selectedTaskId = taskId;
      this.selectedTask = this.taches.find(t => t.id === taskId) || null;
    }
  }

  // exportToPDF() {
  //   if (this.ganttChart) {
  //     this.ganttChart.pdfExport();
  //   }
  // }

  // exportToExcel() {
  //   if (this.ganttChart) {
  //     this.ganttChart.excelExport();
  //   }
  // }

  // Méthode pour filtrer par statut
  // filterByStatus(status: StatutTache) {
  //   if (this.ganttChart) {
  //     this.ganttChart.filterSettings = {
  //       columns: [{ field: 'Status', operator: 'equal', value: status }]
  //     };
  //   }
  // }

  // Méthode pour filtrer par priorité
  // filterByPriority(priority: PrioriteDeTache) {
  //   if (this.ganttChart) {
  //     this.ganttChart.filterSettings = {
  //       columns: [{ field: 'Priority', operator: 'equal', value: priority }]
  //     };
  //   }
  // }

  // Réinitialiser les filtres
  // clearFilters() {
  //   if (this.ganttChart) {
  //     this.ganttChart.clearFiltering();
  //   }
  // }

}
