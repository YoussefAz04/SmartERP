import {Component, OnInit} from '@angular/core';
import {Equipe} from "../../../model/equipe.model";
import {EquipeService} from "../../../core/service/equipe/equipe.service";
import {MembreService} from "../../../core/service/membre/membre.service";
import {FormBuilder, FormGroup, ReactiveFormsModule} from "@angular/forms";
import {Validators} from "ngx-editor";
import {NgForOf} from "@angular/common";
import {sharedModule} from "../../../shared/shared.module";
import {Router, RouterLink} from "@angular/router";
import {routes} from "../../../core/helpers/routes";

@Component({
  selector: 'app-add-membre',
  standalone: true,
  imports: [
    NgForOf,
    ReactiveFormsModule,
    sharedModule,
    RouterLink
  ],
  templateUrl: './add-membre.component.html',
  styleUrl: './add-membre.component.scss'
})
export class AddMembreComponent implements OnInit{

  equipeList : Equipe[] =[];
  membreForm !: FormGroup ;

  constructor(private  equipeService : EquipeService ,
              private membreService : MembreService,
              private  fb: FormBuilder,
              private router : Router) {
  }
  ngOnInit() {
     this.equipeService.getAllEquipes().subscribe(data =>{
       this.equipeList = data ;
     })

    this.membreForm = this.fb.group({
      membreTitre: ['', Validators.required],
      description: ['', Validators.required],
      specialite: ['', Validators.required()],
      idEquipe :['', Validators.required()]
    });

  }

  onSubmit(){
    if (this.membreForm.valid){
      const formValue = this.membreForm.value;
      const equipeId = formValue.idEquipe ;
      this.membreService.addMembre(this.membreForm.value,equipeId).subscribe(data =>{
        console.log('ajoute avec succes', data);
        this.router.navigate(['/equipe/listMembre'])
      })
    }
  }


  protected readonly routes = routes;
}
