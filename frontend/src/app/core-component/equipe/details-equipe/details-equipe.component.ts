import {Component, OnInit} from '@angular/core';
import {EquipeService} from "../../../core/service/equipe/equipe.service";
import {ActivatedRoute, Router} from "@angular/router";
import {EquipeDto} from "../../../model/equipeDto.model";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgForOf, NgIf} from "@angular/common";
import {ResponseDto} from "../../../model/reponseDto.model";
import {PermissionDto} from "../../../model/permissionDto.model";

@Component({
  selector: 'app-details-equipe',
  standalone: true,
  imports: [
    FormsModule,
    NgIf,
    ReactiveFormsModule,
    NgForOf
  ],
  templateUrl: './details-equipe.component.html',
  styleUrl: './details-equipe.component.scss'
})
export class DetailsEquipeComponent implements OnInit{
  equipedetails !: EquipeDto ;
  idEquipe !:  number ;
  permissionEquipe!: PermissionDto;
constructor(private equipeService : EquipeService,private routerAvtivated : ActivatedRoute,private router : Router) {
}
ngOnInit() {
  this.idEquipe = this.routerAvtivated.snapshot.params['id'];
  this.equipeService.getEquipeById(this.idEquipe).subscribe(data =>{
    this.equipedetails = data ;
    this.getUserAuthData();
  })
}


  onSubmit() {
  this.equipeService.updateEquipe(this.idEquipe,this.equipedetails).subscribe(()=>{
    console.log("le update est succes");
    this.router.navigate(['/equipe/listEquipe']);
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
        if (per.moduleTitle.includes("Equipe")){
          this.permissionEquipe = per ;
          console.log("permission Equipe" ,this.permissionEquipe);
        }
      })
      console.log('Equipe:', currentUser?.entreprise.nom);
    }
  }

}
