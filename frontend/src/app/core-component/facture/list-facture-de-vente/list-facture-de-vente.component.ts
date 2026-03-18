import {Component, OnInit} from '@angular/core';

import {Router} from "@angular/router";
import {FactureVente} from "../../../model/FactureDeVente.model";
import {FactureDeVenteServiceService} from "../../../core/service/factureDeVente/facture-de-vente-service.service";
import {DecimalPipe, NgForOf, NgIf} from "@angular/common";
import {MatTooltip} from "@angular/material/tooltip";
import {FormBuilder, FormGroup, ReactiveFormsModule} from "@angular/forms";
import {sharedModule} from "../../../shared/shared.module";
import {ClientService} from "../../../core/service/client/client.service";
import {Client} from "../../../model/client.model";
import {ResponseDto} from "../../../model/reponseDto.model";
import {PermissionDto} from "../../../model/permissionDto.model";

@Component({
  selector: 'app-list-facture-de-vente',
  standalone: true,
  imports: [
    DecimalPipe,
    MatTooltip,
    NgForOf,
    NgIf,
    ReactiveFormsModule,
    sharedModule
  ],
  templateUrl: './list-facture-de-vente.component.html',
  styleUrl: './list-facture-de-vente.component.scss'
})
export class ListFactureDeVenteComponent implements OnInit{
  factureAchatList : FactureVente[] = [] ;
  assignFactureForm!: FormGroup;
  permissionVente!: PermissionDto;


  constructor(private factureService : FactureDeVenteServiceService,
              private router : Router,
              private clientService : ClientService,
              private fb: FormBuilder,) {
  }
  ngOnInit() {
    this.getAllFactureAchat();
    this.assignFactureForm = this.fb.group({
      clientId: ['']
    });

    this.getUserAuthData() ;
  }
  getAllFactureAchat(){
    this.factureService.getAllFactures().subscribe(data =>{
      this.factureAchatList =data ;
    })
  }

  viewFacture(id: number | undefined) {
    this.router.navigate(['facture/detaisFactureVente',id])
  }

  deleteFacture(id: number | undefined) {
    if (id)
    this.factureService.deleteFacture(id).subscribe(()=>{
      this.getAllFactureAchat() ;
    });


  }
  generatePdf(facture:FactureVente){
    this.factureService.generateFactureVentePDF(facture) ;
  }
  sendEmailFactureVente(facture:FactureVente){
    this.factureService.sendEmailFactureVente(
      facture
    )
  }


  addFacture() {
     this.router.navigate(['facture/ajouterFactureDeVente'])
  }


  clients : Client[] = [] ;
  getAllClients(){
    this.clientService.getAllClients().subscribe(
      data =>{
        this.clients =data ;
      }
    )
  }

  idFactureAssigne !: number ;
  factureAssigne(id: number | undefined) {
    if(id)
    this.idFactureAssigne = id ;
    this.getAllClients()
  }


  onSubmitAssignFacture() {
    const clientId = this.assignFactureForm.value.clientId;

    if (this.assignFactureForm && clientId) {
      this.factureService.affecterClient(this.idFactureAssigne, clientId).subscribe(()=>{
          console.log("assigne ///")
        }
      );
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
      currentUser?.permission.map(per =>{
        if (per.moduleTitle == "Vente"){
          this.permissionVente = per ;
          console.log("permission Vente" ,this.permissionVente);
        }
      })
      console.log('Vente:', currentUser?.entreprise.nom);
    }
  }
}
