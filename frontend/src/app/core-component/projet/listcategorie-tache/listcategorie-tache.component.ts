import { Component } from '@angular/core';
import {MatTooltip} from "@angular/material/tooltip";
import {NgForOf} from "@angular/common";
import {CategorieProjet} from "../../../model/CategorieProjet.model";
import {CategorieProjetService} from "../../../core/service/categorieProjet/categorie-projet.service";
import {Router} from "@angular/router";
import {CategorieTacheService} from "../../../core/service/categorieTache/categorie-tache.service";

@Component({
  selector: 'app-listcategorie-tache',
  standalone: true,
    imports: [
        MatTooltip,
        NgForOf
    ],
  templateUrl: './listcategorie-tache.component.html',
  styleUrl: './listcategorie-tache.component.scss'
})
export class ListcategorieTacheComponent {


  categorieTacheList : CategorieProjet[] = [] ;
  constructor(private categorieTacheService: CategorieTacheService ,private router : Router) {
  }
  ngOnInit() {
    this.categorieTacheService.getAllCategories().subscribe(data => {
      this.categorieTacheList = data ;
      console.log(this.categorieTacheList);
    })
  }


  addCategorieTahce() {
     this.router.navigate(['/projet/addCategorieTache'])
  }

  viewCategorieTache(id: number | undefined) {
    return id ;
  }

  deleteCategorie(id: number | undefined) {
    return id ;
  }
}
