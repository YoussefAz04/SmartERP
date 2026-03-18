import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {FactureDeVenteServiceService} from "../../../core/service/factureDeVente/facture-de-vente-service.service";
import {DecimalPipe, NgForOf, NgIf} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {PaginatorModule} from "primeng/paginator";
import {FactureVente} from "../../../model/FactureDeVente.model";
import {ResponseDto} from "../../../model/reponseDto.model";
import {PermissionDto} from "../../../model/permissionDto.model";

@Component({
  selector: 'app-detais-facture-de-vente',
  standalone: true,
  imports: [
    DecimalPipe,
    FormsModule,
    NgIf,
    PaginatorModule,
    NgForOf
  ],
  templateUrl: './detais-facture-de-vente.component.html',
  styleUrl: './detais-facture-de-vente.component.scss'
})
export class DetaisFactureDeVenteComponent implements OnInit{
  factureDetais !:FactureVente ;
  factureId !: number;
  permissionVente!: PermissionDto;

  constructor(private factureService :  FactureDeVenteServiceService,
              private activatedRouter :ActivatedRoute,
              private router : Router) {

  }

  ngOnInit() {
    this.factureId = this.activatedRouter.snapshot.params['id'];
    this.factureService.getFactureById(this.factureId).subscribe(data => {
      this.factureDetais = data;
      console.log(data);
    })
  }
  onSubmit() {
    this.factureService.updateFacture(this.factureId,this.factureDetais).subscribe(()=>{
      console.log("le update est succes");
      this.router.navigate(['/facture/listFactureAchat']);
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
        if (per.moduleTitle == "Vente"){
          this.permissionVente = per ;
          console.log("permission Vente" ,this.permissionVente);
        }
      })
      console.log('Vente:', currentUser?.entreprise.nom);
    }
  }

}
