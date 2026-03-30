import {Component, OnInit} from '@angular/core';
import {MatTooltip} from "@angular/material/tooltip";
import {NgForOf, NgIf} from "@angular/common";
import {SousTache} from "../../../model/sousTache.model";
import {SousTacheService} from "../../../core/service/sousTache/sous-tache.service";
import {Router} from "@angular/router";
import {FormBuilder, FormGroup, ReactiveFormsModule} from "@angular/forms";
import {SharedModule} from "../../../shared/shared.module";
import {Tache} from "../../../model/tache.model";
import {TacheService} from "../../../core/service/taches/tache.service";
import {ResponseDto} from "../../../model/reponseDto.model";
import {PermissionDto} from "../../../model/permissionDto.model";

@Component({
  selector: 'app-list-sous-tache',
  standalone: true,
  imports: [
    MatTooltip,
    NgIf,
    NgForOf,
    ReactiveFormsModule,
    SharedModule
  ],
  templateUrl: './list-sous-tache.component.html',
  styleUrl: './list-sous-tache.component.scss'
})
export class ListSousTacheComponent implements OnInit{
  tacheList : SousTache[] = [] ;
  assignTacheForm!: FormGroup;
  taches : Tache[] = [] ;
  idSousTache !: number ;
  permissionTaches!: PermissionDto;

  constructor(private sousTacheService : SousTacheService,
              private router : Router,
              private tacheService : TacheService ,
              private fb : FormBuilder
  ) {
  }
  ngOnInit() {

    this.assignTacheForm = this.fb.group({
      tacheId: ['']
    });

   this.loadSousTache() ;
   this.getUserAuthData();



  }
  addSousTache() {
    this.router.navigate(['projet/addSousTache'])
  }

  deleteSousTache(id :any) {
    return id ;

  }

  viewSousTache(id: number | undefined) {
    return id ;
  }

  assigneSousTacheATache(id: number | undefined) {
    if (id)
    this.idSousTache = id ;

    this.tacheService.getAllTaches().subscribe(data=>{
      this.taches = data ;
    })
  }
  loadSousTache(){
    this.sousTacheService.getAll().subscribe(data=>{
      this.tacheList = data ;
    })

  }


  onSubmitAssignTache() {
    const tacheId = this.assignTacheForm.value.tacheId;

    if (this.idSousTache && tacheId) {
      this.sousTacheService.assignerSousTache(this.idSousTache, tacheId).subscribe(()=>{
          this.loadSousTache();
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
        if (per.moduleTitle == "Taches"){
          this.permissionTaches = per ;
          console.log("permission Taches" ,this.permissionTaches);
        }
      })
      console.log('Taches:', currentUser?.entreprise.nom);
    }
  }
}
