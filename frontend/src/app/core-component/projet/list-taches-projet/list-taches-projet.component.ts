import {Component, OnInit} from '@angular/core';
import {TacheService} from "../../../core/service/taches/tache.service";
import {ProjetService} from "../../../core/service/projet/projet.service";
import {Projet} from "../../../model/projet.model";
import {Tache} from "../../../model/tache.model";
import {DatePipe, NgClass, NgForOf, NgIf} from "@angular/common";
import {MatTooltip} from "@angular/material/tooltip";
import {Router} from "@angular/router";
import {ReactiveFormsModule} from "@angular/forms";
import {SharedModule} from "../../../shared/shared.module";
import {ResponseDto} from "../../../model/reponseDto.model";
import { PermissionDto } from 'src/app/model/permissionDto.model';

@Component({
  selector: 'app-list-taches-projet',
  standalone: true,
  imports: [
    DatePipe,
    NgForOf,
    NgIf,
    NgClass,
    MatTooltip,
    ReactiveFormsModule,
    SharedModule
  ],
  templateUrl: './list-taches-projet.component.html',
  styleUrl: './list-taches-projet.component.scss'
})
export class ListTachesProjetComponent implements OnInit{
  projetList : Projet[] = [] ;
   permissionTaches!: PermissionDto;
  constructor(private tacheServce : TacheService,private projetService : ProjetService,
              private router : Router
  ) {
  }
  ngOnInit() {
    this.projetService.getAllProjets().subscribe(data=>{
      this.projetList = data ;
    })

    this.getUserAuthData() ;
  }


  addTacheProjet(id : number | undefined) {
     this.router.navigate(['projet/addTache',id])
  }

  detaisTache(id: number | undefined) {
    this.router.navigate(['projet/detaisTache',id])
  }


  getUserAuthData(){
    const item = localStorage.getItem('myLSkey');
    let currentUser: ResponseDto | null = null;

    if (item) {
      currentUser = JSON.parse(decodeURIComponent(escape(atob(item))));
      console.log('User connecté:', currentUser?.name);

      console.log('Token:', currentUser?.token);
      console.log('Permissions:', currentUser?.permission);
      currentUser?.permission.map(per =>{
        if (per.moduleTitle == "Taches"){
          this.permissionTaches = per ;
          console.log("permission Projet" ,this.permissionTaches);
        }
      })
      console.log('Projet:', currentUser?.entreprise.nom);
    }
  }

}
