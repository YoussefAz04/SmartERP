import {Component, OnInit} from '@angular/core';
import {EquipeDto} from "../../../model/equipeDto.model";
import {EquipeService} from "../../../core/service/equipe/equipe.service";
import {ActivatedRoute, Router} from "@angular/router";
import {MembreDto} from "../../../model/membreDto.model";
import {MembreService} from "../../../core/service/membre/membre.service";
import {NgForOf, NgIf} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {SharedModule} from "../../../shared/shared.module";
import {ResponseDto} from "../../../model/reponseDto.model";
import {PermissionDto} from "../../../model/permissionDto.model";

@Component({
  selector: 'app-details-membre',
  standalone: true,
  imports: [
    NgIf,
    NgForOf,
    FormsModule,
    SharedModule
  ],
  templateUrl: './details-membre.component.html',
  styleUrl: './details-membre.component.scss'
})
export class DetailsMembreComponent implements OnInit{
  membredetails !: MembreDto ;
  idMembre !:  number ;
  equipeMembre !: EquipeDto ;
  permissionMembre!: PermissionDto;
  constructor(private equipeService : EquipeService,
              private routerAvtivated : ActivatedRoute,
              private router : Router,
              private membreService : MembreService) {
  }
  ngOnInit() {
    this.idMembre = this.routerAvtivated.snapshot.params['id'];
    this.membreService.getMembreById(this.idMembre).subscribe(data =>{
      this.membredetails = data ;
      this.getEquipeMembre(data.equipeId);
    })

    this.getUserAuthData() ;
  }


  getEquipeMembre(id : number){
    this.equipeService.getEquipeById(id).subscribe(data =>{
      this.equipeMembre =data ;
    })
  }

  onSubmit() {
    this.membreService.updateMembre(this.membredetails).subscribe(()=>{
      console.log("le update est succes");
      this.router.navigate(['/equipe/listMembre']);
    })
    this.router.navigate(['/equipe/listMembre']);

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

}
