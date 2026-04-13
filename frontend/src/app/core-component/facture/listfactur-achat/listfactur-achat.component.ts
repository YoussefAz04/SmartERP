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
  
  permissionAchat: PermissionDto = {
  id: 0,              // ⬅️ Add this
  moduleId: 0,        // ⬅️ Add this
  roleId: 0,          // ⬅️ Add this
  view: false,
  create: false,
  update: false,
  delete: false,
  moduleTitle: ''
};

  constructor(private factureService : FactureService,
              private router : Router) {
  }
  
  ngOnInit() {
    this.getAllFactureAchat();
    this.getUserAuthData();
  }
  
  getAllFactureAchat(){
    this.factureService.getAllFactureAchat().subscribe(data => {
      console.log('✅ Total invoices:', data.length);
      this.factureAchatList = data;
    }, error => {
      console.error('❌ Error:', error);
    })
  }

  viewFacture(id: number | undefined) {
    this.router.navigate(['facture/detaisFactureAchat', id])
  }

  deleteFacture(id: number | undefined) {
    if (id === undefined) return;
    if (!confirm('Êtes-vous sûr de vouloir supprimer cette facture ?')) return;
    this.factureService.deleteFactureAchat(id).subscribe({
      next: () => {
        this.factureAchatList = this.factureAchatList.filter(f => f.id !== id);
      },
      error: () => {
        alert('Erreur lors de la suppression de la facture.');
      }
    });
  }

  getUserAuthData(){
    const item = localStorage.getItem('myLSkey');
    let currentUser: ResponseDto | null = null;

    if (item) {
      currentUser = JSON.parse(atob(item));

      currentUser?.permission.map(per => {
        if (per.moduleTitle == "Achat"){
          this.permissionAchat = per;
        }
      })
    }
  }
}