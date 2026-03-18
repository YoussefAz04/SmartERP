import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {NgIf, NgForOf, DatePipe} from '@angular/common';

import {TacheService} from "../../../core/service/taches/tache.service";
import {CategorieTacheService} from "../../../core/service/categorieTache/categorie-tache.service";

import {Tache, StatutTache, PrioriteDeTache} from "../../../model/tache.model";
import {CategorieTache} from "../../../model/categorieTache.model";
import {routes} from "../../../core/helpers/routes";
import {SousTacheService} from "../../../core/service/sousTache/sous-tache.service";
import {SousTache} from "../../../model/sousTache.model";
import {MatTooltip} from "@angular/material/tooltip";
import {ResponseDto} from "../../../model/reponseDto.model";
import {PermissionDto} from "../../../model/permissionDto.model";

@Component({
  selector: 'app-detais-tache',
  standalone: true,
    imports: [
        RouterLink,
        NgIf,
        NgForOf,
        DatePipe,
        ReactiveFormsModule,
        MatTooltip
    ],
  templateUrl: './detais-tache.component.html',
  styleUrl: './detais-tache.component.scss'
})
export class DetaisTacheComponent implements OnInit {

  idTache!: number;
  tacheDetails!: Tache;

  sousTacheForm!: FormGroup;
  priorites = Object.values(PrioriteDeTache);
  statuts = Object.values(StatutTache);

  categorieTache: CategorieTache[] = [];
  permissionTaches!: PermissionDto;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private tacheService: TacheService,
    private categorieTacheService: CategorieTacheService,
    private sousTacheService : SousTacheService
  ) {}

  ngOnInit(): void {
    this.idTache = this.route.snapshot.params['id'];
    this.getUserAuthData();

    this.categorieTacheService.getAllCategories().subscribe(data => {
      this.categorieTache = data;
    });

    this.tacheService.getTacheById(this.idTache).subscribe(data => {
      this.tacheDetails = data;


      this.sousTacheForm = this.fb.group({
        titre: ['', Validators.required],
        description: [''],
        type: ['', Validators.required],
        dateDebut: [''],
        dateFin: [''],
        progres: [0, ],
        duree: [0, Validators.required],
      });
    });
  }

  onSubmit(): void {
        if (this.sousTacheForm.valid){
          this.sousTacheService.createWithTache(this.sousTacheForm.value,this.idTache).subscribe(()=>{
            console.log("add with succes");
            this.tacheService.getTacheById(this.idTache).subscribe(data => {
              this.tacheDetails = data;});

          })
        }
  }
  protected readonly routes = routes;

  getTachesParType(type: string): SousTache[] {
    return this.tacheDetails.sousTaches?.filter(tache =>
      tache.type?.includes(type)
    ) || [];
  }

  updateTache(id: number | undefined) {
    this.router.navigate(['/projet/updateTache', id]);
  }


  detaisSousTache(id: number | undefined) {
    this.router.navigate(['/projet/detaisSousTache', id])
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
