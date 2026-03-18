import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule} from "@angular/forms";
import {ClientService} from "../../../core/service/client/client.service";
import {Validators} from "ngx-editor";
import {CategorieClient} from "../../../model/categorieClient.model";
import {NgForOf} from "@angular/common";
import {PaginatorModule} from "primeng/paginator";
import {routes} from "../../../core/helpers/routes";
import {RouterLink} from "@angular/router";

@Component({
  selector: 'app-add-client',
  standalone: true,
  imports: [
    NgForOf,
    PaginatorModule,
    ReactiveFormsModule,
    RouterLink
  ],
  templateUrl: './add-client.component.html',
  styleUrl: './add-client.component.scss'
})
export class AddClientComponent implements OnInit{
  clientForm!: FormGroup ;
  categories : CategorieClient[] = [];

  constructor(private clientService : ClientService ,private  fb: FormBuilder ) {

  }

  ngOnInit(): void {

    this.loadCategorie() ;
    this.clientForm = this.fb.group({
      nom: ['', Validators.required],
      prenom: ['', Validators.required],
      email: ['', Validators.required],
      telephone: ['', Validators.required],
      adresse: ['', Validators.required],
      secteurActivite: ['', Validators.required],
      categorieId: [null, Validators.required],
    });
  }

  onSubmit(){
    if (this.clientForm.valid){
      const formValue = this.clientForm.value;
      const idCategorie = formValue.categorieId ;
      this.clientService.addClient(this.clientForm.value,idCategorie).subscribe(data =>{
        console.log('ajoute avec succes', data);
      })
    }
  }

  loadCategorie(){
    this.clientService.getAllCategorieClients().subscribe(data =>{
      this.categories = data ;
    })
  }

  protected readonly routes = routes;
}
