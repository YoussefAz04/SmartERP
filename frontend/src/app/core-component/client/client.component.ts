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
  clientList : Client[] = [];
  permissionClient: PermissionDto = {
    create: false,
    view: false,
    update: false,
    delete: false,
    moduleTitle: ''
  } as PermissionDto;

  constructor(private Clientservise: ClientService, private router: Router) {
  }

  ngOnInit() {
    this.loadClients();
    this.getUserAuthData();
  }

  loadClients(): void {
    this.Clientservise.getAllClients().subscribe({
      next: (data) => {
        this.clientList = data;
        console.log('Clients loaded:', this.clientList);
      },
      error: (err) => console.error('Error loading clients:', err)
    });
  }

  addClient() {
    this.router.navigate(['/client/addClient']);
  }

  viewClient(id: number | undefined) {
    if (id) {
      this.router.navigate(['/client/viewClient', id]);
    }
  }

  deleteClient(id: number | undefined) {
    if (id && confirm('Are you sure?')) {
      this.Clientservise.deleteClient(id).subscribe({
        next: () => {
          this.clientList = this.clientList.filter(c => c.id !== id);
          console.log('Client deleted');
        },
        error: (err) => console.error('Error deleting client:', err)
      });
    }
  }

  getUserAuthData(){
    const item = localStorage.getItem('myLSkey');
    let currentUser: ResponseDto | null = null;

    if (item) {
      currentUser = JSON.parse(atob(item));
      console.log('User connecté:', currentUser?.name);
      console.log('Token:', currentUser?.token);
      console.log('Permissions:', currentUser?.permission);

      if (currentUser?.permission && Array.isArray(currentUser.permission)) {
        currentUser.permission.forEach((per: any) => {
          if (per.moduleTitle?.includes("gestion de Client") || per.moduleTitle?.includes("gestion client")) {
            this.permissionClient = per;
            console.log("Permission Client:", this.permissionClient);
          }
        });
      }

      console.log('Entreprise:', currentUser?.entreprise?.nom);
    } else {
      console.warn("No user found in localStorage");
    }
  }
  ngAfterViewInit() {
  console.log('Final permissionClient:', this.permissionClient);
  console.log('Create permission:', this.permissionClient?.create);
}
}