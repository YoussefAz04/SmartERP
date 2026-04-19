import { Component, OnInit } from '@angular/core';

import {DatePipe, DecimalPipe, NgForOf, NgIf} from "@angular/common";
import { Router } from "@angular/router";
import {Depense} from "../../../model/depense.model";
import {DepenseService} from "../../../core/service/depense/depense.service";
import {ResponseDto} from "../../../model/reponseDto.model";
import {PermissionDto} from "../../../model/permissionDto.model";
import { NotificationService } from '../../../shared/notification/notification.service';

@Component({
  selector: 'app-depense',
  standalone: true,
  imports: [
    NgIf,
    DecimalPipe,
    NgForOf,
    DatePipe
  ],
  templateUrl: './depense.component.html',
  styleUrls: ['./depense.component.scss']
})
export class DepenseComponent implements OnInit {

  depenses: Depense[] = [];
  loading: boolean = false;
  permissionDepense!: PermissionDto;

  constructor(private depenseService: DepenseService,
    protected router: Router,
    private notification: NotificationService) {}

  ngOnInit(): void {
    this.loadDepenses();
    this.getUserAuthData();
  }

  loadDepenses(): void {
    this.loading = true;
    this.depenseService.getAllDepenses().subscribe({
      next: (data) => {
        this.depenses = data;
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.loading = false;
      }
    });
  }

  addDepense(): void {
    this.router.navigate(['/budget/addDepense']);
    console.log("Ouverture du formulaire d’ajout de dépense");
  }

  viewDepense(id: number | undefined): void {
    if (id !== undefined) {
      this.router.navigate([`/budget/detaisDepense/${id}`]);
      console.log("Voir détails dépense:", id);
    }
  }


  deleteDepense(id: number | undefined): void {
    if (id !== undefined) {
      this.notification.confirm("Voulez-vous vraiment supprimer cette dépense ?").then(ok => {
        if (ok) {
          this.depenseService.deleteDepense(id).subscribe({
            next: () => this.loadDepenses(),
            error: (err) => console.error(err)
          });
        }
      });
    }
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
