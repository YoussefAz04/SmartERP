import {Component, OnInit} from '@angular/core';
import {ClientService} from "../../core/service/client/client.service";
import {Router} from "@angular/router";
import {Client} from "../../model/client.model";
import {MatTooltip} from "@angular/material/tooltip";
import {NgForOf, NgIf} from "@angular/common";
import {ResponseDto} from "../../model/reponseDto.model";
import {PermissionDto} from "../../model/permissionDto.model";

@Component({
  selector: 'app-client',
  standalone: true,
  imports: [
    MatTooltip,
    NgForOf,
    NgIf
  ],
  templateUrl: './client.component.html',
  styleUrl: './client.component.scss'
})
export class ClientComponent implements OnInit{
  clientList : Client[] = [] ;
  permissionClient!: PermissionDto;
  constructor(private Clientservise: ClientService,private router : Router) {
  }
  ngOnInit() {
    this.Clientservise.getAllClients().subscribe(data => {
      this.clientList = data ;
      console.log(this.clientList);
    })
    this.getUserAuthData();
  }


  addClient() {
    this.router.navigate(['/client/addClient'])

  }

  viewClient(id: number | undefined) {
    return id ;
  }

  deleteClient(id: number | undefined) {
    return id ;
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
        if (per.moduleTitle.includes("gestion de Client")){
          this.permissionClient = per ;
          console.log("permission Client" ,this.permissionClient);
        }
      })
      console.log('Client:', currentUser?.entreprise.nom);
    }
  }

}
