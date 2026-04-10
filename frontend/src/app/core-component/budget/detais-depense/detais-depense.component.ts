import {Component, OnInit} from '@angular/core';
import {Budget} from "../../../model/budget.model";
import {PermissionDto} from "../../../model/permissionDto.model";
import {BudgetService} from "../../../core/service/Budget/budget.service";
import {ActivatedRoute, Router, RouterLink} from "@angular/router";
import {Depense} from "../../../model/depense.model";
import {DepenseService} from "../../../core/service/depense/depense.service";
import {FormsModule} from "@angular/forms";
import {NgForOf, NgIf} from "@angular/common";
import {ResponseDto} from "../../../model/reponseDto.model";

@Component({
  selector: 'app-detais-depense',
  standalone: true,
  imports: [
    RouterLink,
    FormsModule,
    NgIf,
    NgForOf
  ],
  templateUrl: './detais-depense.component.html',
  styleUrl: './detais-depense.component.scss'
})
export class DetaisDepenseComponent implements OnInit{
  depenseId! : number ;
  depense !: Depense ;
  permissionDepense!: PermissionDto;
  constructor(private depenseService : DepenseService, private routerLink : ActivatedRoute, protected router : Router) {
  }

  ngOnInit() {
    this.depenseId = this.routerLink.snapshot.params['id'];
    this.depenseService.getDepenseById(this.depenseId).subscribe(data =>{
      this.depense = data ;
    })
  }

  onSubmit() {
    this.depenseService.updateDepense(this.depenseId,this.depense).subscribe(data =>{
      console.log(data);
      this.router.navigate(['/budget/listdepense'])
    })
  }


  getUserAuthData(){
    const item = localStorage.getItem('myLSkey');
    let currentUser: ResponseDto | null = null;

    if (item) {
      currentUser = JSON.parse(decodeURIComponent(escape(atob(item))));
      console.log('User connecté:', currentUser?.name);

      console.log('Token:', currentUser?.token);
      console.log('Permissions:', currentUser?.permission);
      currentUser?.permission.map(per =>{
        if (per.moduleTitle == "Depense"){
          this.permissionDepense = per ;
          console.log("permission Budget" ,this.permissionDepense);
        }
      })
      console.log('Depense:', currentUser?.entreprise.nom);
    }
  }
}
