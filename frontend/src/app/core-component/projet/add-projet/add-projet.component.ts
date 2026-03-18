import {Component, OnInit} from '@angular/core';
import {ProjetService} from "../../../core/service/projet/projet.service";
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {Router, RouterLink} from "@angular/router";
import {StatutProjet} from "../../../model/projet.model";
import {PaginatorModule} from "primeng/paginator";
import {routes} from "../../../core/helpers/routes";
import {NgForOf} from "@angular/common";
import {CategorieProjetService} from "../../../core/service/categorieProjet/categorie-projet.service";
import {CategorieProjet} from "../../../model/CategorieProjet.model";

@Component({
  selector: 'app-add-projet',
  standalone: true,
  imports: [
    PaginatorModule,
    ReactiveFormsModule,
    RouterLink,
    NgForOf
  ],
  templateUrl: './add-projet.component.html',
  styleUrl: './add-projet.component.scss'
})
export class AddProjetComponent  implements  OnInit{
  projetForm !: FormGroup ;
  categorieProjet !: CategorieProjet[];


  constructor(private projetService : ProjetService,private fb: FormBuilder,private router :Router,private categorieProjetService : CategorieProjetService) {
  }
  statuts = Object.values(StatutProjet);

  ngOnInit() {

    this.getAllCategorie();
    this.projetForm = this.fb.group({
      nom: ['', Validators.required],
      description: ['', Validators.required],
      dateDebut: ['', Validators.required],
      dateFin: ['', Validators.required],
      statut: ['', Validators.required],
      categorieId : ['',Validators.required]
    });
  }
  onSubmit() {
    if (this.projetForm.valid) {
      console.log("id de categorie Projet" , this.projetForm.value.categorieId);
      this.projetService.createProjetWithCategorie(this.projetForm.value, this.projetForm.value.categorieId).subscribe(() =>{

        this.router.navigate(['projet/listProjet']);
      });
    }
  }

  getAllCategorie(){
    this.categorieProjetService.getAll().subscribe(data =>{
      this.categorieProjet = data ;
    })
  }

  protected readonly routes = routes;
}
