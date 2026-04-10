import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {TacheService} from "../../../core/service/taches/tache.service";
import {SousTache, TypeSousTache} from "../../../model/sousTache.model";
import {FormsModule} from "@angular/forms";
import {ResponseDto} from "../../../model/reponseDto.model";
import {PermissionDto} from "../../../model/permissionDto.model";
import {NgIf} from "@angular/common";

@Component({
  selector: 'app-detais-sous-tache',
  standalone: true,
  imports: [
    FormsModule,
    NgIf
  ],
  templateUrl: './detais-sous-tache.component.html',
  styleUrl: './detais-sous-tache.component.scss'
})
export class DetaisSousTacheComponent implements OnInit{
  idTache !: number ;
  sousTache !: SousTache ;
  permissionTaches!: PermissionDto;
  ngOnInit() {
    this.idTache = this.activatedRouter.snapshot.params['id'];
    this.getTacheById() ;
    this.getUserAuthData();
  }

  constructor(private router : Router ,
              private tacheService : TacheService,
              private activatedRouter : ActivatedRoute ) {
  }
  getTacheById(){
    this.tacheService.getSousTacheById(this.idTache).subscribe(data =>{
      this.sousTache =data ;
      console.log(data) ;
    })
  }

  onSubmit() {
    this.tacheService.updateSousTache(this.idTache,this.sousTache).subscribe(data =>{
      console.log("tache modifier" , data);
      this.router.navigate(['projet/detaisTache',this.idTache])

    })
  }


  getUserAuthData(){
    const item = localStorage.getItem('myLSkey');
    let currentUser: ResponseDto | null = null;

    if (item) {
      currentUser = JSON.parse(decodeURIComponent(escape(atob(item))));
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
  protected readonly TypeSousTache = TypeSousTache;
}
