import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatOption} from "@angular/material/autocomplete";
import {MatSelect} from "@angular/material/select";
import {Router, RouterLink} from "@angular/router";
import {routes} from "../../../core/helpers/routes";
import {EntrepriseService} from "../../../core/service/entreprise/entreprise.service";
import {Validators} from "ngx-editor";

@Component({
  selector: 'app-add-entreprise',
  standalone: true,
  imports: [
    FormsModule,
    MatOption,
    MatSelect,
    RouterLink,
    ReactiveFormsModule
  ],
  templateUrl: './add-entreprise.component.html',
  styleUrl: './add-entreprise.component.scss'
})
export class AddEntrepriseComponent implements OnInit{

  protected readonly routes = routes;

  entrepriseFormGroup!: FormGroup;

  constructor(private entrepriseService: EntrepriseService,
              private router : Router,
              private fb: FormBuilder,
  ) {

  }



  ngOnInit(): void {
    this.entrepriseFormGroup = this.fb.group({
      nom: ['', Validators.required],
      raisonSociale: ['', Validators.required],
      description: [''],
      secteurActivite: [''],
      statutJuridique: [''],
      numeroIdentificationFiscale: [''],
      registreCommerce: [''],
      siteWeb: [''],
      dateCreation: ['', Validators.required],
      dateDerniereModification: ['']
    });
  }

  createEntreprise (){
    if (this.entrepriseFormGroup.valid) {
      this.entrepriseService.ajouterEntreprise(this.entrepriseFormGroup.value).subscribe({
        next: (data) => {
          console.log("Entreprise créée avec ID:", data);
        }
      });
    }
  }

}
