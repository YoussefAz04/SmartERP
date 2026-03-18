import {Component, OnInit} from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import {Budget} from "../../../model/budget.model";
import {BudgetService} from "../../../core/service/Budget/budget.service";
import {RouterLink} from "@angular/router";
import {routes} from "../../../core/helpers/routes";
import {Projet} from "../../../model/projet.model";
import {ProjetService} from "../../../core/service/projet/projet.service";


@Component({
  selector: 'app-add-budget',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, RouterLink],
  templateUrl: './add-budget.component.html',
  styleUrls: ['./add-budget.component.scss']
})
export class AddBudgetComponent implements OnInit{
  budgetForm: FormGroup;

  projets : Projet[] = [] ;


  ngOnInit() {
   this.getAllProjet() ;
  }
  getAllProjet(){
    this.projetService.getAllProjetNotBudget().subscribe(data =>{
      this.projets = data ;
    })
  }



  constructor(private fb: FormBuilder, private budgetService: BudgetService,private projetService : ProjetService) {
    this.budgetForm = this.fb.group({
      nom: ['', Validators.required],
      montant: [0, [Validators.required, Validators.min(0)]],
      dateDebut: ['', Validators.required],
      dateFin: ['', Validators.required],
      idProjet: [null, Validators.required]
    });
  }

  onSubmit() {
    if (this.budgetForm.valid) {
      const newBudget: Budget = this.budgetForm.value;
      this.budgetService.addBudget(newBudget).subscribe({
        next: (res) => {
          console.log('✅ Budget ajouté avec succès', res);
          this.budgetForm.reset();

        },
        error: (err) => {
          console.error('❌ Erreur lors de l’ajout du budget', err);
        }
      });
    }
  }

  protected readonly routes = routes;
}
