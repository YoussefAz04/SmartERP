import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule} from "@angular/forms";
import {Validators} from "ngx-editor";
import {ClientService} from "../../../core/service/client/client.service";
import {routes} from "../../../core/helpers/routes";
import {RouterLink} from "@angular/router";

@Component({
  selector: 'app-add-categorie-client',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink
  ],
  templateUrl: './add-categorie-client.component.html',
  styleUrl: './add-categorie-client.component.scss'
})
export class AddCategorieClientComponent implements OnInit{
  categorieClientForm!: FormGroup ;

  constructor(private clientService : ClientService ,private  fb: FormBuilder ) {

  }

  ngOnInit(): void {
    this.categorieClientForm = this.fb.group({
      nom: ['', Validators.required],
      description: ['', Validators.required],
    });
  }

  createCategorieClient(){
    if (this.categorieClientForm.valid){
      this.clientService.addCategorieClient(this.categorieClientForm.value).subscribe(data =>{
        console.log('ajoute avec succes', data);
      })
    }
  }

  protected readonly routes = routes;
}
