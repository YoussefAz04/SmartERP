import { Component, OnInit } from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import {Budget} from "../../../model/budget.model";
import {CategorieDepense} from "../../../model/depense.model";
import {DepenseService} from "../../../core/service/depense/depense.service";
import {BudgetService} from "../../../core/service/Budget/budget.service";
import {CategorieDepenseService} from "../../../core/service/categorieDepense/categorie-depense.service";
import {routes} from "../../../core/helpers/routes";
import {DecimalPipe, NgForOf, NgIf} from "@angular/common";

@Component({
  selector: 'app-add-depense',
  standalone: true,
  templateUrl: './add-depense.component.html',
  imports: [
    RouterLink,
    ReactiveFormsModule,
    DecimalPipe,
    NgForOf,
    NgIf
  ],
  styleUrls: ['./add-depense.component.scss']
})
export class AddDepenseComponent implements OnInit {

  depenseForm: FormGroup;
  budgets: Budget[] = [];
  categories: CategorieDepense[] = [];
  loading = false;

  constructor(
    private fb: FormBuilder,
    private depenseService: DepenseService,
    private budgetService: BudgetService,
    private categorieService: CategorieDepenseService,
    protected router: Router
  ) {
    this.depenseForm = this.fb.group({
      description: ['', [Validators.required, Validators.minLength(3)]],
      montant: [0, [Validators.required, Validators.min(0.01)]],
      dateDepense: ['', Validators.required],
      idBudget: ['', Validators.required], // Added to form
      idCategorie: ['', Validators.required] // Added to form
    });
  }

  ngOnInit(): void {
    this.loadBudgets();
    this.loadCategories();
  }

  loadBudgets(): void {
    this.budgetService.getAllBudgets().subscribe({
      next: (data) => this.budgets = data,
      error: (err) => console.error('Error loading budgets:', err)
    });
  }

  loadCategories(): void {
    this.categorieService.getAllCategories().subscribe({
      next: (data) => this.categories = data,
      error: (err) => console.error('Error loading categories:', err)
    });
  }

  submit(): void {
    if (this.depenseForm.invalid) {
      this.depenseForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    const formValue = this.depenseForm.value;

    this.depenseService.addDepense(
      {
        description: formValue.description,
        montant: formValue.montant,
        dateDepense: formValue.dateDepense
      },
      +formValue.idBudget, // Convert to number
      +formValue.idCategorie // Convert to number
    ).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/budget/listDepense']);
      },
      error: (err) => {
        console.error('Error adding depense:', err);
        this.loading = false;
      }
    });
  }

  get f() {
    return this.depenseForm.controls;
  }

  protected readonly routes = routes;
}
