import {Component, OnInit} from '@angular/core';
import {EntrepriseService} from "../../../core/service/entreprise/entreprise.service";
import {ActivatedRoute, Router, RouterLink} from "@angular/router";
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
    NgIf,
    RouterLink
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
  isAdmin: boolean = false;
  isSuperAdmin: boolean = false;

  ngOnInit() {
     this.idEntreprise = this.routerLink.snapshot.params['id'];
     this.getUserAuthData();
     this.entrepriseService.getEntrepriseById(this.idEntreprise).subscribe(data =>{
       this.entreprise = data ;
       console.log(data);
     });
   }

  getUserAuthData(){
    const item = localStorage.getItem('myLSkey');
    let currentUser: ResponseDto | null = null;

    if (item) {
      currentUser = JSON.parse(decodeURIComponent(escape(atob(item))));
      console.log('User connecté:', currentUser?.name);

      console.log('Token:', currentUser?.token);
      console.log('Permissions:', currentUser?.permission);
      currentUser?.permission?.forEach(per => {
        if (per.moduleTitle.includes("Entreprises") || per.moduleTitle.includes("gestion entreprise")){
          this.permissionEntreprise = per ;
          console.log("permission Entreprise" ,this.permissionEntreprise);
        }
      });

      const roleId: number = currentUser?.roleId ?? 0;
      this.isSuperAdmin = roleId === 0;
      this.isAdmin = roleId === 1;

      // ADMIN can only edit their own enterprise — redirect if URL id doesn't match
      if (this.isAdmin) {
        const userEntrepriseId = currentUser?.entreprise?.id ?? currentUser?.entrepriseId ?? null;
        if (userEntrepriseId !== null && Number(userEntrepriseId) !== Number(this.idEntreprise)) {
          console.warn('⚠️ ADMIN attempted to access another enterprise. Redirecting.');
          this.router.navigate(['/entreprise/listEntreprise']);
          return;
        }
      }

      console.log('Entreprise:', currentUser?.entreprise?.nom);
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
