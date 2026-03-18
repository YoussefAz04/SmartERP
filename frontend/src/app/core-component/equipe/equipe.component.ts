import {Component, OnInit} from '@angular/core';
import {Equipe} from "../../model/equipe.model";
import {EquipeService} from "../../core/service/equipe/equipe.service";
import {MatTooltip} from "@angular/material/tooltip";
import {DatePipe, NgForOf, NgIf} from "@angular/common";
import {Router} from "@angular/router";
import {ResponseDto} from "../../model/reponseDto.model";
import {PermissionDto} from "../../model/permissionDto.model";

@Component({
  selector: 'app-equipe',
  standalone: true,
  imports: [
    MatTooltip,
    DatePipe,
    NgForOf,
    NgIf
  ],
  templateUrl: './equipe.component.html',
  styleUrl: './equipe.component.scss'
})
export class EquipeComponent implements OnInit{
  equipes: Equipe[] = [];
  permissionEquipe !: PermissionDto;

  constructor(private equipeService: EquipeService,private  router :Router) {}

  ngOnInit(): void {
    this.equipeService.getAllEquipes().subscribe(data => {
      this.equipes = data;
    });
    this.getUserAuthData() ;
  }

  addEquipe() {
    this.router.navigate(['/equipe/addEquipe'])
  }

  viewEquipe(id : any) {
         this.router.navigate(['/equipe/detailsEquipe',id])
  }

  deleteEquipe(id: any) {
    return id ;
  }

  affecterMembre(id: number | undefined) {
    this.router.navigate(['/equipe/affectedMembreToEquipe',id])
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
        if (per.moduleTitle.includes("Equipe")){
          this.permissionEquipe = per ;
          console.log("permission Equipe" ,this.permissionEquipe);
        }
      })
      console.log('Equipe:', currentUser?.entreprise.nom);
    }
  }

}
