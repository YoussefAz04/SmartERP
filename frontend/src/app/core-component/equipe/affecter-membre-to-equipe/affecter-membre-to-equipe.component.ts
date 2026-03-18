import { Component, OnInit } from '@angular/core';
import { Membre } from "../../../model/membre.model";
import { MembreService } from "../../../core/service/membre/membre.service";
import { CdkDrag, CdkDragDrop, CdkDropList, CdkDropListGroup, moveItemInArray, transferArrayItem } from "@angular/cdk/drag-drop";
import { NgForOf } from "@angular/common";
import {ActivatedRoute, Router} from "@angular/router";

@Component({
  selector: 'app-affecter-membre-to-equipe',
  standalone: true,
  imports: [
    CdkDropListGroup,
    CdkDropList,
    NgForOf,
    CdkDrag
  ],
  templateUrl: './affecter-membre-to-equipe.component.html',
  styleUrl: './affecter-membre-to-equipe.component.scss'
})
export class AffecterMembreToEquipeComponent implements OnInit {
  listMembreNotAffected: Membre[] = [];
  listMembreToAffecter: Membre[] = [];
  idEquipe !: number ;

  constructor(private membreService: MembreService,private router :ActivatedRoute) {}

  ngOnInit(): void {
    this.idEquipe = this.router.snapshot.params['id'];
    this.loadMembresNonAffectes();
  }

  private loadMembresNonAffectes(): void {
    this.membreService.getAllMembresNonAffectes().subscribe({
      next: (data) => {
        this.listMembreNotAffected = data;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des membres:', error);
      }
    });
  }

  drop(event: CdkDragDrop<Membre[]>): void {
    if (event.previousContainer === event.container) {
      moveItemInArray(
        event.container.data,
        event.previousIndex,
        event.currentIndex
      );
    } else {
      transferArrayItem(
        event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex
      );
    }
  }

  trackByMembre(index: number, membre: Membre): any {
    return membre.id || index;
  }

  affecterMembreToEquipe(){
    this.membreService.affecterMembresAEquipe(this.listMembreToAffecter,this.idEquipe).subscribe(()=>{
      console.log("affected") ;
    });
  }
}
