import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {ProjetService} from "../../../core/service/projet/projet.service";
import {Projet, StatutProjet} from "../../../model/projet.model";
import {FormsModule} from "@angular/forms";

@Component({
  selector: 'app-upadate-projet',
  standalone: true,
  imports: [
    FormsModule
  ],
  templateUrl: './upadate-projet.component.html',
  styleUrl: './upadate-projet.component.scss'
})
export class UpadateProjetComponent implements OnInit{

  idProjet !: number ;
  projet !: Projet ;
  ngOnInit() {
    this.idProjet = this.activatedRouter.snapshot.params['id'];
    this.getProjetById() ;
  }

  constructor(private router : Router ,
              private projetService : ProjetService,
              private activatedRouter : ActivatedRoute ) {
  }
  getProjetById(){
    this.projetService.getProjetById(this.idProjet).subscribe(data =>{
      this.projet =data ;
    })
  }

  onSubmit() {
    this.projetService.updateProjet(this.idProjet,this.projet).subscribe(data =>{
      console.log("projet modifier" , data);
      this.router.navigate(['projet/detaisProjet',this.idProjet])
    })
  }


  protected readonly StatutProjet = StatutProjet;
}
