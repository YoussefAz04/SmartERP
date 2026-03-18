import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {Validators} from "ngx-editor";
import {CategorieProjetService} from "../../../core/service/categorieProjet/categorie-projet.service";
import { routes } from 'src/app/core/helpers/routes';
import {Router, RouterLink} from "@angular/router";

@Component({
  selector: 'app-add-categorie-projet',
  standalone: true,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    RouterLink
  ],
  templateUrl: './add-categorie-projet.component.html',
  styleUrl: './add-categorie-projet.component.scss'
})
export class AddCategorieProjetComponent implements OnInit{
  categorieProjetForm!: FormGroup ;

  constructor(private categorieProjetService : CategorieProjetService ,private  fb: FormBuilder ,private router : Router) {
  }

  ngOnInit(): void {
    this.categorieProjetForm = this.fb.group({
      nom: ['', Validators.required],
      description: ['', Validators.required],
    });
  }

  createCategorieProjet(){
    if (this.categorieProjetForm.valid){
      this.categorieProjetService.create(this.categorieProjetForm.value).subscribe(data =>{
        console.log('ajoute avec succes', data);
        this.router.navigate(['/projet/addCategorieProjet']);
      })
    }
  }


  protected readonly routes = routes;
}
