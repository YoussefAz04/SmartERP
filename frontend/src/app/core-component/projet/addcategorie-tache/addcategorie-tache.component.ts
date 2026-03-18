import { Component } from '@angular/core';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {Router, RouterLink} from "@angular/router";
import {Validators} from "ngx-editor";
import {CategorieTacheService} from "../../../core/service/categorieTache/categorie-tache.service";
import { routes } from 'src/app/core/helpers/routes';

@Component({
  selector: 'app-addcategorie-tache',
  standalone: true,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    RouterLink
  ],
  templateUrl: './addcategorie-tache.component.html',
  styleUrl: './addcategorie-tache.component.scss'
})
export class AddcategorieTacheComponent {
  categorieTacheForm!: FormGroup ;

  constructor(private categorietacheService : CategorieTacheService ,private  fb: FormBuilder ,private router : Router) {
  }

  ngOnInit(): void {
    this.categorieTacheForm = this.fb.group({
      nom: ['', Validators.required],
      description: ['', Validators.required],
    });
  }

  createCategorieTache(){
    if (this.categorieTacheForm.valid){
      this.categorietacheService.addCategorie(this.categorieTacheForm.value).subscribe(data =>{
        console.log('ajoute avec succes', data);
        this.router.navigate(['/projet/listCategorieTache']);
      })
    }
  }


  protected readonly routes = routes;
}
