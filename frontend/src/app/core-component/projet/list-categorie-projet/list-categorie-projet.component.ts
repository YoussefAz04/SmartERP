import {Component, OnInit} from '@angular/core';

import {Router} from "@angular/router";
import {CategorieProjet} from "../../../model/CategorieProjet.model";
import {CategorieProjetService} from "../../../core/service/categorieProjet/categorie-projet.service";
import {MatTooltip} from "@angular/material/tooltip";
import {NgForOf} from "@angular/common";

@Component({
  selector: 'app-list-categorie-projet',
  standalone: true,
  imports: [
    MatTooltip,
    NgForOf
  ],
  templateUrl: './list-categorie-projet.component.html',
  styleUrl: './list-categorie-projet.component.scss'
})
export class ListCategorieProjetComponent implements OnInit{

  categorieProjetList : CategorieProjet[] = [] ;
  constructor(private categorieProjetService: CategorieProjetService ,private router : Router) {
  }
  ngOnInit() {
    this.categorieProjetService.getAll().subscribe(data => {
      this.categorieProjetList = data ;
      console.log(this.categorieProjetList);
    })
  }


  addCategorieProjet() {
    this.router.navigate(['/projet/addCategorieProjet'])
  }

  viewCategorieProjet(id: number | undefined) {
    return id ;
  }

  deleteCategorie(id: number | undefined) {
    if (id)
       this.categorieProjetService.delete(id).subscribe(()=>{
         console.log("delete avec succes");
       })
  }
}
