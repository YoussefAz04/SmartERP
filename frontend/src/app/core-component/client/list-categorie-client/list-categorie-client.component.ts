import {Component, OnInit} from '@angular/core';
import {ClientService} from "../../../core/service/client/client.service";
import {Client} from "../../../model/client.model";
import {CategorieClient} from "../../../model/categorieClient.model";
import {MatTooltip} from "@angular/material/tooltip";
import {NgForOf, NgIf} from "@angular/common";
import {Router} from "@angular/router";

@Component({
  selector: 'app-list-categorie-client',
  standalone: true,
  imports: [
    MatTooltip,
    NgForOf,
    NgIf
  ],
  templateUrl: './list-categorie-client.component.html',
  styleUrl: './list-categorie-client.component.scss'
})
export class ListCategorieClientComponent implements OnInit{

  categorieClientList : CategorieClient[] = [] ;
  constructor(private Clientservise: ClientService,private router : Router) {
  }
  ngOnInit() {
    this.Clientservise.getAllCategorieClients().subscribe(data => {
      this.categorieClientList = data ;
      console.log(this.categorieClientList);
    })
  }


  addCategorieClient() {
    this.router.navigate(['/client/addCategorie'])

  }

  viewCategorieClient(id: number | undefined) {
    return id ;
  }

  deleteCategorie(id: number | undefined) {
    return id ;
  }
}
