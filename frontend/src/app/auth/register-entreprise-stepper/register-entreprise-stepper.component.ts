import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
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
  selector: 'app-register-entreprise-stepper',
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
  templateUrl: './register-entreprise-stepper.component.html',
  styleUrl: './register-entreprise-stepper.component.scss'
})
export class RegisterEntrepriseStepperComponent implements OnInit {
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
      password: ['', Validators.required],
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
    localStorage.removeItem('tempEntrepriseData');
  }

  async createEntreprise(): Promise<number> {
    return new Promise((resolve, reject) => {
      if (this.entrepriseFormGroup.valid) {
        this.entrepriseService.ajouterEntreprise(this.entrepriseFormGroup.value).subscribe({
          next: (data) => {
            if (data?.id != null){
              this.idEntreprise = data.id;
              resolve(this.idEntreprise);
            } else {
              reject(new Error('Entreprise creation returned no ID'));
            }
          },
          error: (err) => {
            reject(err);
          }
        });
      } else {
        reject(new Error('Entreprise form is invalid'));
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

      localStorage.setItem('tempEntrepriseData', JSON.stringify(entrepriseData));
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
