import {Component, OnInit} from '@angular/core';
import {SousTache} from "../../../model/sousTache.model";
import {SousTacheService} from "../../../core/service/sousTache/sous-tache.service";
import {routes} from "../../../core/helpers/routes";
import {RouterLink} from "@angular/router";
import {FormBuilder, FormGroup, ReactiveFormsModule} from "@angular/forms";
import {Validators} from "ngx-editor";
import {NgForOf, NgIf} from "@angular/common";
import {CategorieTache} from "../../../model/categorieTache.model";
import {CategorieTacheService} from "../../../core/service/categorieTache/categorie-tache.service";

@Component({
  selector: 'app-add-sous-tache',
  standalone: true,
  imports: [
    RouterLink,
    ReactiveFormsModule,
    NgIf,
    NgForOf
  ],
  templateUrl: './add-sous-tache.component.html',
  styleUrl: './add-sous-tache.component.scss'
})
export class AddSousTacheComponent implements OnInit{
  sousTacheForm!: FormGroup;
  categorieSousTache : CategorieTache[] = [] ;

  constructor(private fb: FormBuilder, private sousTacheService: SousTacheService,
              private categorieSousTacheService : CategorieTacheService) {}

  ngOnInit(): void {
    this.categorieSousTacheService.getAllCategories().subscribe(data => {
      this.categorieSousTache = data ;
    })
    this.sousTacheForm = this.fb.group({
      titre: ['', Validators.required],
      description: [''],
      type: ['', Validators.required],
      dateDebut: [''],
      dateFin: [''],
      progres: [0, ],
      duree: [0, Validators.required],
      idCategorieTache : ['',Validators.required]
    });
  }

  onSubmit(): void {
    if (this.sousTacheForm.valid) {

      this.sousTacheService.create(this.sousTacheForm.value).subscribe(() => {
        console.log("add with secces");
      });
    }
  }

  protected readonly routes = routes;
}
