import {Component, OnInit} from '@angular/core';
import {DepartementService} from "../../core/service/departement/departement.service";
import {NgForOf, NgIf} from "@angular/common";
import {Router, RouterLink} from "@angular/router";
import {DepartementDTO} from "../../Dto/departement.dto";
import {MatTooltip} from "@angular/material/tooltip";
import {ResponseDto} from "../../model/reponseDto.model";
import {PermissionDto} from "../../model/permissionDto.model";
import {PermissionService} from "../../core/service/permission/permission.service";
import {UserService} from "../../auth/service/user/user.service";

@Component({
  selector: 'app-departement',
  standalone: true,
  imports: [
    NgForOf,
    RouterLink,
    MatTooltip,
    NgIf
  ],
  templateUrl: './departement.component.html',
  styleUrl: './departement.component.scss'
})
export class DepartementComponent implements OnInit{

  departementList : DepartementDTO[] = []
  permissionDepartement !: PermissionDto;


  constructor(private departementService : DepartementService,private router : Router,private permissionService : PermissionService,private userService : UserService) {
  }
  ngOnInit() {
    this.departementService.getAllDepartements().subscribe(data => {
      this.departementList = data ;
      console.log("dapartment list",data);
    })
    this.getUserAuthData();
    this.getUserId() ;
  }






  deleteDepartement(id : number | undefined) {
    if (confirm("Voulez-vous vraiment supprimer cette departement ?") && id) {
      this.departementService.deleteDepartement(id).subscribe({
        next: () => {
          alert("departement supprimée avec succès !");
        },
        error: () => {
          alert("Erreur : impossible de supprimer departement.");
        }
      });
    }  }

  addDepartement() {
    this.router.navigate(['/departement/addDepartement']);
  }

  viewDepartement(id : number| undefined) {
    this.router.navigate(['/departement/detaisDepartement',id])

  }





  idUser !: number ;
  getUserId() {
    const item = localStorage.getItem('myLSkey');
    let currentUser: ResponseDto | null = null;

    if (item) {
      currentUser = JSON.parse(atob(item));
      if (currentUser?.userId){
        this.idUser = currentUser?.userId ;
        console.log("userId" , this.idUser);

      }

    }
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
        if (per.moduleTitle.includes("gestion departement")){
          this.permissionDepartement = per ;
          console.log("permission Departement" ,this.permissionDepartement);
        }
      })
      console.log('Departement:', currentUser?.entreprise.nom);
    }
  }

}
