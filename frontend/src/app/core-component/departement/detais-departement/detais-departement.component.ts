import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {DepartementService} from "../../../core/service/departement/departement.service";
import {NgForOf, NgIf} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {ResponseDto} from "../../../model/reponseDto.model";
import {PermissionDto} from "../../../model/permissionDto.model";

@Component({
  selector: 'app-detais-departement',
  standalone: true,
  imports: [
    NgForOf,
    FormsModule,
    NgIf
  ],
  templateUrl: './detais-departement.component.html',
  styleUrl: './detais-departement.component.scss'
})
export class DetaisDepartementComponent implements OnInit{
  departementId! : number ;
  departement !: any ;
  permissionEntreprise!: PermissionDto;
  constructor(private departementService : DepartementService,private routerLink : ActivatedRoute,private router : Router) {
  }

  ngOnInit() {
    this.departementId = this.routerLink.snapshot.params['id'];
    this.departementService.getDepartementDToById(this.departementId).subscribe(data =>{
      console.log(data);
      this.departement = data ;
    })
    this.getUserAuthData();
  }

  onSubmit() {
    this.departementService.updateDepartement(this.departementId,this.departement).subscribe(data =>{
      console.log("dapartement modifier" , data);
      this.router.navigate(['/departement/listDepartement'])
    })
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
        if (per.moduleTitle.includes("Entreprises")){
          this.permissionEntreprise = per ;
          console.log("permission Entreprise" ,this.permissionEntreprise);
        }
      })
      console.log('Entreprise:', currentUser?.entreprise.nom);
    }
  }

}
