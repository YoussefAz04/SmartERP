import {Component, OnInit} from '@angular/core';
import {FactureAchat} from "../../../model/FactureAchat.model";
import {FactureService} from "../../../core/service/facture/facture.service";
import {MatTooltip} from "@angular/material/tooltip";
import {DecimalPipe, NgForOf, NgIf} from "@angular/common";
import {Router} from "@angular/router";
import {ResponseDto} from "../../../model/reponseDto.model";
import {PermissionDto} from "../../../model/permissionDto.model";

@Component({
  selector: 'app-listfactur-achat',
  standalone: true,
  imports: [
    MatTooltip,
    DecimalPipe,
    NgForOf,
    NgIf
  ],
  templateUrl: './listfactur-achat.component.html',
  styleUrl: './listfactur-achat.component.scss'
})
export class ListfacturAchatComponent implements OnInit{
  factureAchatList : FactureAchat[] = [] ;
  permissionAchat!: PermissionDto;

  constructor(private factureService : FactureService,
              private router : Router) {
  }
  ngOnInit() {
   this.getAllFactureAchat();
   this.getUserAuthData();
  }
  getAllFactureAchat(){
    this.factureService.getAllFactureAchat().subscribe(data =>{
      this.factureAchatList =data ;
    })
  }

  viewFacture(id: number | undefined) {
    this.router.navigate(['facture/detaisFactureAchat',id])
  }

  deleteFacture(id: number | undefined) {

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
        if (per.moduleTitle == "Achat"){
          this.permissionAchat = per ;
          console.log("permission Achat" ,this.permissionAchat);
        }
      })
      console.log('Achat:', currentUser?.entreprise.nom);
    }
  }
}
