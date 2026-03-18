import {Component, OnInit} from '@angular/core';
import {FormsModule} from "@angular/forms";
import {Projet, StatutProjet} from "../../model/projet.model";
import {ProjetService} from "../../core/service/projet/projet.service";
import {CdkDrag, CdkDragDrop, CdkDropList, moveItemInArray, transferArrayItem} from "@angular/cdk/drag-drop";
import {DatePipe, NgClass, NgForOf, NgIf} from "@angular/common";
import {MatTooltip} from "@angular/material/tooltip";
import {Router, RouterLink} from "@angular/router";
import {CategorieProjetService} from "../../core/service/categorieProjet/categorie-projet.service";
import {CategorieProjet} from "../../model/CategorieProjet.model";
import {ResponseDto} from "../../model/reponseDto.model";
import {PermissionDto} from "../../model/permissionDto.model";

@Component({
  selector: 'app-project-board',
  templateUrl: './projet.component.html',
  standalone: true,
  imports: [
    FormsModule,
    CdkDropList,
    CdkDrag,
    NgForOf,
    MatTooltip,
    DatePipe,
    NgIf,
    NgClass,
    RouterLink
  ],
  styleUrls: ['./projet.component.scss']
})
export class ProjetComponent implements OnInit {

  projetListEN_ATTENTE: Projet[] = [];
  projetListEN_COURS: Projet[] = [];
  projetListTERMINE: Projet[] = [];
  projetListANNULE: Projet[] = [];
  permissionProjet !: PermissionDto;

  constructor(private projetService: ProjetService,private router : Router,private categorieService : CategorieProjetService) {}

  ngOnInit(): void {
    this.getProjetParStatut();
    this.getUserAuthData();
  }

  getProjetParStatut(): void {
    this.projetService.getAllProjetsByStatus(StatutProjet.EN_ATTENTE).subscribe(data => this.projetListEN_ATTENTE = data);
    this.projetService.getAllProjetsByStatus(StatutProjet.EN_COURS).subscribe(data => this.projetListEN_COURS = data);
    this.projetService.getAllProjetsByStatus(StatutProjet.TERMINE).subscribe(data => this.projetListTERMINE = data);
    this.projetService.getAllProjetsByStatus(StatutProjet.ANNULE).subscribe(data => this.projetListANNULE = data);
  }

  drop(event: CdkDragDrop<Projet[]>) {
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    } else {
      const projet = event.previousContainer.data[event.previousIndex];
      console.log("contraint id ",event.container.id);
      const nouveauStatut = this.getStatutFromContainerId(event.container.id);

      projet.statut = nouveauStatut;

      transferArrayItem(
        event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex
      );

      this.updateProjetStatut(projet, nouveauStatut);
    }
  }

  private getStatutFromContainerId(containerId: string): StatutProjet {
    switch (containerId) {
      case 'cdk-drop-list-0':
        return StatutProjet.EN_ATTENTE;
      case 'cdk-drop-list-1':
        return StatutProjet.EN_COURS;
      case 'cdk-drop-list-2':
        return StatutProjet.TERMINE;
      case 'cdk-drop-list-3':
        return StatutProjet.ANNULE;
      default:
        throw new Error(`Statut inconnu pour le container: ${containerId}`);
    }
  }

  private updateProjetStatut(projet: Projet, nouveauStatut: StatutProjet): void {
    this.projetService.updateStatutProjet(projet.id!, nouveauStatut).subscribe({

    });
  }



  trackProjet(index: number, projet: Projet): number {
    return projet.id!;
  }

  addProjet() {
      this.router.navigate(['/projet/addProjet']);
  }

  detaisProjet(id: number | undefined) {
    this.router.navigate(['/projet/detaisProjet',id]);

  }



  getUserAuthData(){
    const item = localStorage.getItem('myLSkey');
    let currentUser: ResponseDto | null = null;

    if (item) {
      currentUser = JSON.parse(atob(item));
      console.log('User connecté:', currentUser?.name);

      console.log('Token:', currentUser?.token);
      console.log('Permissions:', currentUser?.permission);
      currentUser?.permission.map(per =>{
        if (per.moduleTitle == "Projet"){
          this.permissionProjet = per ;
          console.log("permission Projet" ,this.permissionProjet);
        }
      })
      console.log('Projet:', currentUser?.entreprise.nom);
    }
  }
}
