import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule} from "@angular/forms";
import {Validators} from "ngx-editor";
import {NgForOf, NgIf} from "@angular/common";
import {UserService} from "../service/user/user.service";
import {EntrepriseService} from "../../core/service/entreprise/entreprise.service";
import {MatStep, MatStepLabel, MatStepper} from "@angular/material/stepper";
import {MatFormField} from "@angular/material/form-field";
import {MatInput} from "@angular/material/input";
import {MatButton} from "@angular/material/button";
import {Router, RouterLink} from "@angular/router";
import { routes } from 'src/app/core/helpers/routes';
import {MatIcon} from "@angular/material/icon";






@Component({
  selector: 'app-register-entreprise-setpper',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    NgIf,
    NgForOf,
    MatStepper,
    MatStep,
    MatFormField,
    MatInput,
    MatButton,
    MatStepLabel,
    RouterLink,
    MatIcon
  ],
  templateUrl: './register-entreprise-setpper.component.html',
  styleUrl: './register-entreprise-setpper.component.scss'
})
export class RegisterEntrepriseSetpperComponent implements OnInit {
  public routes = routes;

  userFormGroup!: FormGroup;
  entrepriseFormGroup!: FormGroup;
  userEntrepriseId!: number ;
  currentStep = 1;
  idEntreprise! : number  ;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private entrepriseService: EntrepriseService,
    private router : Router
  ) {
  }

  ngOnInit(): void {
    this.userFormGroup = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required()],
      email: ['', [Validators.required]],
    });

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
    localStorage.removeItem('tempUserData');
    localStorage.removeItem('tempEntrepirseData');
  }

  async createEntreprise(): Promise<number> {
    return new Promise((resolve) => {
      if (this.entrepriseFormGroup.valid) {
        this.entrepriseService.ajouterEntreprise(this.entrepriseFormGroup.value).subscribe({
          next: (data) => {
            if (data?.id!=null){
              this.idEntreprise = data.id;
            }
            console.log("Entreprise créée avec ID:", this.idEntreprise);
            resolve(this.idEntreprise);
          }
        });
      }
    });
  }
  async createUser() {
    try {
      await this.createEntreprise();

      if (this.userFormGroup.valid) {
        this.userService.registerUser(this.userFormGroup.value,this.idEntreprise,0).subscribe({
          next: () => {
            this.userEntrepriseId = this.idEntreprise;
            console.log("Id entreprise:", this.userEntrepriseId);
            this.currentStep = 2;
            this.router.navigate(['/signin'])

          },
        });
      }
    } catch (error) {
      console.error("Erreur lors de la création:", error);
    }
  }




  goToStep(stepNumber: number): void {
    if (stepNumber === 1) {
      const userData = this.userFormGroup.value;

      localStorage.setItem('tempUserData', JSON.stringify(userData));
    } else if (stepNumber === 2) {
      const entrepriseData = this.entrepriseFormGroup.value;

      localStorage.setItem('tempEntrepirseData', JSON.stringify(entrepriseData));
    }
    this.currentStep = stepNumber;

  }

  // assigne(){
  //   this.entrepriseService.assignerUserToEntreprise(this.userFormGroup.value.username, this.entrepriseFormGroup.value.nom)
  //     .subscribe({
  //       next: (res) => alert('Succès : ' + res),
  //       error: (err) => alert('Erreur : ' + err.error)
  //     });
  // }


  // assignerUserToEntreprise() {
  //
  //    this.createUser();
  //    this.createEntreprise();
  //   //this.assigne();
  //
  //
  // }
}
