import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {PrioriteDeTache, StatutTache, Tache} from "../../../model/tache.model";
import {TacheService} from "../../../core/service/taches/tache.service";
import {FormsModule} from "@angular/forms";

@Component({
  selector: 'app-upadate-tache',
  standalone: true,
  imports: [
    FormsModule
  ],
  templateUrl: './upadate-tache.component.html',
  styleUrl: './upadate-tache.component.scss'
})
export class UpadateTacheComponent implements OnInit{
  idTache !: number ;
  tache !: Tache ;
  ngOnInit() {
    this.idTache = this.activatedRouter.snapshot.params['id'];
    this.getTacheById() ;
  }

  constructor(private router : Router ,
              private tacheService : TacheService,
              private activatedRouter : ActivatedRoute ) {
  }
  getTacheById(){
    this.tacheService.getTacheById(this.idTache).subscribe(data =>{
      this.tache =data ;
    })
  }

  onSubmit() {
    this.tacheService.updateTache(this.idTache,this.tache).subscribe(data =>{
      console.log("tache modifier" , data);
      this.router.navigate(['projet/detaisProjet',this.idTache])

    })
  }


  protected readonly StatutTache = StatutTache;
  protected readonly PrioriteDeTache = PrioriteDeTache;
}
