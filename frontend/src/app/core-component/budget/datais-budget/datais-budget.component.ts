import {Component, OnInit} from '@angular/core';
import {PermissionDto} from "../../../model/permissionDto.model";
import {ActivatedRoute, Router, RouterLink} from "@angular/router";
import {BudgetService} from "../../../core/service/Budget/budget.service";
import {Budget} from "../../../model/budget.model";
import {FormsModule} from "@angular/forms";
import {NgIf} from "@angular/common";
import {ResponseDto} from "../../../model/reponseDto.model";

@Component({
  selector: 'app-datais-budget',
  standalone: true,
  imports: [
    FormsModule,
    RouterLink,
    NgIf
  ],
  templateUrl: './datais-budget.component.html',
  styleUrl: './datais-budget.component.scss'
})
export class DataisBudgetComponent implements OnInit{
  budgetId! : number ;
  budget !: Budget ;
   permissionBudget!: PermissionDto;
  constructor(private budgetService : BudgetService, private routerLink : ActivatedRoute, protected router : Router) {
  }

  ngOnInit() {
    this.budgetId = this.routerLink.snapshot.params['id'];
    this.budgetService.getBudgetById(this.budgetId).subscribe(data =>{
      this.budget = data ;
    })
    this.getUserAuthData() ;
  }

  onSubmit() {
    this.budgetService.updateBudget(this.budgetId,this.budget).subscribe(data =>{
      console.log(data);
      this.router.navigate(['/budget/listBudget'])
    })
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
