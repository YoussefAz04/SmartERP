import {Component, OnInit} from '@angular/core';
import {Router, RouterLink} from "@angular/router";
import { routes } from 'src/app/core/helpers/routes';
import {EntrepriseService} from "../../core/service/entreprise/entreprise.service";
import {Entreprise} from "../../model/entreprise.model";
import {DatePipe, NgForOf, NgIf} from "@angular/common";
import {MatTooltip} from "@angular/material/tooltip";
import {PermissionDto} from "../../model/permissionDto.model";
import {AuthResponse} from "../../auth/service/auth.service";
import {ResponseDto} from "../../model/reponseDto.model";


@Component({
  selector: 'app-entreprise',
  standalone: true,
  imports: [
    RouterLink,
    DatePipe,
    NgForOf,
    MatTooltip,
    NgIf
  ],
  templateUrl: './entreprise.component.html',
  styleUrl: './entreprise.component.scss'
})
export class EntrepriseComponent implements OnInit{







  permissionEntreprise !: PermissionDto ;


  public routes = routes;
  entreprises : Entreprise[] = []  ;
  constructor(private entrepriseService:EntrepriseService,private router : Router) {
  }



  ngOnInit(): void {
    this.entrepriseService.getAllEntreprises().subscribe({
      next: (data) => {
        this.entreprises = data;
        console.log("Entreprises chargées:", data);
      },
      error: (err) => {
        console.error("Erreur lors du chargement des entreprises:", err);
      }
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
        if (per.moduleTitle.includes("gestion entreprise")){
          this.permissionEntreprise = per ;
          console.log("permission Entreprise" ,this.permissionEntreprise);
        }
      })
      console.log('Entreprise:', currentUser?.entreprise.nom);
    }
  }


  editEntreprise(id : number | undefined) {
    this.router.navigate(['/entreprise/detaisEntreprise', id]);
  }

  deleteEntreprise(id: number | undefined): void {
    if (confirm("Voulez-vous vraiment supprimer cette entreprise ?") && id) {
      this.entrepriseService.deleteEntreprise(id).subscribe({
        next: () => {
          alert("Entreprise supprimée avec succès !");
        },
        error: () => {
          alert("Erreur : impossible de supprimer l'entreprise.");
        }
      });
    }
  }

  addEntreprise() {
    this.router.navigate(['/entreprise/addEntreprise'])
  }









}
