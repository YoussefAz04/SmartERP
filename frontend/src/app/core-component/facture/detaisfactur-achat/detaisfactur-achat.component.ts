import {Component, OnInit} from '@angular/core';
import {FactureAchat} from "../../../model/FactureAchat.model";
import {FactureService} from "../../../core/service/facture/facture.service";
import {ActivatedRoute, Router} from "@angular/router";
import {FormsModule} from "@angular/forms";
import {DecimalPipe, NgIf} from "@angular/common";

@Component({
  selector: 'app-detaisfactur-achat',
  standalone: true,
  imports: [
    FormsModule,
    NgIf,
    DecimalPipe
  ],
  templateUrl: './detaisfactur-achat.component.html',
  styleUrl: './detaisfactur-achat.component.scss'
})
export class DetaisfacturAchatComponent  implements OnInit{
factureDetais !:FactureAchat ;
factureId !: number;

  constructor(private factureService :  FactureService,
              private activatedRouter :ActivatedRoute,
              private router : Router) {

  }

  ngOnInit() {
    this.factureId = this.activatedRouter.snapshot.params['id'];
    this.factureService.getFactureAchatById(this.factureId).subscribe(data => {
      this.factureDetais = data;
    })
  }
  onSubmit() {
    this.factureService.updateFactureAchat(this.factureId,this.factureDetais).subscribe(()=>{
      console.log("le update est succes");
      this.router.navigate(['/facture/listFactureAchat']);
    })
  }



}
