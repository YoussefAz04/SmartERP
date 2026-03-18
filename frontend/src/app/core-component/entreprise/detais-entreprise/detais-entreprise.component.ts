import {Component, OnInit} from '@angular/core';
import {EntrepriseService} from "../../../core/service/entreprise/entreprise.service";
import {ActivatedRoute, Router} from "@angular/router";
import {Entreprise} from "../../../model/entreprise.model";
import {DatePipe, NgIf} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {ResponseDto} from "../../../model/reponseDto.model";
import {PermissionDto} from "../../../model/permissionDto.model";

@Component({
  selector: 'app-detais-entreprise',
  standalone: true,
  imports: [
    DatePipe,
    FormsModule,
    NgIf
  ],
  templateUrl: './detais-entreprise.component.html',
  styleUrl: './detais-entreprise.component.scss'
})
export class DetaisEntrepriseComponent implements OnInit{


   constructor(private entrepriseService : EntrepriseService,private routerLink : ActivatedRoute,private router : Router) {
   }
   idEntreprise!: number ;
   entreprise!: Entreprise ;
  permissionEntreprise !: PermissionDto ;

  ngOnInit() {
     this.idEntreprise = this.routerLink.snapshot.params['id'];
     this.entrepriseService.getEntrepriseById(this.idEntreprise).subscribe(data =>{
       this.entreprise = data ;
       console.log(data);
     });
     this.getUserAuthData();
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



  onSubmit() {
    if (this.entreprise.id) {
      this.entrepriseService.updateEntreprise(this.entreprise.id, this.entreprise).subscribe({
        next: (updated) => {
          console.log("Entreprise mise à jour :", updated);
          this.router.navigate(['/entreprise/listEntreprise'])
        },
        error: (err) => {
          console.error("Erreur de mise à jour :", err);
        }
      });
    }
   }
}
