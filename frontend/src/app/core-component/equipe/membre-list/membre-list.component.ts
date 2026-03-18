import {Component, OnInit} from '@angular/core';
import {Equipe} from "../../../model/equipe.model";
import {EquipeService} from "../../../core/service/equipe/equipe.service";
import {Membre} from "../../../model/membre.model";
import {MembreService} from "../../../core/service/membre/membre.service";
import {MatTooltip} from "@angular/material/tooltip";
import {NgForOf, NgIf} from "@angular/common";
import {routes} from "../../../core/helpers/routes";
import {Router, RouterLink} from "@angular/router";
import {ResponseDto} from "../../../model/reponseDto.model";
import {PermissionDto} from "../../../model/permissionDto.model";

@Component({
  selector: 'app-membre-list',
  standalone: true,
  imports: [
    MatTooltip,
    NgForOf,
    RouterLink,
    NgIf
  ],
  templateUrl: './membre-list.component.html',
  styleUrl: './membre-list.component.scss'
})
export class MembreListComponent implements OnInit{
  membres: Membre[] = [];
  permissionMembre !: PermissionDto;

  constructor(private membreService: MembreService,
              private router : Router) {}

  ngOnInit(): void {
    this.membreService.getAllMembre().subscribe(data => {
      this.membres = data;
    });
    this.getUserAuthData() ;
  }

  addMembre() {
    this.router.navigate(['/equipe/addMembre'])
  }

  viewMembre(id: any) {
    this.router.navigate(['/equipe/detailsMembre',id])

  }

  deleteMembre(id: any) {
    return id ;
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
        if (per.moduleTitle.includes("Membre")){
          this.permissionMembre = per ;
          console.log("permission Membre" ,this.permissionMembre);
        }
      })
      console.log('Membre:', currentUser?.entreprise.nom);
    }
  }


  protected readonly routes = routes;
}
