import {Component, OnInit} from '@angular/core';
import {BudgetService} from "../../core/service/Budget/budget.service";
import {Budget} from "../../model/budget.model";
import {AsyncPipe, DecimalPipe, NgForOf, NgIf} from "@angular/common";
import {Router} from "@angular/router";
import {ResponseDto} from "../../model/reponseDto.model";
import {PermissionDto} from "../../model/permissionDto.model";
import {ProjetService} from "../../core/service/projet/projet.service";
import {map, Observable} from "rxjs";

@Component({
  selector: 'app-budget',
  standalone: true,
  imports: [
    NgIf,
    DecimalPipe,
    NgForOf,
    AsyncPipe
  ],
  templateUrl: './budget.component.html',
  styleUrl: './budget.component.scss'
})
export class BudgetComponent implements OnInit{
   permissionBudget!: PermissionDto;
  constructor(private budgetService : BudgetService,
              protected router : Router,
              private projetService : ProjetService) {
  }
  budgets : Budget[] = []
  loading: any;
  ngOnInit() {
    this.budgetService.getAllBudgets().subscribe(data =>{
      this.budgets = data ;
    })
    this.getUserAuthData() ;
  }
  addBudget() {
    this.router.navigate(['/budget/addBudget'])
    console.log("Ouverture du modal d’ajout de budget");
  }

  getProjetById(id: number): Observable<string> {
    return this.projetService.getProjetById(id).pipe(
      map(projet => projet.nom)
    );
  }

  deleteBudget(id: number | undefined) {
    if (confirm("Voulez-vous vraiment supprimer ce budget ?") && id) {
      this.budgetService.deleteBudget(id).subscribe(() => {
        this.loadBudgets();
      });
    }
  }

  loadBudgets() {
    this.budgetService.getAllBudgets().subscribe({
      next: (data) => (this.budgets = data),
      error: (err) => console.error(err),
    });
  }


  getUserAuthData(){
    const item = localStorage.getItem('myLSkey');
    let currentUser: ResponseDto | null = null;

    if (item) {
      currentUser = JSON.parse(atob(item));
      console.log('User connecté:', currentUser?.name);

      console.log('Token:', currentUser?.token);
      console.log('Permissions:', currentUser?.permission);
      currentUser?.permission.map(per =>{
        if (per.moduleTitle == "Budget"){
          this.permissionBudget = per ;
          console.log("permission Budget" ,this.permissionBudget);
        }
      })
      console.log('Budget:', currentUser?.entreprise.nom);
    }
  }
}
